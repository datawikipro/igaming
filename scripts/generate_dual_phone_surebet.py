#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Dual Phone Mobile App Screenshot Generator for Surebets
Creates a side-by-side mockup of 2 mobile apps (e.g. Betcity vs Marathonbet)
displaying live surebet coupons from the database.
"""

import os
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

def get_font(size: int, bold: bool = False):
    font_names = [
        "C:\\Windows\\Fonts\\segoeuib.ttf" if bold else "C:\\Windows\\Fonts\\segoeui.ttf",
        "C:\\Windows\\Fonts\\arialbd.ttf" if bold else "C:\\Windows\\Fonts\\arial.ttf",
    ]
    for fn in font_names:
        if os.path.exists(fn):
            try:
                return ImageFont.truetype(fn, size)
            except Exception:
                pass
    return ImageFont.load_default()

def draw_rounded_rect(draw, bbox, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(bbox, radius=radius, fill=fill, outline=outline, width=width)

def draw_phone_mockup(draw, x, y, width, height, bookmaker_name, primary_color, match_title, market_name, odds, stake, payout, is_left=True):
    # Outer phone shadow / bezel
    bezel_radius = 36
    draw_rounded_rect(draw, (x - 6, y - 6, x + width + 6, y + height + 6), radius=bezel_radius + 4, fill=(18, 22, 32, 255))
    draw_rounded_rect(draw, (x, y, x + width, y + height), radius=bezel_radius, fill=(15, 18, 26, 255), outline=(50, 60, 80, 255), width=3)
    
    # Phone screen inner area
    screen_margin = 12
    sx = x + screen_margin
    sy = y + screen_margin
    sw = width - screen_margin * 2
    sh = height - screen_margin * 2
    draw_rounded_rect(draw, (sx, sy, sx + sw, sy + sh), radius=bezel_radius - 8, fill=(22, 27, 38, 255))
    
    # Notch / Camera island
    notch_w = 110
    notch_h = 24
    notch_x = x + (width - notch_w) // 2
    draw_rounded_rect(draw, (notch_x, sy + 6, notch_x + notch_w, sy + 6 + notch_h), radius=12, fill=(10, 12, 18, 255))
    
    # Status bar (Time & Icons)
    font_status = get_font(14, bold=True)
    draw.text((sx + 24, sy + 10), "15:02", fill=(200, 210, 225, 255), font=font_status)
    draw.text((sx + sw - 60, sy + 10), "5G  89%", fill=(200, 210, 225, 255), font=font_status)
    
    # App Header Bar (Bookmaker brand color)
    app_bar_y = sy + 40
    app_bar_h = 56
    draw.rectangle([(sx, app_bar_y), (sx + sw, app_bar_y + app_bar_h)], fill=primary_color)
    
    font_app_title = get_font(20, bold=True)
    draw.text((sx + 20, app_bar_y + 14), f"📱 {bookmaker_name.upper()}", fill=(255, 255, 255, 255), font=font_app_title)
    
    # Balance badge on top right
    draw_rounded_rect(draw, (sx + sw - 120, app_bar_y + 10, sx + sw - 16, app_bar_y + 44), radius=16, fill=(0, 0, 0, 120))
    font_bal = get_font(13, bold=True)
    draw.text((sx + sw - 108, app_bar_y + 18), "25 000 ₽", fill=(255, 215, 0, 255), font=font_bal)
    
    # Match card container
    match_card_y = app_bar_y + app_bar_h + 16
    draw_rounded_rect(draw, (sx + 14, match_card_y, sx + sw - 14, match_card_y + 130), radius=14, fill=(30, 37, 52, 255))
    
    # Live / Pre-match badge
    draw_rounded_rect(draw, (sx + 26, match_card_y + 14, sx + 90, match_card_y + 36), radius=6, fill=(0, 200, 83, 255))
    font_badge = get_font(11, bold=True)
    draw.text((sx + 34, match_card_y + 17), "LIVE 58'", fill=(255, 255, 255, 255), font=font_badge)
    
    font_tourn = get_font(12, bold=False)
    draw.text((sx + 102, match_card_y + 18), "Италия. Серия B / Кубок", fill=(160, 175, 195, 255), font=font_tourn)
    
    font_match = get_font(17, bold=True)
    draw.text((sx + 26, match_card_y + 48), match_title, fill=(255, 255, 255, 255), font=font_match)
    
    font_score = get_font(14, bold=True)
    draw.text((sx + 26, match_card_y + 82), "Текущий счет: 2 : 2 (4 гола)", fill=(255, 215, 0, 255), font=font_score)
    
    # Betslip / Coupon container (Bottom Half of phone)
    coupon_y = match_card_y + 150
    coupon_h = 280
    draw_rounded_rect(draw, (sx + 14, coupon_y, sx + sw - 14, coupon_y + coupon_h), radius=16, fill=(26, 32, 46, 255), outline=(60, 75, 105, 255), width=2)
    
    # Betslip title bar
    draw_rounded_rect(draw, (sx + 14, coupon_y, sx + sw - 14, coupon_y + 44), radius=14, fill=(36, 45, 65, 255))
    font_coupon_header = get_font(15, bold=True)
    draw.text((sx + 28, coupon_y + 12), "КУПОН СТАВКИ (ОРДИНАР)", fill=(240, 245, 255, 255), font=font_coupon_header)
    
    # Market & Outcome
    font_lbl = get_font(13, bold=False)
    draw.text((sx + 28, coupon_y + 58), "Выбранный исход:", fill=(150, 165, 185, 255), font=font_lbl)
    
    font_mkt = get_font(18, bold=True)
    draw.text((sx + 28, coupon_y + 80), market_name, fill=(255, 255, 255, 255), font=font_mkt)
    
    # Odds pill button
    odds_box_x = sx + sw - 110
    draw_rounded_rect(draw, (odds_box_x, coupon_y + 70, sx + sw - 28, coupon_y + 115), radius=10, fill=primary_color)
    font_odds_lbl = get_font(11, bold=False)
    draw.text((odds_box_x + 12, coupon_y + 74), "КЭФ", fill=(240, 240, 240, 255), font=font_odds_lbl)
    font_odds_val = get_font(20, bold=True)
    draw.text((odds_box_x + 12, coupon_y + 88), f"{odds:.2f}", fill=(255, 255, 255, 255), font=font_odds_val)
    
    # Stake input line
    draw.rectangle([(sx + 28, coupon_y + 130), (sx + sw - 28, coupon_y + 132)], fill=(45, 55, 78, 255))
    draw.text((sx + 28, coupon_y + 145), "Сумма ставки:", fill=(150, 165, 185, 255), font=font_lbl)
    
    font_stake = get_font(20, bold=True)
    draw.text((sx + 28, coupon_y + 168), f"{stake:,.0f} ₽".replace(",", " "), fill=(0, 230, 118, 255), font=font_stake)
    
    # Payout
    draw.text((sx + sw - 180, coupon_y + 145), "Возможный выигрыш:", fill=(150, 165, 185, 255), font=font_lbl)
    font_win = get_font(20, bold=True)
    draw.text((sx + sw - 180, coupon_y + 168), f"{payout:,.0f} ₽".replace(",", " "), fill=(255, 215, 0, 255), font=font_win)
    
    # Green action button "Ставка принята"
    btn_y = coupon_y + 215
    draw_rounded_rect(draw, (sx + 28, btn_y, sx + sw - 28, btn_y + 48), radius=12, fill=(0, 175, 80, 255))
    font_btn = get_font(15, bold=True)
    draw.text((sx + 90, btn_y + 14), "✓ СТАВКА УСПЕШНО ПРИНЯТА", fill=(255, 255, 255, 255), font=font_btn)
    
    # Bottom Navigation bar (Phone UI)
    nav_y = sy + sh - 45
    draw_rounded_rect(draw, (sx, nav_y, sx + sw, sy + sh), radius=12, fill=(18, 22, 32, 255))
    font_nav = get_font(12, bold=True)
    draw.text((sx + 35, nav_y + 12), "Линия", fill=(140, 150, 170, 255), font=font_nav)
    draw.text((sx + sw // 2 - 25, nav_y + 12), "Купон (1)", fill=primary_color, font=font_nav)
    draw.text((sx + sw - 85, nav_y + 12), "Профиль", fill=(140, 150, 170, 255), font=font_nav)


def generate_dual_phone_surebet_image(output_path: str):
    canvas_w = 1440
    canvas_h = 960
    
    img = Image.new("RGBA", (canvas_w, canvas_h), (11, 14, 21, 255))
    draw = ImageDraw.Draw(img)
    
    # Top Banner Header
    draw.rectangle([(0, 0), (canvas_w, 85)], fill=(18, 24, 36, 255))
    font_hdr = get_font(26, bold=True)
    draw.text((50, 24), "⚡ SmartBet.guru | АРБИТРАЖНЫЙ СКАНЕР КОТИРОВОК", fill=(255, 255, 255, 255), font=font_hdr)
    
    font_live = get_font(15, bold=True)
    draw_rounded_rect(draw, (canvas_w - 220, 22, canvas_w - 50, 62), radius=10, fill=(227, 6, 19, 255))
    draw.text((canvas_w - 200, 31), "🔴 LIVE СИГНАЛ", fill=(255, 255, 255, 255), font=font_live)
    
    # Center Arbitrage Profit Badge
    profit_badge_y = 105
    draw_rounded_rect(draw, (canvas_w // 2 - 260, profit_badge_y, canvas_w // 2 + 260, profit_badge_y + 64), radius=16, fill=(16, 44, 30, 255), outline=(0, 230, 118, 255), width=2)
    font_profit = get_font(24, bold=True)
    draw.text((canvas_w // 2 - 235, profit_badge_y + 16), "⚖️ ДОХОДНОСТЬ ВИЛКИ: +11.78%", fill=(0, 230, 118, 255), font=font_profit)
    
    # Phone mockups side-by-side
    phone_w = 440
    phone_h = 660
    phone_y = 195
    
    gap = 140
    left_phone_x = (canvas_w - (phone_w * 2 + gap)) // 2
    right_phone_x = left_phone_x + phone_w + gap
    
    # Left Phone: БЕТСИТИ
    # Market: Тотал 4.5 Больше @ 2.14, Stake 5 295 RUB -> Payout 11 331 RUB
    draw_phone_mockup(
        draw=draw,
        x=left_phone_x,
        y=phone_y,
        width=phone_w,
        height=phone_h,
        bookmaker_name="БЕТСИТИ",
        primary_color=(24, 118, 209, 255), # Betcity Blue
        match_title="Юве Стабия vs Пальмезе",
        market_name="Тотал Больше (4.5)",
        odds=2.14,
        stake=5295.0,
        payout=11331.0,
        is_left=True
    )
    
    # Right Phone: МАРАФОНБЕТ
    # Market: Тотал 4.5 Меньше @ 2.41, Stake 4 705 RUB -> Payout 11 339 RUB
    draw_phone_mockup(
        draw=draw,
        x=right_phone_x,
        y=phone_y,
        width=phone_w,
        height=phone_h,
        bookmaker_name="МАРАФОНБЕТ",
        primary_color=(15, 76, 129, 255), # Marathon Navy
        match_title="Юве Стабия vs Пальмезе",
        market_name="Тотал Меньше (4.5)",
        odds=2.41,
        stake=4705.0,
        payout=11339.0,
        is_left=False
    )
    
    # Center Connector Box between the two phones
    center_x = canvas_w // 2
    center_box_y = phone_y + 220
    draw_rounded_rect(draw, (center_x - 55, center_box_y, center_x + 55, center_box_y + 110), radius=14, fill=(24, 32, 48, 255), outline=(0, 230, 118, 255), width=2)
    font_vs = get_font(13, bold=True)
    draw.text((center_x - 38, center_box_y + 12), "ВЗАИМНОЕ", fill=(180, 195, 215, 255), font=font_vs)
    draw.text((center_x - 48, center_box_y + 30), "ПЕРЕКРЫТИЕ", fill=(180, 195, 215, 255), font=font_vs)
    font_plus = get_font(26, bold=True)
    draw.text((center_x - 46, center_box_y + 58), "+1 335 ₽", fill=(255, 215, 0, 255), font=font_plus)
    
    # Footer disclaimer bar
    footer_y = canvas_h - 65
    draw.rectangle([(0, footer_y), (canvas_w, canvas_h)], fill=(12, 15, 22, 255))
    font_disc = get_font(13, bold=False)
    disclaimer_text = "Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно. 18+"
    draw.text((50, footer_y + 14), disclaimer_text, fill=(120, 130, 145, 255), font=font_disc)
    
    font_url = get_font(14, bold=True)
    draw.text((canvas_w - 320, footer_y + 14), "Сканер линий: smartbet.guru", fill=(66, 165, 245, 255), font=font_url)
    
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"Generated dual phone mockup at: {output_path}")

if __name__ == "__main__":
    out = "c:\\Users\\chernousov_a\\IdeaProjects\\igaming\\smm-agent\\scratch\\dual_phone_surebet.png"
    generate_dual_phone_surebet_image(out)
