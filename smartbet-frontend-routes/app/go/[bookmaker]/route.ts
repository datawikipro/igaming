import { NextRequest, NextResponse } from "next/server";

// Fallback catalog in case internal affiliate microservice is unavailable
const DEFAULT_FALLBACK_LINKS: Record<string, string> = {
  winline: "https://winline.ru/signup/?ref=smartbet",
  fonbet: "https://fon.bet/promo/freebet/?ref=smartbet",
  pari: "https://pari.ru/promo/?ref=smartbet",
  betcity: "https://betcity.ru/reg/?ref=smartbet",
  baltbet: "https://baltbet.ru/registration/?ref=smartbet",
  ligastavok: "https://ligastavok.ru/auth/register/?ref=smartbet",
  leon: "https://leon.ru/registration?ref=smartbet",
  olimpbet: "https://olimp.bet/register?ref=smartbet",
  pinnacle: "https://pinnacle.com/signup?ref=smartbet",
  "1xbet": "https://1xbet.com/registration/?ref=smartbet",
  stake: "https://stake.com/?c=smartbet",
  draftkings: "https://draftkings.com/register?ref=smartbet",
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
