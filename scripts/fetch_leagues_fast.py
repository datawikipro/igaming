import urllib.request
import urllib.parse
import json
import time

HEADERS = {
    'User-Agent': 'SmartBetGuruBot/1.0 (sports-aggregator; contact@smartbet.guru)'
}

LEAGUES = [
    # Football
    ('FOOTBALL', 'Premier League', 'wd:Q9448'),
    ('FOOTBALL', 'La Liga', 'wd:Q324867'),
    ('FOOTBALL', 'Serie A', 'wd:Q15804'),
    ('FOOTBALL', 'Bundesliga', 'wd:Q82595'),
    ('FOOTBALL', 'Ligue 1', 'wd:Q13394'),
    ('FOOTBALL', 'RPL', 'wd:Q189118'),
    ('FOOTBALL', 'Liga Portugal', 'wd:Q182994'),
    ('FOOTBALL', 'Eredivisie', 'wd:Q2470'),
    ('FOOTBALL', 'MLS', 'wd:Q18656'),
    ('FOOTBALL', 'Brasileirao', 'wd:Q19658'),
    ('FOOTBALL', 'Argentine Primera', 'wd:Q11445'),
    ('FOOTBALL', 'Saudi Pro League', 'wd:Q483020'),
    ('FOOTBALL', 'Turkish Super Lig', 'wd:Q208579'),
    ('FOOTBALL', 'Scottish Premiership', 'wd:Q184487'),
    ('FOOTBALL', 'Belgian Pro League', 'wd:Q186638'),
    ('FOOTBALL', 'EFL Championship', 'wd:Q12248'),
    ('FOOTBALL', 'Russian First League', 'wd:Q506248'),
    # Hockey
    ('HOCKEY', 'NHL', 'wd:Q1215892'),
    ('HOCKEY', 'KHL', 'wd:Q193510'),
    ('HOCKEY', 'SHL', 'wd:Q215286'),
    ('HOCKEY', 'Liiga', 'wd:Q851365'),
    ('HOCKEY', 'VHL', 'wd:Q11978'),
    # Basketball
    ('BASKETBALL', 'NBA', 'wd:Q155223'),
    ('BASKETBALL', 'EuroLeague', 'wd:Q185493'),
    ('BASKETBALL', 'VTB United League', 'wd:Q188185'),
    ('BASKETBALL', 'Liga ACB', 'wd:Q11251'),
    ('BASKETBALL', 'BBL Germany', 'wd:Q694380'),
    ('BASKETBALL', 'LNB Pro A France', 'wd:Q1463774'),
    ('BASKETBALL', 'Lega Basket Serie A', 'wd:Q929919'),
]

def fetch_league(sport, name, qid):
    sparql = f'''
    SELECT DISTINCT ?team ?enLabel ?ruLabel 
      (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
      (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
    WHERE {{
      ?team wdt:P118 {qid} .
      OPTIONAL {{ ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }}
      OPTIONAL {{ ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }}
      OPTIONAL {{ ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }}
      OPTIONAL {{ ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }}
    }}
    GROUP BY ?team ?enLabel ?ruLabel
    '''
    url = 'https://query.wikidata.org/sparql?query=' + urllib.parse.quote(sparql.strip()) + '&format=json'
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=20) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data['results']['bindings']

all_items = []
for sport, name, qid in LEAGUES:
    t0 = time.time()
    try:
        bindings = fetch_league(sport, name, qid)
        dt = time.time() - t0
        print(f"[{sport}] {name} ({qid}): fetched {len(bindings)} teams in {dt:.2f}s")
        for b in bindings:
            en = b.get('enLabel', {}).get('value', '').strip()
            ru = b.get('ruLabel', {}).get('value', '').strip()
            if not en and not ru:
                continue
            alts_en = [x.strip() for x in b.get('altsEn', {}).get('value', '').split('|') if x.strip()]
            alts_ru = [x.strip() for x in b.get('altsRu', {}).get('value', '').split('|') if x.strip()]
            all_items.append({
                'sport': sport,
                'league': name,
                'name_en': en,
                'name_ru': ru,
                'aliases_en': alts_en,
                'aliases_ru': alts_ru
            })
        time.sleep(0.5)
    except Exception as e:
        print(f"[{sport}] {name} ({qid}) ERROR: {e}")

print(f"Total teams collected from major leagues: {len(all_items)}")
with open('data/wikidata_leagues_teams.json', 'w', encoding='utf-8') as f:
    json.dump(all_items, f, ensure_ascii=False, indent=2)
