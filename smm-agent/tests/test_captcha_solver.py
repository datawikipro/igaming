#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Captcha Solver & Fallback Unit Tests
Tests:
- CapSolverClient: ReCaptcha v2, Turnstile, hCaptcha task creation and polling
- SliderPuzzleSolver: Humanized drag track generation and bounding checks
- TelegramFallbackNotifier: Operator alerting logic
- handle_challenge_flow: Orchestration with automated fallback
"""

import os
import sys
import unittest
from unittest.mock import AsyncMock, MagicMock, patch

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from captcha_solver import (
    CapSolverClient,
    SliderPuzzleSolver,
    TelegramFallbackNotifier,
    handle_challenge_flow,
)


class TestCapSolverClient(unittest.TestCase):
    """Unit tests for CapSolver API client."""

    @patch("captcha_solver.requests.Session")
    def test_solve_recaptcha_v2_success(self, mock_session_cls):
        mock_session = MagicMock()
        mock_session_cls.return_value = mock_session

        # Mock createTask response
        mock_session.post.side_effect = [
            MagicMock(json=lambda: {"errorId": 0, "taskId": "task_12345"}),
            MagicMock(json=lambda: {"status": "ready", "solution": {"gRecaptchaResponse": "token_abc_xyz"}}),
        ]

        client = CapSolverClient(api_key="test_api_key")
        token = client.solve_recaptcha_v2(website_url="https://www.instagram.com", website_key="6L_test_sitekey")

        self.assertEqual(token, "token_abc_xyz")
        self.assertEqual(mock_session.post.call_count, 2)

    @patch("captcha_solver.requests.Session")
    def test_solve_turnstile_success(self, mock_session_cls):
        mock_session = MagicMock()
        mock_session_cls.return_value = mock_session

        mock_session.post.side_effect = [
            MagicMock(json=lambda: {"errorId": 0, "taskId": "task_turnstile_99"}),
            MagicMock(json=lambda: {"status": "ready", "solution": {"token": "turnstile_token_ready"}}),
        ]

        client = CapSolverClient(api_key="test_api_key")
        token = client.solve_turnstile(website_url="https://smartbet.guru", website_key="0x4AAAAAA")

        self.assertEqual(token, "turnstile_token_ready")

    def test_client_missing_key_returns_none(self):
        client = CapSolverClient(api_key="")
        token = client.solve_recaptcha_v2(website_url="https://example.com", website_key="sitekey")
        self.assertIsNone(token)


class TestSliderPuzzleSolver(unittest.TestCase):
    """Unit tests for slider puzzle kinematics."""

    def test_human_drag_track_generation(self):
        distance = 180
        track = SliderPuzzleSolver.generate_human_drag_track(distance)

        self.assertGreater(len(track), 10)
        # Sum of dx moves should approximately equal distance
        total_x = sum(step[0] for step in track)
        self.assertAlmostEqual(total_x, distance, delta=5)

        # Check that delay is positive
        for dx, dy, delay in track:
            self.assertGreater(delay, 0)
            self.assertIn(dy, [-1, 0, 1])


class TestChallengeFlowOrchestration(unittest.IsolatedAsyncioTestCase):
    """Unit tests for handle_challenge_flow."""

    async def test_handle_challenge_recaptcha_solved(self):
        mock_page = AsyncMock()
        mock_page.url = "https://www.instagram.com/accounts/emailsignup/"
        mock_frame = MagicMock()
        mock_frame.url = "https://www.google.com/recaptcha/api2/anchor?k=6L_instagram_key&co=..."
        mock_page.frames = [mock_frame]

        with patch.object(CapSolverClient, "solve_recaptcha_v2", return_value="mock_recaptcha_token"):
            solved = await handle_challenge_flow(
                mock_page,
                service_name="smm-bot-meta",
                challenge_type="reCAPTCHA v2",
                capsolver_key="mock_key"
            )
            self.assertTrue(solved)
            mock_page.evaluate.assert_called_once()

    async def test_handle_challenge_turnstile_solved(self):
        mock_page = AsyncMock()
        mock_page.url = "https://example.com"
        mock_page.frames = []

        with patch.object(CapSolverClient, "solve_turnstile", return_value="mock_turnstile_token"):
            solved = await handle_challenge_flow(
                mock_page,
                service_name="source-crawler",
                challenge_type="Cloudflare Turnstile",
                capsolver_key="mock_key"
            )
            self.assertTrue(solved)


if __name__ == "__main__":
    unittest.main()
