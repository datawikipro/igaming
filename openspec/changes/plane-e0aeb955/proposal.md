# Proposal: [freebet-calc] Отдельный калькулятор конвертации фрибетов в 80% гарантированных денег (/tools/freebet-calculator), гайд на сайте и анонс-посты

## Context
Plane Task ID: `e0aeb955-8752-4e20-ac75-6ddda1fc8d9c`

## Description
Реализация интерактивного калькулятора конвертации фрибетов (Matched Betting / SNR Calculator) на роуте `/[locale]/tools/freebet-calculator`, обучающего гайда в блоге, промо-баннеров и генератора анонс-постов для соцсетей.

### Математическая модель (SNR — Stake Not Returned):
- $S_2 = F \cdot (K_1 - 1) / K_2$ (сумма хедж-ставки)
- $\text{Profit} = F \cdot (K_1 - 1) \cdot (K_2 - 1) / K_2$
- Коэффициент конвертации $\eta = \frac{(K_1 - 1)(K_2 - 1)}{K_2} \times 100\%$ (при $K_1 = 5.0$, $K_2 = 1.25$ дает гарантированные $80\%$ живых денег).

### Архитектура изменений:
1. `src/app/[locale]/tools/freebet-calculator/page.tsx` & `src/components/freebet-calculator/FreebetCalculator.tsx` — страница и интерактивный калькулятор с пресетами, deep-link URL query params, и таблицей актуальных акций БК с пересчетом в $80\%$ гарантированного кэша.
2. `src/app/[locale]/go/[bookmaker]/route.ts` — редирект для партнерских ссылок БК.
3. Промо-баннеры и навигация: `HeaderNav.tsx`, `HeaderMobileDrawer.tsx`, `HomeClient.tsx`, `SurebetCalculator.tsx`.
4. SEO-статья в блоге: `src/content/blog/how-to-convert-freebets-guaranteed-cash.mdx`.
5. Локализация: `messages/ru.json` и `messages/en.json`.
6. Генератор анонс-постов: `scripts/generate_freebet_announcement_posts.py`.
