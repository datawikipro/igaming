package pro.datawiki.igaming.source.esportesdasorte.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteMatchOddsData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteRawMatch;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeData;
import pro.datawiki.igaming.source.esportesdasorte.dto.EsportesdasorteStakeGroupData;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EsportesdasorteFeedClient implements WebSocket.Listener {

    @org.springframework.beans.factory.annotation.Value("${app.digitain.ws-url:wss://apisportfeed-ff.esportesdasorte.com/v3}")
    private String wsBaseUrl;

    @org.springframework.beans.factory.annotation.Value("${app.digitain.uuid:a93f412e-71bd-4912-9c12-38b8123da456}")
    private String apiUuid;

    private final AtomicInteger midCounter = new AtomicInteger(1000);
    private final ConcurrentHashMap<Integer, CompletableFuture<Value>> pendingFutures = new ConcurrentHashMap<>();
    private final ByteArrayOutputStream incomingBuffer = new ByteArrayOutputStream();

    private WebSocket webSocket;
    private HttpClient httpClient;
    private ScheduledExecutorService pingExecutor;
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.pingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "esportesdasorte-ws-ping");
            t.setDaemon(true);
            return t;
        });
        this.pingExecutor.scheduleAtFixedRate(this::sendPing, 10, 10, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void cleanup() {
        if (pingExecutor != null) {
            pingExecutor.shutdownNow();
        }
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutting down").join();
            } catch (Exception ignored) {
            }
        }
    }

    private synchronized void ensureConnected() {
        if (webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed()) {
            return;
        }

        try {
            int connId = 100000000 + random.nextInt(900000000);
            String url = String.format("%s/%s/1/2/0/%d/1", wsBaseUrl, apiUuid, connId);
            log.info("Connecting to Esportes da Sorte WebSocket feed: {}", url);

            webSocket = httpClient.newWebSocketBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0 Safari/537.36")
                    .header("Origin", "https://esportesdasorte.com")
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(url), this)
                    .get(15, TimeUnit.SECONDS);

            log.info("Successfully connected to Esportes da Sorte WebSocket feed!");
        } catch (Exception e) {
            log.error("Failed to connect to Esportes da Sorte WebSocket feed: {}", e.getMessage());
            throw new RuntimeException("Esportes da Sorte feed connection failed", e);
        }
    }

    private void sendPing() {
        if (webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed()) {
            try {
                int mid = midCounter.incrementAndGet();
                MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
                packer.packArrayHeader(3);
                packer.packInt(1);
                packer.packInt(mid);
                packer.packArrayHeader(1);
                packer.packInt(0);
                packer.close();
                webSocket.sendBinary(ByteBuffer.wrap(packer.toByteArray()), true);
            } catch (Exception e) {
                log.debug("Error sending ping to Esportes da Sorte feed: {}", e.getMessage());
            }
        }
    }

    public List<EsportesdasorteRawMatch> fetchTree() {
        ensureConnected();
        int mid = midCounter.incrementAndGet();
        CompletableFuture<Value> future = new CompletableFuture<>();
        pendingFutures.put(mid, future);

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(3);
            packer.packInt(1);
            packer.packInt(mid);
            packer.packArrayHeader(2);
            packer.packInt(1);
            packer.packArrayHeader(1);
            packer.packInt(1);
            packer.close();

            webSocket.sendBinary(ByteBuffer.wrap(packer.toByteArray()), true);
            Value response = future.get(15, TimeUnit.SECONDS);
            return parseTreeResponse(response);
        } catch (Exception e) {
            log.error("Failed to fetch tree from Esportes da Sorte feed: {}", e.getMessage());
            return Collections.emptyList();
        } finally {
            pendingFutures.remove(mid);
        }
    }

    public List<EsportesdasorteRawMatch> fetchMatchesDetails(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return Collections.emptyList();
        }

        ensureConnected();
        int mid = midCounter.incrementAndGet();
        CompletableFuture<Value> future = new CompletableFuture<>();
        pendingFutures.put(mid, future);

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(3);
            packer.packInt(1);
            packer.packInt(mid);
            packer.packArrayHeader(2);
            packer.packInt(2);
            packer.packArrayHeader(matchIds.size());
            for (Long id : matchIds) {
                packer.packLong(id);
            }
            packer.close();

            webSocket.sendBinary(ByteBuffer.wrap(packer.toByteArray()), true);
            Value response = future.get(15, TimeUnit.SECONDS);
            return parseMatchesDetailsResponse(response);
        } catch (Exception e) {
            log.error("Failed to fetch match details for {} matches: {}", matchIds.size(), e.getMessage());
            return Collections.emptyList();
        } finally {
            pendingFutures.remove(mid);
        }
    }

    public List<EsportesdasorteMatchOddsData> fetchMatchesOdds(List<Long> matchIds, List<Integer> stakeTypes) {
        if (matchIds == null || matchIds.isEmpty()) {
            return Collections.emptyList();
        }

        ensureConnected();
        int mid = midCounter.incrementAndGet();
        CompletableFuture<Value> future = new CompletableFuture<>();
        pendingFutures.put(mid, future);

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(3);
            packer.packInt(1);
            packer.packInt(mid);
            packer.packArrayHeader(3);
            packer.packInt(3);
            packer.packArrayHeader(matchIds.size());
            for (Long id : matchIds) {
                packer.packLong(id);
            }
            packer.packArrayHeader(stakeTypes.size());
            for (Integer st : stakeTypes) {
                packer.packInt(st);
            }
            packer.close();

            webSocket.sendBinary(ByteBuffer.wrap(packer.toByteArray()), true);
            Value response = future.get(15, TimeUnit.SECONDS);
            return parseMatchesOddsResponse(response);
        } catch (Exception e) {
            log.error("Failed to fetch odds for {} matches: {}", matchIds.size(), e.getMessage());
            return Collections.emptyList();
        } finally {
            pendingFutures.remove(mid);
        }
    }

    private List<EsportesdasorteRawMatch> parseTreeResponse(Value val) {
        List<EsportesdasorteRawMatch> matches = new ArrayList<>();
        try {
            if (!val.isArrayValue()) return matches;
            ArrayValue arr = val.asArrayValue();
            if (arr.size() < 2 || !arr.get(1).isArrayValue()) return matches;

            ArrayValue sportsArr = arr.get(1).asArrayValue();
            for (Value sportVal : sportsArr) {
                if (!sportVal.isArrayValue()) continue;
                ArrayValue sArr = sportVal.asArrayValue();
                String sportName = sArr.size() > 1 && sArr.get(1).isStringValue() ? sArr.get(1).asStringValue().asString() : "Football";

                if (sArr.size() > 2 && sArr.get(2).isArrayValue()) {
                    ArrayValue catArr = sArr.get(2).asArrayValue();
                    for (Value catVal : catArr) {
                        if (!catVal.isArrayValue()) continue;
                        ArrayValue cArr = catVal.asArrayValue();
                        if (cArr.size() > 2 && cArr.get(2).isArrayValue()) {
                            ArrayValue champArr = cArr.get(2).asArrayValue();
                            for (Value champVal : champArr) {
                                if (!champVal.isArrayValue()) continue;
                                ArrayValue chArr = champVal.asArrayValue();
                                String leagueName = chArr.size() > 1 && chArr.get(1).isStringValue() ? chArr.get(1).asStringValue().asString() : "";

                                if (chArr.size() > 2 && chArr.get(2).isArrayValue()) {
                                    ArrayValue matchArr = chArr.get(2).asArrayValue();
                                    for (Value mVal : matchArr) {
                                        if (!mVal.isArrayValue()) continue;
                                        ArrayValue m = mVal.asArrayValue();
                                        Long matchId = m.size() > 0 && m.get(0).isIntegerValue() ? m.get(0).asIntegerValue().asLong() : null;
                                        if (matchId == null) continue;

                                        Long startTime = m.size() > 2 && m.get(2).isIntegerValue() ? m.get(2).asIntegerValue().asLong() : null;
                                        Boolean isLive = m.size() > 3 && m.get(3).isBooleanValue() ? m.get(3).asBooleanValue().getBoolean() : false;

                                        matches.add(EsportesdasorteRawMatch.builder()
                                                .id(matchId)
                                                .sportName(sportName)
                                                .leagueName(leagueName)
                                                .startTime(startTime)
                                                .isLive(isLive)
                                                .build());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Esportes da Sorte tree response: {}", e.getMessage());
        }
        return matches;
    }

    private List<EsportesdasorteRawMatch> parseMatchesDetailsResponse(Value val) {
        List<EsportesdasorteRawMatch> details = new ArrayList<>();
        try {
            if (!val.isArrayValue()) return details;
            ArrayValue arr = val.asArrayValue();
            if (arr.size() < 2 || !arr.get(1).isArrayValue()) return details;

            ArrayValue matchesArr = arr.get(1).asArrayValue();
            for (Value mVal : matchesArr) {
                if (!mVal.isArrayValue()) continue;
                ArrayValue m = mVal.asArrayValue();
                Long matchId = m.size() > 0 && m.get(0).isIntegerValue() ? m.get(0).asIntegerValue().asLong() : null;
                if (matchId == null) continue;

                String name = m.size() > 1 && m.get(1).isStringValue() ? m.get(1).asStringValue().asString() : null;
                String home = m.size() > 2 && m.get(2).isStringValue() ? m.get(2).asStringValue().asString() : null;
                String away = m.size() > 3 && m.get(3).isStringValue() ? m.get(3).asStringValue().asString() : null;

                details.add(EsportesdasorteRawMatch.builder()
                        .id(matchId)
                        .name(name)
                        .homeTeam(home)
                        .awayTeam(away)
                        .build());
            }
        } catch (Exception e) {
            log.error("Error parsing Esportes da Sorte match details: {}", e.getMessage());
        }
        return details;
    }

    private List<EsportesdasorteMatchOddsData> parseMatchesOddsResponse(Value val) {
        List<EsportesdasorteMatchOddsData> result = new ArrayList<>();
        try {
            if (!val.isArrayValue()) return result;
            ArrayValue arr = val.asArrayValue();
            if (arr.size() < 2 || !arr.get(1).isArrayValue()) return result;

            ArrayValue matchesArr = arr.get(1).asArrayValue();
            for (Value mVal : matchesArr) {
                if (!mVal.isArrayValue()) continue;
                ArrayValue m = mVal.asArrayValue();
                Long matchId = m.size() > 0 && m.get(0).isIntegerValue() ? m.get(0).asIntegerValue().asLong() : null;
                if (matchId == null) continue;

                List<EsportesdasorteStakeGroupData> groups = new ArrayList<>();
                if (m.size() > 1 && m.get(1).isArrayValue()) {
                    ArrayValue groupsArr = m.get(1).asArrayValue();
                    for (Value gVal : groupsArr) {
                        if (!gVal.isArrayValue()) continue;
                        ArrayValue g = gVal.asArrayValue();
                        Long groupId = g.size() > 0 && g.get(0).isIntegerValue() ? g.get(0).asIntegerValue().asLong() : null;
                        String nameRu = g.size() > 1 && g.get(1).isStringValue() ? g.get(1).asStringValue().asString() : null;
                        String nameEn = g.size() > 2 && g.get(2).isStringValue() ? g.get(2).asStringValue().asString() : null;

                        List<EsportesdasorteStakeData> stakes = new ArrayList<>();
                        if (g.size() > 3 && g.get(3).isArrayValue()) {
                            ArrayValue stakesArr = g.get(3).asArrayValue();
                            for (Value sVal : stakesArr) {
                                if (!sVal.isArrayValue()) continue;
                                ArrayValue s = sVal.asArrayValue();
                                Long stakeId = s.size() > 0 && s.get(0).isIntegerValue() ? s.get(0).asIntegerValue().asLong() : null;
                                String sNameRu = s.size() > 1 && s.get(1).isStringValue() ? s.get(1).asStringValue().asString() : null;
                                String sNameEn = s.size() > 2 && s.get(2).isStringValue() ? s.get(2).asStringValue().asString() : null;
                                Double factor = s.size() > 3 && s.get(3).isFloatValue() ? s.get(3).asFloatValue().toDouble() :
                                        (s.size() > 3 && s.get(3).isIntegerValue() ? s.get(3).asIntegerValue().toDouble() : null);
                                Double arg = s.size() > 4 && s.get(4).isFloatValue() ? s.get(4).asFloatValue().toDouble() :
                                        (s.size() > 4 && s.get(4).isIntegerValue() ? s.get(4).asIntegerValue().toDouble() : null);

                                stakes.add(EsportesdasorteStakeData.builder()
                                        .id(stakeId)
                                        .nameRu(sNameRu)
                                        .nameEn(sNameEn)
                                        .factor(factor)
                                        .argument(arg)
                                        .build());
                            }
                        }

                        groups.add(EsportesdasorteStakeGroupData.builder()
                                .id(groupId)
                                .nameRu(nameRu)
                                .nameEn(nameEn)
                                .stakes(stakes)
                                .build());
                    }
                }

                result.add(EsportesdasorteMatchOddsData.builder()
                        .matchId(matchId)
                        .groups(groups)
                        .build());
            }
        } catch (Exception e) {
            log.error("Error parsing Esportes da Sorte match odds: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer data, boolean last) {
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        incomingBuffer.write(bytes, 0, bytes.length);

        if (last) {
            byte[] fullPayload = incomingBuffer.toByteArray();
            incomingBuffer.reset();

            try {
                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(fullPayload);
                Value val = unpacker.unpackValue();
                unpacker.close();

                if (val.isArrayValue()) {
                    ArrayValue arr = val.asArrayValue();
                    if (arr.size() >= 3) {
                        int mid = arr.get(1).asIntegerValue().asInt();
                        CompletableFuture<Value> future = pendingFutures.get(mid);
                        if (future != null) {
                            future.complete(arr.get(2));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Error unpacking MessagePack message: {}", e.getMessage());
            }
        }

        ws.request(1);
        return null;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("Esportes da Sorte WebSocket connection opened.");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.warn("Esportes da Sorte WebSocket closed: {} / {}", statusCode, reason);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Esportes da Sorte WebSocket error: {}", error.getMessage());
    }
}
