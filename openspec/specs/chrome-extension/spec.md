# Chrome Extension Specification

## Purpose
Provides automated bet coupon autofill, odds synchronization, and anti-fingerprint masking on supported bookmaker websites directly from the SmartBet.guru web application.

## Requirements

### Requirement: Cross-Origin Coupon Autofill
The extension must receive postMessage commands from `smartbet.guru` and autofill betting coupons across supported bookmaker portals (Fonbet, Winline, Marathonbet, Betcity).

#### Scenario: User clicks bet in web UI
- **WHEN** the user selects a surebet leg to bet on
- **THEN** the extension opens the bookmaker event tab and fills the bet slip with the calculated stake amount.

---

### Requirement: Anti-Fingerprint Protection
The extension must inject fingerprint masking scripts into supported bookmaker domains before DOM content load.

#### Scenario: Script injection on navigation
- **WHEN** navigating to a supported bookmaker domain
- **THEN** `inject-fingerprint.js` spoofs canvas, navigator, and user agent parameters.
