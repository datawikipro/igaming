import json
import os

files = [
    'igaming-bot/src/main/resources/content_plan_ru.json',
    'igaming-bot/src/main/resources/content_plan_en.json',
    'igaming-bot/src/main/resources/content_plan_es.json',
    'igaming-bot/src/main/resources/content_plan_fr.json'
]

intro_ru = {
    "day": 0,
    "blogPost": "👋 Добро пожаловать! Мы — команда инженеров SmartBet.guru.\n\nНаш проект создан для тех, кто ищет математическое преимущество над букмекером. Здесь мы будем делиться тем, как работает наш сканер изнутри, как обходить антифрод-системы БК, и публиковать самые интересные вилки (Surebets) и валуи (+EV). Наш подход: никаких эмоций, только чистый алгоритмический расчет и статистика! 🤖\n\n⚠️ ВАЖНО: Мы категорически против необдуманного беттинга и лудомании. Любые ставки сопряжены с высоким риском потери средств. Относитесь к этому как к инвестициям с повышенным риском, никогда не ставьте последние деньги и всегда сохраняйте холодный рассудок.",
    "devPost": "👋 Добро пожаловать! Мы — команда инженеров SmartBet.guru.\n\nНаш проект создан для тех, кто ищет математическое преимущество над букмекером. Здесь мы будем делиться тем, как работает наш сканер изнутри, как обходить антифрод-системы БК, и публиковать самые интересные вилки (Surebets) и валуи (+EV). Наш подход: никаких эмоций, только чистый алгоритмический расчет и статистика! 🤖\n\n⚠️ ВАЖНО: Мы категорически против необдуманного беттинга и лудомании. Любые ставки сопряжены с высоким риском потери средств. Относитесь к этому как к инвестициям с повышенным риском, никогда не ставьте последние деньги и всегда сохраняйте холодный рассудок."
}

disclaimer = "\n\n⚠️ Помните: Ставки на спорт сопряжены с финансовыми рисками. Мы против лудомании и необдуманного беттинга. Играйте ответственно."

for f in files:
    if not os.path.exists(f):
        continue
    with open(f, 'r', encoding='utf-8') as file:
        data = json.load(file)
        
    # Append disclaimer to all existing posts
    for item in data:
        if "⚠️" not in item.get('blogPost', ''):
            item['blogPost'] = item.get('blogPost', '') + disclaimer
        if "⚠️" not in item.get('devPost', ''):
            item['devPost'] = item.get('devPost', '') + disclaimer
            
    # Prepend intro post if not exists
    if len(data) == 0 or data[0].get('day') != 0:
        data.insert(0, intro_ru)
        
    with open(f, 'w', encoding='utf-8') as file:
        json.dump(data, file, ensure_ascii=False, indent=2)

print("Updated content plans.")
