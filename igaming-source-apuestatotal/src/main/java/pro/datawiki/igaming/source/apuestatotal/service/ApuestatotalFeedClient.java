package pro.datawiki.igaming.source.apuestatotal.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.Value;
import org.springframework.stereotype.Service;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalMatchOddsData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalRawMatch;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeData;
import pro.datawiki.igaming.source.apuestatotal.dto.ApuestatotalStakeGroupData;

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
public class ApuestatotalFeedClient implements WebSocket.Listener {

    @org.springframework.beans.factory.annotation.Value("${app.digitain.ws-url:wss://apisportfeed-ff.apuestatotal.com/v3}")
    private String wsBaseUrl;

    @org.springframework.beans.factory.annotation.Value("${app.digitain.uuid:35c6d708-2caa-464a-bab5-a6656b2b80f3}")
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
            Thread t = new Thread(r, "apuestatotal-ws-ping");
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
            log.info("Connecting to Apuesta Total WebSocket feed: {}", url);

            webSocket = httpClient.newWebSocketBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/133.0.0.0 Safari/537.36")
                    .header("Origin", "https://apuestatotal.com")
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(url), this)
                    .get(15, TimeUnit.SECONDS);

            log.info("Successfully connected to Apuesta Total WebSocket feed!");
        } catch (Exception e) {
            log.error("Failed to connect to Apuesta Total WebSocket feed: {}", e.getMessage());
            throw new RuntimeException("Apuesta Total feed connection failed", e);
        }
    }

    private void sendPing() {
        if (webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed()) {
            try {
                int mid = midCounter.incrementAndGet();
                MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
                packer.packInt(mid);
                packer.packInt(4); // CMD 4: ping
                packer.packNil();
                packer.close();
                webSocket.sendBinary(ByteBuffer.wrap(packer.toByteArray()), true);
            } catch (Exception e) {
                log.debug("Failed sending ping to Apuesta Total WS: {}", e.getMessage());
            }
        }
    }

    public CompletableFuture<Value> sendCommand(int cmd, byte[] packedData) {
        ensureConnected();
        int mid = midCounter.incrementAndGet();
        CompletableFuture<Value> future = new CompletableFuture<>();
        pendingFutures.put(mid, future);

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packInt(mid);
            packer.packInt(cmd);
            if (packedData != null && packedData.length > 0) {
                packer.writePayload(packedData);
            } else {
                packer.packNil();
            }
            packer.close();
            byte[] msgBytes = packer.toByteArray();

            webSocket.sendBinary(ByteBuffer.wrap(msgBytes), true);
        } catch (Exception e) {
            pendingFutures.remove(mid);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("Apuesta Total WebSocket connected and ready");
        webSocket.request(1);
    }

    @Override
    public synchronized CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        incomingBuffer.write(bytes, 0, bytes.length);

        if (last) {
            byte[] fullMessage = incomingBuffer.toByteArray();
            incomingBuffer.reset();
            handleBinaryMessage(fullMessage);
        }

        webSocket.request(1);
        return null;
    }

    private void handleBinaryMessage(byte[] fullMessage) {
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(fullMessage);
            while (unpacker.hasNext()) {
                Value val0 = unpacker.unpackValue();
                if (!val0.isIntegerValue()) {
                    continue;
                }
                int mid = val0.asIntegerValue().toInt();
                int status = unpacker.hasNext() ? unpacker.unpackValue().asIntegerValue().toInt() : 0;
                int type = unpacker.hasNext() ? unpacker.unpackValue().asIntegerValue().toInt() : 0;
                Value data = unpacker.hasNext() ? unpacker.unpackValue() : org.msgpack.value.ValueFactory.newNil();
                if (unpacker.hasNext()) {
                    unpacker.unpackValue();
                }

                log.info("Received Apuesta Total feed response: mid={}, status={}, type={}, data={}",
                        mid, status, type, data.getValueType());

                CompletableFuture<Value> future = pendingFutures.remove(mid);
                if (future != null) {
                    future.complete(data);
                }
            }
        } catch (Exception e) {
            log.error("Error decoding MessagePack frame: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.warn("Apuesta Total WebSocket error: {}", error.getMessage());
        pendingFutures.forEach((k, v) -> v.completeExceptionally(error));
        pendingFutures.clear();
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.warn("Apuesta Total WebSocket closed: {} ({})", reason, statusCode);
        pendingFutures.forEach((k, v) -> v.completeExceptionally(new RuntimeException("Socket closed: " + reason)));
        pendingFutures.clear();
        return null;
    }

    /**
     * CMD 9: GetTree.
     * Returns all sports, categories, tournaments, and match skeletons.
     */
    public List<ApuestatotalRawMatch> fetchTree() {
        try {
            Value data = sendCommand(9, null).get(15, TimeUnit.SECONDS);
            if (data == null || !data.isArrayValue()) {
                log.warn("GetTree returned null or non-array data");
                return Collections.emptyList();
            }

            List<ApuestatotalRawMatch> matches = new ArrayList<>();
            ArrayValue sportsArray = data.asArrayValue();

            for (Value sportVal : sportsArray) {
                if (!sportVal.isArrayValue()) continue;
                ArrayValue sport = sportVal.asArrayValue();
                String sportName = sport.size() > 3 ? sport.get(3).asStringValue().asString() : "Football";

                if (sport.size() <= 5 || !sport.get(5).isArrayValue()) continue;
                ArrayValue categories = sport.get(5).asArrayValue();

                for (Value catVal : categories) {
                    if (!catVal.isArrayValue()) continue;
                    ArrayValue category = catVal.asArrayValue();

                    if (category.size() <= 5 || !category.get(5).isArrayValue()) continue;
                    ArrayValue tournaments = category.get(5).asArrayValue();

                    for (Value tournVal : tournaments) {
                        if (!tournVal.isArrayValue()) continue;
                        ArrayValue tournament = tournVal.asArrayValue();
                        String leagueName = tournament.size() > 4 ? tournament.get(4).asStringValue().asString() : "League";

                        if (tournament.size() <= 7 || !tournament.get(7).isArrayValue()) continue;
                        ArrayValue matchesArr = tournament.get(7).asArrayValue();

                        for (Value matchVal : matchesArr) {
                            if (!matchVal.isArrayValue()) continue;
                            ArrayValue m = matchVal.asArrayValue();
                            long matchId = m.get(0).asIntegerValue().toLong();

                            long startTimeMs = System.currentTimeMillis();
                            if (m.size() > 4 && !m.get(4).isNilValue()) {
                                Value timeVal = m.get(4);
                                if (timeVal.isIntegerValue()) {
                                    startTimeMs = timeVal.asIntegerValue().toLong() * 1000L;
                                }
                            }

                            boolean isLive = m.size() > 7 && !m.get(7).isNilValue() && m.get(7).asBooleanValue().getBoolean();

                            matches.add(ApuestatotalRawMatch.builder()
                                    .id(matchId)
                                    .sportName(sportName)
                                    .leagueName(leagueName)
                                    .startTime(startTimeMs)
                                    .isLive(isLive)
                                    .build());
                        }
                    }
                }
            }

            log.info("Apuesta Total GetTree parsed {} matches across {} sports", matches.size(), sportsArray.size());
            return matches;
        } catch (Exception e) {
            log.error("Failed to fetch Apuesta Total tree: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * CMD 34: GetTreeMixedMatches [matchIds, [], false].
     * Returns team names and event details for a batch of matches.
     */
    public List<ApuestatotalRawMatch> fetchMatchesDetails(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(3);
            packer.packArrayHeader(matchIds.size());
            for (Long id : matchIds) {
                packer.packLong(id);
            }
            packer.packArrayHeader(0);
            packer.packBoolean(false);
            packer.close();

            Value data = sendCommand(34, packer.toByteArray()).get(10, TimeUnit.SECONDS);
            if (data == null || !data.isArrayValue()) {
                return Collections.emptyList();
            }

            List<ApuestatotalRawMatch> results = new ArrayList<>();
            for (Value mVal : data.asArrayValue()) {
                if (!mVal.isArrayValue()) continue;
                ArrayValue m = mVal.asArrayValue();
                if (m.size() < 5) continue;

                long matchId = m.get(0).asIntegerValue().toLong();
                String name = m.size() > 2 && !m.get(2).isNilValue() ? m.get(2).asStringValue().asString() : "";
                String team1 = m.size() > 3 && !m.get(3).isNilValue() ? m.get(3).asStringValue().asString() : "";
                String team2 = m.size() > 4 && !m.get(4).isNilValue() ? m.get(4).asStringValue().asString() : "";

                results.add(ApuestatotalRawMatch.builder()
                        .id(matchId)
                        .name(name)
                        .homeTeam(team1)
                        .awayTeam(team2)
                        .build());
            }

            return results;
        } catch (Exception e) {
            log.debug("Error fetching Apuesta Total match details for batch: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * CMD 35: GetMixedMatchesPeriodsStakes [matchIds, stakeTypeIds, 0, 0].
     * Returns odds for matches.
     */
    public List<ApuestatotalMatchOddsData> fetchMatchesOdds(List<Long> matchIds, List<Integer> stakeTypes) {
        if (matchIds == null || matchIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            packer.packArrayHeader(4);
            packer.packArrayHeader(matchIds.size());
            for (Long id : matchIds) {
                packer.packLong(id);
            }
            packer.packArrayHeader(stakeTypes.size());
            for (Integer st : stakeTypes) {
                packer.packInt(st);
            }
            packer.packInt(0);
            packer.packInt(0);
            packer.close();

            Value data = sendCommand(35, packer.toByteArray()).get(10, TimeUnit.SECONDS);
            if (data == null || !data.isArrayValue()) {
                return Collections.emptyList();
            }

            List<ApuestatotalMatchOddsData> resultList = new ArrayList<>();
            for (Value mVal : data.asArrayValue()) {
                if (!mVal.isArrayValue()) continue;
                ArrayValue m = mVal.asArrayValue();
                if (m.size() < 3) continue;

                long matchId = m.get(0).asIntegerValue().toLong();
                if (!m.get(2).isArrayValue()) continue;

                List<ApuestatotalStakeGroupData> groups = new ArrayList<>();
                for (Value grpVal : m.get(2).asArrayValue()) {
                    if (!grpVal.isArrayValue()) continue;
                    ArrayValue grp = grpVal.asArrayValue();
                    if (grp.size() < 5) continue;

                    long grpId = grp.get(0).asIntegerValue().toLong();
                    String nameRu = grp.size() > 1 && !grp.get(1).isNilValue() ? grp.get(1).asStringValue().asString() : "";
                    String nameEn = grp.size() > 2 && !grp.get(2).isNilValue() ? grp.get(2).asStringValue().asString() : "";

                    if (!grp.get(4).isArrayValue()) continue;
                    List<ApuestatotalStakeData> stakes = new ArrayList<>();
                    for (Value stkVal : grp.get(4).asArrayValue()) {
                        if (!stkVal.isArrayValue()) continue;
                        ArrayValue stk = stkVal.asArrayValue();
                        if (stk.size() < 4) continue;

                        long stkId = stk.get(0).asIntegerValue().toLong();
                        String sNameRu = stk.size() > 3 && !stk.get(3).isNilValue() ? stk.get(3).asStringValue().asString() : "";
                        String sNameEn = stk.size() > 5 && !stk.get(5).isNilValue() ? stk.get(5).asStringValue().asString() : "";

                        Double arg = null;
                        if (stk.size() > 6 && !stk.get(6).isNilValue()) {
                            Value argVal = stk.get(6);
                            if (argVal.isFloatValue()) {
                                arg = argVal.asFloatValue().toDouble();
                            } else if (argVal.isIntegerValue()) {
                                arg = (double) argVal.asIntegerValue().toLong();
                            }
                        }

                        Double factor = null;
                        if (stk.size() > 23 && !stk.get(23).isNilValue()) {
                            Value fVal = stk.get(23);
                            if (fVal.isFloatValue()) {
                                factor = fVal.asFloatValue().toDouble();
                            } else if (fVal.isIntegerValue()) {
                                factor = (double) fVal.asIntegerValue().toLong();
                            }
                        }

                        if (factor != null && factor > 1.0) {
                            stakes.add(ApuestatotalStakeData.builder()
                                    .id(stkId)
                                    .nameRu(sNameRu)
                                    .nameEn(sNameEn)
                                    .factor(factor)
                                    .argument(arg)
                                    .build());
                        }
                    }

                    if (!stakes.isEmpty()) {
                        groups.add(ApuestatotalStakeGroupData.builder()
                                .id(grpId)
                                .nameRu(nameRu)
                                .nameEn(nameEn)
                                .stakes(stakes)
                                .build());
                    }
                }

                resultList.add(ApuestatotalMatchOddsData.builder()
                        .matchId(matchId)
                        .groups(groups)
                        .build());
            }

            return resultList;
        } catch (Exception e) {
            log.debug("Error fetching Apuesta Total odds for batch: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
