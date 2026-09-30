#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Unit and Integration Tests for Video Shorts Pipeline
Task #59 [video-shorts] (b1cbfcef-863a-4be2-b550-2c87ece37263)
"""

import json
from pathlib import Path
import sys
import unittest
import urllib.request

# Add smm-agent to path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from video_shorts_pipeline import (
    MANDATORY_DISCLAIMER,
    CloudRenderJob,
    DIDCloudProvider,
    HeyGenCloudProvider,
    MockCloudVideoProvider,
    ShortsAutoposter,
    ShortsScript,
    ShortsScriptGenerator,
    SurebetInfo,
    VideoShortsPipeline,
)


class TestVideoShortsPipeline(unittest.TestCase):

    def setUp(self):
        self.sample_surebet = SurebetInfo(
            sport="Футбол",
            event_name="Ливерпуль — Реал Мадрид",
            event_date="2026-10-15 22:00",
            bookmaker_a="Winline",
            market_a="П1 (Победа Ливерпуля)",
            odds_a=2.25,
            bookmaker_b="Fonbet",
            market_b="Х2 (Ничья или Реал)",
            odds_b=2.08,
            profit_percent=8.16,
        )
        self.generator = ShortsScriptGenerator()
        self.autoposter = ShortsAutoposter()

    def test_arbitrage_math_calculation(self):
        """Verify exact stake and profit math."""
        math_res = self.generator.calculate_stakes(odds_a=2.25, odds_b=2.08, bankroll=10000.0)
        self.assertTrue(math_res["is_surebet"])
        self.assertAlmostEqual(math_res["profit_percent"], 8.08, places=1)
        self.assertGreater(math_res["net_profit"], 800.0)
        self.assertEqual(round(math_res["stake_a"] + math_res["stake_b"]), 10000.0)

    def test_script_generation_content_and_disclaimer(self):
        """Verify hook, 9:16 ratio, script components, and mandatory disclaimer."""
        script = self.generator.generate_script(self.sample_surebet, bankroll=10000.0)

        # 9:16 aspect ratio
        self.assertEqual(script.aspect_ratio, "9:16")

        # Viral hook present
        self.assertTrue(len(script.hook_text) > 10)
        self.assertTrue(any(word in script.hook_text for word in ["Шок", "Букмекеры", "Вилка", "студенты", "кэфов", "без риска"]))

        # Mandatory disclaimer present (Golden Rule #10 / social-media-bot spec)
        self.assertIn(MANDATORY_DISCLAIMER, script.full_speech_script)
        self.assertIn(MANDATORY_DISCLAIMER, script.description)

        # Duration within short format (< 60s)
        self.assertGreaterEqual(script.estimated_duration_sec, 20)
        self.assertLessEqual(script.estimated_duration_sec, 60)

        # Visual cues for video rendering
        self.assertTrue(len(script.visual_cues) >= 4)
        cue_types = [c["type"] for c in script.visual_cues]
        self.assertIn("hook_banner", cue_types)
        self.assertIn("odds_comparison", cue_types)

        # Affiliate links included
        self.assertIn("Winline", script.affiliate_links)
        self.assertIn("Fonbet", script.affiliate_links)
        self.assertTrue(script.affiliate_links["Winline"].startswith("https://smartbet.guru/go/winline?"))

    def test_cloud_video_render_mock_provider(self):
        """
        Verify Cloud Video Provider (zero CPU Xeon rendering constraint).
        Fulfills DoD: generates finished video MP4 URL externally without local GPU load.
        """
        script = self.generator.generate_script(self.sample_surebet)
        provider = MockCloudVideoProvider(public_base_url="http://smm-video-shorts.igaming-dev.svc.cluster.local:8080")
        job = provider.generate_video(script, webhook_url="http://localhost:8080/api/v1/shorts/webhook")

        self.assertEqual(job.status, "COMPLETED")
        self.assertEqual(job.provider, "mock_cloud_ai")
        self.assertTrue(job.video_url.endswith(".mp4"))
        self.assertTrue(job.video_url.startswith("http://smm-video-shorts.igaming-dev.svc.cluster.local:8080/video/"))

        # Verify job status inquiry
        status_job = provider.get_job_status(job.job_id)
        self.assertEqual(status_job.job_id, job.job_id)
        self.assertEqual(status_job.status, "COMPLETED")

    def test_multi_platform_autopost_bundle(self):
        """Verify bundle packages for YouTube Shorts, Instagram Reels, and TikTok."""
        script = self.generator.generate_script(self.sample_surebet)
        provider = MockCloudVideoProvider()
        job = provider.generate_video(script, webhook_url="http://localhost:8080/webhook")

        bundle = self.autoposter.prepare_publication_bundle(script, job)
        self.assertIn("platforms", bundle)

        # YouTube Shorts verification
        yt = bundle["platforms"]["youtube"]
        self.assertEqual(yt["platform"], "youtube_shorts")
        self.assertLessEqual(len(yt["title"]), 100)
        self.assertIn("Ливерпуль", yt["description"])
        self.assertIn("shorts", yt["tags"])

        # Instagram Reels verification
        ig = bundle["platforms"]["instagram"]
        self.assertEqual(ig["platform"], "instagram_reels")
        self.assertIn("Ливерпуль", ig["caption"])
        self.assertIn(MANDATORY_DISCLAIMER, ig["caption"])

        # TikTok verification
        tt = bundle["platforms"]["tiktok"]
        self.assertEqual(tt["platform"], "tiktok")
        self.assertIn("#shorts", tt["text"])

    def test_pipeline_full_cycle_and_http_server(self):
        """Verify full pipeline execution and HTTP endpoints (/healthz, /api/v1/shorts/generate)."""
        test_port = 18099
        pipeline = VideoShortsPipeline(port=test_port, provider_name="mock")
        try:
            pipeline.start_http_server(blocking=False)

            # Test /healthz
            req = urllib.request.Request(f"http://127.0.0.1:{test_port}/healthz")
            with urllib.request.urlopen(req, timeout=3.0) as resp:
                self.assertEqual(resp.status, 200)
                body = json.loads(resp.read().decode())
                self.assertEqual(body["status"], "UP")
                self.assertEqual(body["service"], "smm-video-shorts")
                self.assertEqual(body["xeon_cpu_load"], "0%")

            # Test /api/v1/shorts/generate
            cycle_payload = json.dumps({"bankroll": 15000.0}).encode("utf-8")
            post_req = urllib.request.Request(
                f"http://127.0.0.1:{test_port}/api/v1/shorts/generate",
                data=cycle_payload,
                headers={"Content-Type": "application/json"},
                method="POST"
            )
            with urllib.request.urlopen(post_req, timeout=5.0) as resp:
                self.assertEqual(resp.status, 200)
                res_body = json.loads(resp.read().decode())
                self.assertEqual(res_body["status"], "COMPLETED")
                self.assertTrue(res_body["video_url"].endswith(".mp4"))

            # Test /api/v1/shorts/latest
            latest_req = urllib.request.Request(f"http://127.0.0.1:{test_port}/api/v1/shorts/latest")
            with urllib.request.urlopen(latest_req, timeout=3.0) as resp:
                self.assertEqual(resp.status, 200)
                latest_body = json.loads(resp.read().decode())
                self.assertEqual(latest_body["status"], "success")
                self.assertIsNotNone(latest_body["job"])

            # Test sample mp4 download
            mp4_req = urllib.request.Request(f"http://127.0.0.1:{test_port}/video/test.mp4")
            with urllib.request.urlopen(mp4_req, timeout=3.0) as resp:
                self.assertEqual(resp.status, 200)
                self.assertEqual(resp.headers.get("Content-Type"), "video/mp4")
                video_bytes = resp.read()
                self.assertTrue(video_bytes.startswith(b"ftypmp42"))

        finally:
            pipeline.stop_http_server()


if __name__ == "__main__":
    unittest.main()
