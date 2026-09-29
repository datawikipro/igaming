#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — SMM & Stealth Browser Agents
Cache Warmup Module: Natural Pre-Browsing Routine (Rule 9 & Task #54)

Features:
1. Simulates realistic organic pre-surfing across neutral sports & news resources.
2. Accumulates HTTP cache, CDN assets, cookies, and local browsing history.
3. Natural kinematics: smooth Bezier curve mouse movements, pauses, reading delays, scrolling.
4. Prevents immediate anti-fraud flagging when targeting Threads, Instagram, Reddit, etc.
"""

import asyncio
import logging
import random
import time
from typing import Any, Dict, List, Optional

logger = logging.getLogger("CacheWarmup")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")

# Neutral sports & high-reputation portals for natural profile warmup
DEFAULT_WARMUP_SITES = [
    "https://www.bbc.com/sport",
    "https://www.espn.com",
    "https://www.flashscore.com",
    "https://news.ycombinator.com",
    "https://en.wikipedia.org/wiki/Portal:Current_events",
    "https://en.wikipedia.org/wiki/Premier_League",
    "https://en.wikipedia.org/wiki/UEFA_Champions_League",
]


class CacheWarmupManager:
    """
    Manages browser cache warmup through natural human-like browsing sessions.
    """

    def __init__(self, sites: Optional[List[str]] = None):
        self.sites = sites or list(DEFAULT_WARMUP_SITES)

    async def _safe_scroll(self, page: Any, human: Optional[Any], total_ticks: int = 4) -> None:
        """Performs natural intermittent scrolling down and slight up-scroll."""
        for _ in range(total_ticks):
            scroll_delta = random.randint(250, 500)
            if human and hasattr(human, "scroll"):
                await human.scroll(delta_y=scroll_delta, steps=4)
            else:
                await page.mouse.wheel(0, scroll_delta)
            # Human reading pause between scrolls
            await asyncio.sleep(random.uniform(1.2, 3.0))

        # Occasional slight scroll back up (re-reading)
        if random.random() < 0.4:
            up_delta = -random.randint(100, 250)
            if human and hasattr(human, "scroll"):
                await human.scroll(delta_y=up_delta, steps=3)
            else:
                await page.mouse.wheel(0, up_delta)
            await asyncio.sleep(random.uniform(1.0, 2.0))

    async def _safe_mouse_roam(self, page: Any, human: Optional[Any]) -> None:
        """Moves mouse naturally around elements and headings."""
        try:
            viewport = page.viewport_size or {"width": 1280, "height": 800}
            max_w = viewport["width"]
            max_h = viewport["height"]

            for _ in range(random.randint(2, 4)):
                target_x = random.uniform(100, max_w - 150)
                target_y = random.uniform(100, max_h - 150)
                if human and hasattr(human, "mouse_move"):
                    await human.mouse_move(target_x, target_y, steps=random.randint(15, 25))
                else:
                    await page.mouse.move(target_x, target_y)
                await asyncio.sleep(random.uniform(0.3, 1.2))
        except Exception as e:
            logger.debug(f"Mouse roaming ignored exception: {e}")

    async def warmup(
        self,
        page: Any,
        human: Optional[Any] = None,
        duration_seconds: int = 120,
        max_sites: int = 3,
    ) -> Dict[str, Any]:
        """
        Executes an organic warmup cycle for `duration_seconds`.
        Visits up to `max_sites` and interacts like a real sports enthusiast.
        """
        start_time = time.time()
        visited: List[str] = []
        selected_sites = random.sample(self.sites, min(max_sites, len(self.sites)))

        logger.info(f"Starting browser cache warmup: target duration {duration_seconds}s across {len(selected_sites)} sites")

        for idx, site in enumerate(selected_sites):
            elapsed = time.time() - start_time
            if elapsed >= duration_seconds:
                logger.info(f"Target warmup duration of {duration_seconds}s reached.")
                break

            remaining = duration_seconds - elapsed
            # Allocate proportional time for this site
            site_budget = min(remaining, random.uniform(30.0, 55.0))

            logger.info(f"Warmup [{idx+1}/{len(selected_sites)}]: visiting {site} (~{site_budget:.1f}s)")

            try:
                # 1. Navigate with realistic domcontentloaded wait
                await page.goto(site, wait_until="domcontentloaded", timeout=25000)
                visited.append(site)

                # 2. Initial dwell time (user looking at above-the-fold content)
                await asyncio.sleep(random.uniform(2.5, 5.0))

                # 3. Roam mouse cursor across navigation bar / headlines
                await self._safe_mouse_roam(page, human)

                # 4. Scroll through content
                await self._safe_scroll(page, human, total_ticks=random.randint(3, 5))

                # 5. Organic click on internal link if available
                article_links = await page.query_selector_all("article a, main a, h2 a, h3 a")
                if article_links and random.random() < 0.7:
                    valid_links = []
                    for link in article_links[:10]:
                        href = await link.get_attribute("href")
                        if href and not href.startswith(("#", "javascript:", "mailto:")):
                            valid_links.append(link)

                    if valid_links:
                        chosen_link = random.choice(valid_links[:5])
                        logger.info("Warmup: clicking internal article link to deepen navigation history...")
                        try:
                            box = await chosen_link.bounding_box()
                            if box and box["y"] < 800:
                                if human and hasattr(human, "click"):
                                    await human.click(coords=(
                                        box["x"] + box["width"] * 0.5,
                                        box["y"] + box["height"] * 0.5,
                                    ))
                                else:
                                    await chosen_link.click(timeout=5000)

                                # Wait for article page and spend time reading
                                await page.wait_for_load_state("domcontentloaded", timeout=15000)
                                article_dwell = random.uniform(5.0, 12.0)
                                await asyncio.sleep(article_dwell)
                                await self._safe_scroll(page, human, total_ticks=2)
                        except Exception as e:
                            logger.debug(f"Article navigation sub-step skipped: {e}")

            except Exception as e:
                logger.warning(f"Error during warmup on {site}: {e}")
                # Continue with next site gracefully
                await asyncio.sleep(2.0)

        total_elapsed = time.time() - start_time
        logger.info(f"Warmup completed successfully in {total_elapsed:.1f}s. Visited: {len(visited)} sites.")
        return {
            "status": "success",
            "duration_seconds": round(total_elapsed, 1),
            "sites_visited": visited,
        }
