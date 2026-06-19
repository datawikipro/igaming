-- Seed logo_emoji, ratings, bonuses, promo codes, summaries and descriptions for bookmakers in bet_source table.

-- 1. Fonbet
UPDATE bet_source SET
    logo_emoji = '🔴',
    rating = 4.9,
    rating_reliability = 5.0,
    rating_coefficients = 4.7,
    referral_url = 'https://fon.bet',
    promo_code = 'FONSMART',
    bonus_ru = 'Фрибет до 15 000 ₽ новым игрокам',
    bonus_en = 'Up to 15,000 ₽ Welcome Freebet',
    summary_ru = 'Фонбет — старейший и самый надежный букмекер в СНГ с мгновенными выплатами, огромной Live-линией и высокими лимитами ставок.',
    summary_en = 'Fonbet is the oldest and most reliable bookmaker in Russia and the CIS. It features sub-second live line updates and holds high security marks.',
    description_ru = 'Букмекерская контора Фонбет занимает лидирующие позиции на рынке ставок благодаря безупречной репутации, быстрым выплатам на карты и СБП, а также качественным видеотрансляциям топовых спортивных событий. Для вилочников Фонбет является обязательной конторой благодаря высокой живучести аккаунтов и лояльности к профессиональным игрокам на крупных рынках.',
    description_en = 'Fonbet is a premier sports betting operator with decades of historical presence. Known for its ultra-fast payouts, comprehensive sports coverage, and high-quality live streams directly in the app. For surebetting, Fonbet is a mandatory recreational bookmaker due to its high betting limits and prompt response times.'
WHERE id = 'fonbet';

-- 2. Pinnacle
UPDATE bet_source SET
    logo_emoji = '🔶',
    rating = 4.9,
    rating_reliability = 5.0,
    rating_coefficients = 5.0,
    referral_url = 'https://pinnacle.com',
    promo_code = 'PINSMART',
    bonus_ru = 'Минимальная маржа (от 1.5% до 2.5%)',
    bonus_en = 'Low Margin Guarantee (1.5% - 2.5%)',
    summary_ru = 'Pinnacle — мировой эталон профессионального беттинга с самыми высокими лимитами ставок, низким уровнем маржи и политикой отсутствия порезок за выигрыши.',
    summary_en = 'Pinnacle is the global benchmark for professional betting. Features highest limits, lowest margins, and a strict no-limit-reduction policy for winners.',
    description_ru = 'Pinnacle — безусловный лидер для профессиональных вилочников. Они официально приветствуют арбитражных игроков и никогда не снижают лимиты за успешные стратегии. Коэффициенты формируются с минимальной маржой, выступая в качестве ориентира для оценки вероятностей (True Odds) по всему миру.',
    description_en = 'Pinnacle is the ultimate destination for arbitrage and EV bettors. They officially welcome arbers and never restrict winning accounts. Offering margins close to zero, Pinnacle’s lines serve as the reference point for true market probability globally.'
WHERE id = 'pinnacle';

-- 3. Winline
UPDATE bet_source SET
    logo_emoji = '🧡',
    rating = 4.8,
    rating_reliability = 4.8,
    rating_coefficients = 4.6,
    referral_url = 'https://winline.ru',
    promo_code = 'WINSMART',
    bonus_ru = 'Бездепозитный фрибет до 3 000 ₽ в приложении',
    bonus_en = 'No-deposit freebet up to 3,000 ₽ in mobile app',
    summary_ru = 'Winline — технологичный и надежный букмекер, известный своей программой лояльности, трансляциями матчей и быстрым выводом средств.',
    summary_en = 'Winline is a high-tech bookmaker popular for its loyalty program, extensive live broadcasts, and fast mobile withdrawals.',
    description_ru = 'Winline предлагает отличный мобильный продукт и широкую линию на популярные виды спорта. Игрокам нравится программа лояльности «Бонус-клуб» и оперативный вывод выигрышей. Для арбитража Winline подходит как отличный донор на топовые футбольные и баскетбольные события.',
    description_en = 'Winline delivers a superior mobile betting experience with extensive live streaming of major events. Known for its strong customer retention campaigns. It serves as an excellent recreational partner in surebet algorithms for football and basketball.'
WHERE id = 'winline';

-- 4. 1xBet
UPDATE bet_source SET
    logo_emoji = '🔵',
    rating = 4.7,
    rating_reliability = 4.4,
    rating_coefficients = 4.9,
    referral_url = 'https://1xbet.com',
    promo_code = '1XSMART',
    bonus_ru = '100% бонус на первый депозит до 120$',
    bonus_en = '100% First Deposit Bonus up to $120',
    summary_ru = '1xBet — один из крупнейших международных букмекеров с гигантской линией, глубокой росписью событий и высокими коэффициентами.',
    summary_en = '1xBet is a massive international betting brand offering unmatched sports coverage, deep market options, and highly competitive odds.',
    description_ru = '1xBet предлагает одну из лучших линий в мире с тысячами рынков на каждое событие. Коэффициенты очень высокие благодаря низкой марже. Однако профессиональным игрокам следует быть аккуратными из-за возможной верификации при выводе крупных сумм.',
    description_en = '1xBet boasts an enormous sportsbook covering virtually every sport and event globally. Margins are low, leading to high odds. Arbers should proceed with caution due to strict verification procedures on high withdrawal amounts.'
WHERE id = '1xbet';

-- 5. Marathonbet
UPDATE bet_source SET
    logo_emoji = '💙',
    rating = 4.8,
    rating_reliability = 4.7,
    rating_coefficients = 4.9,
    referral_url = 'https://marathonbet.ru',
    promo_code = 'MARASMART',
    bonus_ru = 'Страховка первой ставки до 3 000 ₽',
    bonus_en = 'First bet insurance up to 3,000 ₽',
    summary_ru = 'Марафон — легендарный букмекер с уникально высокими коэффициентами (маржа 0% на топ-матчи) и отличной прематч-линией.',
    summary_en = 'Marathonbet is a legendary betting company offering top-market coefficients (0% margin on key events) and excellent pre-match depth.',
    description_ru = 'Марафон славится своей ценовой политикой: нулевая маржа на главные футбольные события делает их коэффициенты лучшими на рынке. Отличный выбор как для традиционных ставок, так и для поиска валуйных предложений.',
    description_en = 'Marathonbet stands out for its zero-margin promotions on major football leagues, ensuring the highest returns. A solid core bookmaker for arbing due to unique line movements.'
WHERE id = 'marathonbet';

-- 6. Betcity
UPDATE bet_source SET
    logo_emoji = '⚪',
    rating = 4.7,
    rating_reliability = 4.6,
    rating_coefficients = 4.6,
    referral_url = 'https://betcity.ru',
    promo_code = 'CITYSMART',
    bonus_ru = 'Фрибет до 10 000 ₽ за первый депозит',
    bonus_en = 'Up to 10,000 ₽ freebet on your first deposit',
    summary_ru = 'Бетсити — популярный букмекер с глубокой проработкой статистических рынков, уникальной росписью на игроков и надежными выплатами.',
    summary_en = 'Betcity is a major bookmaker famous for its comprehensive stats markets, unique player props, and reliable operations.',
    description_ru = 'Бетсити предлагает эксклюзивную линию на статистику матчей: угловые, карточки, фолы, броски и индивидуальные показатели игроков. Это делает его отличным источником уникальных вилок в прематче.',
    description_en = 'Betcity excels in statistics and player props, creating opportunities for niche market arbs that are unavailable elsewhere. Very reliable payout system.'
WHERE id = 'betcity';

-- 7. Liga Stavok
UPDATE bet_source SET
    logo_emoji = '🟢',
    rating = 4.6,
    rating_reliability = 4.7,
    rating_coefficients = 4.4,
    referral_url = 'https://ligastavok.ru',
    promo_code = 'LIGASMART',
    bonus_ru = 'Фрибет до 2 222 ₽ за регистрацию',
    bonus_en = 'Up to 2,222 ₽ freebet on sign up',
    summary_ru = 'Лига Ставок — крупный российский букмекер с хорошей репутацией, широкой сетью клубов и поддержкой отечественного спорта.',
    summary_en = 'Liga Stavok is a leading domestic bookmaker with a strong reputation, extensive betting shops, and robust local sport sponsorship.',
    description_ru = 'Лига Ставок предоставляет надежные выплаты и хороший выбор ставок на российский спорт. Для вилок контора используется на крупных рынках в паре с профессиональными БК.',
    description_en = 'Liga Stavok provides solid coverage of regional competitions. Ideal for matching arbs on primary markets with long account lifetime.'
WHERE id = 'ligastavok';

-- 8. Leon
UPDATE bet_source SET
    logo_emoji = '🦁',
    rating = 4.7,
    rating_reliability = 4.5,
    rating_coefficients = 4.7,
    referral_url = 'https://leon.ru',
    promo_code = 'LEONSMART',
    bonus_ru = '25 000 ₽ фрибетами за регистрацию',
    bonus_en = 'Up to 25,000 ₽ in freebets for new players',
    summary_ru = 'Леон — букмекер с быстрым выводом, стильным мобильным приложением и щедрыми еженедельными бонусами.',
    summary_en = 'Leon is an active betting brand with fast withdrawals, a modern app layout, and generous weekly bonus campaigns.',
    description_ru = 'Леон предлагает отличный интерфейс приложения и быструю обработку заявок на вывод денег. Маржа на топовые события низкая, что привлекает любителей выгодных коэффициентов.',
    description_en = 'Leon is highly rated for its mobile interface and smooth cashouts. Competitive margins on major football matches make it a solid choice.'
WHERE id = 'leon';

-- 9. Baltbet
UPDATE bet_source SET
    logo_emoji = '🔵',
    rating = 4.5,
    rating_reliability = 4.5,
    rating_coefficients = 4.5,
    referral_url = 'https://baltbet.ru',
    promo_code = 'BALTSMART',
    bonus_ru = '3 фрибета за регистрацию',
    bonus_en = '3 Welcome freebets on registration',
    summary_ru = 'Балтбет — один из старейших букмекеров с собственной аналитической линией и уникальной бонусной системой.',
    summary_en = 'Baltbet is a long-standing bookmaker featuring a proprietary compilation line and an interesting loyalty program.',
    description_ru = 'Балтбет примечателен тем, что держит собственный штат аналитиков. Из-за этого их коэффициенты часто отличаются от общемировых, создавая много выгодных арбитражных ситуаций.',
    description_en = 'Baltbet compiles its lines independently rather than copying global feeds. This variance creates numerous high-yield arbs for smart bettors.'
WHERE id = 'baltbet';

-- 10. Tennisi
UPDATE bet_source SET
    logo_emoji = '🍎',
    rating = 4.4,
    rating_reliability = 4.6,
    rating_coefficients = 4.2,
    referral_url = 'https://tennisi.bet',
    promo_code = 'TENSMART',
    bonus_ru = 'Бесконечный бонус до 17 500 ₽',
    bonus_en = 'Infinite bonus up to 17,500 ₽',
    summary_ru = 'Tennisi — букмекер с самым юмористическим подходом, уникальной линией на эксклюзивные события и надежными выплатами.',
    summary_en = 'Tennisi is a unique bookmaker famous for its humorous marketing, exclusive custom markets, and reliable payouts.',
    description_ru = 'Тенниси предлагает абсолютно уникальную линию ставок на то то, чего нет больше нигде (например, поведение тренеров, судейские решения, трансферные слухи). Прекрасный выбор для развлечения и ловли редких вилок.',
    description_en = 'Tennisi features a completely custom betting line with funny specials and specific player props. A fun addition to any betting portfolio.'
WHERE id = 'tennisi';

-- 11. Zenit
UPDATE bet_source SET
    logo_emoji = '🔵',
    rating = 4.5,
    rating_reliability = 4.4,
    rating_coefficients = 4.6,
    referral_url = 'https://zenit.win',
    promo_code = 'ZENITSMART',
    bonus_ru = '100% бонус на первый депозит до 20 000 ₽',
    bonus_en = '100% first deposit bonus up to 20,000 ₽',
    summary_ru = 'Зенит — классический букмекер с хорошим прематчем, простой регистрацией и стабильными выплатами.',
    summary_en = 'Zenit is a classic sports betting brand with robust pre-match odds, straightforward KYC, and consistent payouts.',
    description_ru = 'БК Зенит предлагает стабильные коэффициенты на популярные спортивные дисциплины. Подходит в качестве дополнительного букмекера для расширения вилочного пула.',
    description_en = 'Zenit provides competitive odds and covers a broad range of standard markets. Serves as a great secondary bookmaker.'
WHERE id = 'zenit';

-- 12. Pari
UPDATE bet_source SET
    logo_emoji = '🟢',
    rating = 4.8,
    rating_reliability = 4.8,
    rating_coefficients = 4.7,
    referral_url = 'https://pari.ru',
    promo_code = 'PARISMART',
    bonus_ru = 'Фрибет 2 024 ₽ новым клиентам',
    bonus_en = '2,024 ₽ Freebet for new registrants',
    summary_ru = 'Pari — один из лидеров российского рынка, известный ярким спонсорством, технологичной платформой и качественным лайвом.',
    summary_en = 'Pari is a top-tier modern betting company known for high-profile sponsorships, advanced platform, and great live line.',
    description_ru = 'Букмекер Pari предлагает отличный продукт на базе современных технологий, с быстрыми расчетами ставок и отличным выбором киберспорта. Удобен для вилок из-за высокой лояльности к активным аккаунтам.',
    description_en = 'Pari is a highly popular brand with outstanding esports coverage, quick bet settlements, and top-tier reliability for arbing.'
WHERE id = 'pari';

-- 13. Olimpbet
UPDATE bet_source SET
    logo_emoji = '🔴',
    rating = 4.6,
    rating_reliability = 4.6,
    rating_coefficients = 4.5,
    referral_url = 'https://olimp.bet',
    promo_code = 'OLIMPSMART',
    bonus_ru = 'Страховка первой ставки до 10 000 ₽',
    bonus_en = 'First bet insurance up to 10,000 ₽',
    summary_ru = 'Олимпбет — надежный букмекер со сбалансированной линией, хорошим выбором долгосрочных ставок и быстрыми выплатами.',
    summary_en = 'Olimpbet is a reliable sports bookmaker with balanced markets, long-term options, and rapid cashouts.',
    description_ru = 'Олимпбет предлагает конкурентные условия на популярные футбольные и хоккейные чемпионаты. Регулярные акции делают игру здесь более выгодной.',
    description_en = 'Olimpbet delivers solid coverage of domestic football leagues and major international events with friendly support.'
WHERE id = 'olimpbet';
