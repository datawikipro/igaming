import { NextRequest, NextResponse } from "next/server";

// Fallback catalog in case internal affiliate microservice is unavailable
const DEFAULT_FALLBACK_LINKS: Record<string, string> = {
  // Russian Licensed (RUB)
  winline: "https://winline.ru/signup/?ref=smartbet",
  "winline-ru": "https://winline.ru/signup/?ref=smartbet",
  fonbet: "https://fon.bet/promo/freebet/?ref=smartbet",
  "fon-bet": "https://fon.bet/promo/freebet/?ref=smartbet",
  "fon-bet-ru": "https://fon.bet/promo/freebet/?ref=smartbet",
  pari: "https://pari.ru/promo/?ref=smartbet",
  "pari-ru": "https://pari.ru/promo/?ref=smartbet",
  betcity: "https://betcity.ru/reg/?ref=smartbet",
  "betcity-ru": "https://betcity.ru/reg/?ref=smartbet",
  baltbet: "https://baltbet.ru/registration/?ref=smartbet",
  "baltbet-ru": "https://baltbet.ru/registration/?ref=smartbet",
  ligastavok: "https://ligastavok.ru/auth/register/?ref=smartbet",
  "ligastavok-ru": "https://ligastavok.ru/auth/register/?ref=smartbet",
  liga: "https://ligastavok.ru/auth/register/?ref=smartbet",
  betboom: "https://betboom.ru/auth/signup?ref=smartbet",
  "betboom-ru": "https://betboom.ru/auth/signup?ref=smartbet",
  leon: "https://leon.ru/registration?ref=smartbet",
  "leon-ru": "https://leon.ru/registration?ref=smartbet",
  olimpbet: "https://www.olimp.bet/register?ref=smartbet",
  "olimpbet-ru": "https://www.olimp.bet/register?ref=smartbet",
  olimp: "https://www.olimp.bet/register?ref=smartbet",
  tennisi: "https://tennisi.bet/?ref=smartbet",
  "tennisi-bet": "https://tennisi.bet/?ref=smartbet",
  "tennisi-ru": "https://tennisi.bet/?ref=smartbet",
  zenit: "https://zenit.win/?ref=smartbet",
  "zenit-win": "https://zenit.win/?ref=smartbet",
  "zenit-ru": "https://zenit.win/?ref=smartbet",
  marathonbet: "https://www.marathonbet.ru/su/?ref=smartbet",
  "marathonbet-ru": "https://www.marathonbet.ru/su/?ref=smartbet",
  marathon: "https://www.marathonbet.ru/su/?ref=smartbet",
  melbet: "https://melbet.ru/registration/?ref=smartbet",
  "melbet-ru": "https://melbet.ru/registration/?ref=smartbet",
  "melbet-com": "https://melbet.com/registration/?ref=smartbet",
  "1xbet": "https://melbet.ru/registration/?ref=smartbet",
  "1x": "https://melbet.ru/registration/?ref=smartbet",
  "1xbet-ru": "https://melbet.ru/registration/?ref=smartbet",

  // Belarus Licensed (BYN)
  "winline-by": "https://winline.by/signup/?ref=smartbet",
  "winline.by": "https://winline.by/signup/?ref=smartbet",
  "fonbet-by": "https://fonbet.by/registration/?ref=smartbet",
  "fonbet.by": "https://fonbet.by/registration/?ref=smartbet",
  "betcity-by": "https://betcity.by/reg/?ref=smartbet",
  "betcity.by": "https://betcity.by/reg/?ref=smartbet",
  "marathonbet-by": "https://marathonbet.by/su/join.htm?ref=smartbet",
  "marathonbet.by": "https://marathonbet.by/su/join.htm?ref=smartbet",
  maxline: "https://maxline.by/registration?ref=smartbet",
  "maxline.by": "https://maxline.by/registration?ref=smartbet",
  bravo: "https://bravobet.by/register?ref=smartbet",
  bravobet: "https://bravobet.by/register?ref=smartbet",

  // Kazakhstan Licensed (KZT)
  "olimpbet-kz": "https://olimpbet.kz/register?ref=smartbet",
  "olimpbet.kz": "https://olimpbet.kz/register?ref=smartbet",
  "fonbet-kz": "https://fonbet.kz/registration/?ref=smartbet",
  "fonbet.kz": "https://fonbet.kz/registration/?ref=smartbet",
  "1xbet-kz": "https://1xbet.kz/registration/?ref=smartbet",
  "1xbet.kz": "https://1xbet.kz/registration/?ref=smartbet",
  "tennisi-kz": "https://tennisi.kz/auth/register?ref=smartbet",
  "tennisi.kz": "https://tennisi.kz/auth/register?ref=smartbet",

  // BetB2B Family / Global
  "1xbet-com": "https://1xbet.com/registration/?ref=smartbet",
  "888starz": "https://888starz.bet/?ref=smartbet",
  "888starz-bet": "https://888starz.bet/?ref=smartbet",
  fansport: "https://fan-sport.com/?ref=smartbet",
  "fan-sport": "https://fan-sport.com/?ref=smartbet",
  "22bet": "https://22bet.com/?ref=smartbet",
  "1xbit": "https://1xbit.com/?ref=smartbet",
  megapari: "https://megapari.com/?ref=smartbet",
  linebet: "https://linebet.com/?ref=smartbet",
  betandyou: "https://betandyou.com/?ref=smartbet",
  betwinner: "https://betwinner.com/?ref=smartbet",
  paripesa: "https://paripesa.com/?ref=smartbet",
  spinbetter: "https://spinbetter.com/?ref=smartbet",
  betlabel: "https://betlabel.com/?ref=smartbet",

  // International Tier-1
  pinnacle: "https://pinnacle.com/signup?ref=smartbet",
  stake: "https://stake.com/?c=smartbet",
  bet365: "https://bet365.com/?affiliate=smartbet",
  unibet: "https://unibet.com/register?ref=smartbet",
  betway: "https://betway.com/en/sports?ref=smartbet",
  "888sport": "https://888sport.com/join?ref=smartbet",
  betano: "https://betano.com/register?ref=smartbet",
  stoiximan: "https://stoiximan.gr/register?ref=smartbet",
  bwin: "https://sports.bwin.com/en/sports?wm=smartbet",
  betfair: "https://betfair.com/join?ref=smartbet",
  smarkets: "https://smarkets.com/join?ref=smartbet",
  betsson: "https://betsson.com/join?ref=smartbet",
  betvictor: "https://betvictor.com/en-gb/sports?ref=smartbet",
  bovada: "https://bovada.lv/join?ref=smartbet",

  // Americas & LatAm
  draftkings: "https://sportsbook.draftkings.com/?ref=smartbet",
  fanduel: "https://sportsbook.fanduel.com/?ref=smartbet",
  betmgm: "https://sports.betmgm.com/?ref=smartbet",
  caesars: "https://www.caesars.com/sportsbook-and-casino?ref=smartbet",
  betrivers: "https://betrivers.com/?page=sportsbook&ref=smartbet",
  apuestatotal: "https://www.apuestatotal.com/?ref=smartbet",
  bet7k: "https://bet7k.com/?ref=smartbet",
  caliente: "https://www.caliente.mx/?ref=smartbet",
  wplay: "https://www.wplay.co/?ref=smartbet",
};

const AFFILIATE_SERVICE_URL =
  process.env.AFFILIATE_SERVICE_URL ||
  "http://igaming-affiliate-service.igaming-dev.svc.cluster.local:8080";

export async function GET(
  request: NextRequest,
  { params }: { params: { bookmaker: string } }
) {
  const bookmaker = params.bookmaker.toLowerCase();
  const searchParams = request.nextUrl.searchParams;

  // 1. Try delegating to internal affiliate microservice for full click tracking
  try {
    const upstreamUrl = new URL(`${AFFILIATE_SERVICE_URL}/go/${bookmaker}`);
    searchParams.forEach((val, key) => upstreamUrl.searchParams.set(key, val));

    const clientIp =
      request.headers.get("cf-connecting-ip") ||
      request.headers.get("x-forwarded-for")?.split(",")[0].trim() ||
      "127.0.0.1";
    const userAgent = request.headers.get("user-agent") || "";
    const referer = request.headers.get("referer") || "";

    const response = await fetch(upstreamUrl.toString(), {
      method: "GET",
      redirect: "manual",
      headers: {
        "X-Forwarded-For": clientIp,
        "User-Agent": userAgent,
        Referer: referer,
      },
    });

    const location = response.headers.get("location");
    if (response.status === 302 && location) {
      return NextResponse.redirect(new URL(location), { status: 302 });
    }
  } catch (err) {
    console.warn(`[Affiliate Next.js Gateway] Upstream error:`, err);
  }

  // 2. Direct Fallback if microservice is offline or slow
  const baseFallback =
    DEFAULT_FALLBACK_LINKS[bookmaker] || `https://${bookmaker}.com/?ref=smartbet`;
  const fallbackUrl = new URL(baseFallback);
  searchParams.forEach((val, key) => fallbackUrl.searchParams.set(key, val));

  return NextResponse.redirect(fallbackUrl, {
    status: 302,
    headers: {
      "Cache-Control": "no-cache, no-store, must-revalidate",
    },
  });
}
