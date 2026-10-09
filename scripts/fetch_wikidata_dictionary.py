import urllib.request
import urllib.parse
import json
import time
import os

HEADERS = {
    'User-Agent': 'SmartBetGuruBot/1.0 (sports-aggregator; contact@smartbet.guru)'
}

QUERIES = [
    # 1. Top Football Clubs (by league)
    {
        'sport': 'FOOTBALL',
        'desc': 'Football Major Leagues Clubs',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel 
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          VALUES ?league { 
            wd:Q9448   # Premier League
            wd:Q324867 # La Liga
            wd:Q15804  # Serie A
            wd:Q82595  # Bundesliga
            wd:Q13394  # Ligue 1
            wd:Q189118 # RPL
            wd:Q182994 # Liga Portugal
            wd:Q2470   # Eredivisie
            wd:Q18656  # MLS
            wd:Q19658  # Brasileirao
            wd:Q11445  # Argentine Primera
            wd:Q483020 # Saudi Pro League
            wd:Q208579 # Turkish Super Lig
            wd:Q184487 # Scottish Premiership
            wd:Q186638 # Belgian Pro League
            wd:Q12248  # EFL Championship
            wd:Q506248 # Russian First League
          }
          ?team wdt:P118 ?league .
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        '''
    },
    # 2. National Football Teams
    {
        'sport': 'FOOTBALL',
        'desc': 'National Football Teams',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          ?team wdt:P31 wd:Q6979593 .
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        '''
    },
    # 3. Hockey (NHL, KHL, SHL, Liiga)
    {
        'sport': 'HOCKEY',
        'desc': 'Hockey Major Leagues Clubs',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          VALUES ?league {
            wd:Q1215892 # NHL
            wd:Q193510  # KHL
            wd:Q215286  # SHL
            wd:Q851365  # Liiga
            wd:Q11978   # VHL
          }
          ?team wdt:P118 ?league .
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        '''
    },
    # 4. Basketball (NBA, EuroLeague, VTB, ACB)
    {
        'sport': 'BASKETBALL',
        'desc': 'Basketball Major Leagues Clubs',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          VALUES ?league {
            wd:Q155223  # NBA
            wd:Q185493  # EuroLeague
            wd:Q188185  # VTB United League
            wd:Q11251   # Liga ACB
          }
          ?team wdt:P118 ?league .
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        '''
    },
    # 5. Top Tennis Players (ATP & WTA Singles Players)
    {
        'sport': 'TENNIS',
        'desc': 'Top Tennis Players',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          ?team wdt:P106 wd:Q10833314 . # Tennis player
          ?team wdt:P564 ?atpId .        # Has ATP ID (pro player)
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        LIMIT 2500
        '''
    },
    {
        'sport': 'TENNIS',
        'desc': 'Top WTA Tennis Players',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          ?team wdt:P106 wd:Q10833314 . # Tennis player
          ?team wdt:P597 ?wtaId .        # Has WTA ID (pro player)
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        LIMIT 2500
        '''
    },
    # 6. Esports Teams (CS, Dota, LoL)
    {
        'sport': 'ESPORTS',
        'desc': 'Esports Teams',
        'query': '''
        SELECT DISTINCT ?team ?enLabel ?ruLabel
          (GROUP_CONCAT(DISTINCT ?altEn; separator="|") AS ?altsEn)
          (GROUP_CONCAT(DISTINCT ?altRu; separator="|") AS ?altsRu)
        WHERE {
          ?team wdt:P31 wd:Q24068307 . # Esports team
          OPTIONAL { ?team rdfs:label ?enLabel FILTER(LANG(?enLabel) = "en") }
          OPTIONAL { ?team rdfs:label ?ruLabel FILTER(LANG(?ruLabel) = "ru") }
          OPTIONAL { ?team skos:altLabel ?altEn FILTER(LANG(?altEn) = "en") }
          OPTIONAL { ?team skos:altLabel ?altRu FILTER(LANG(?altRu) = "ru") }
        }
        GROUP BY ?team ?enLabel ?ruLabel
        LIMIT 2000
        '''
    }
]

def run_sparql(query):
    url = 'https://query.wikidata.org/sparql?query=' + urllib.parse.quote(query.strip()) + '&format=json'
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=45) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        return data['results']['bindings']

def main():
    os.makedirs('data', exist_ok=True)
    all_teams = []
    
    print('Starting Wikidata fetch for canonical team translations and aliases...')
    for q_cfg in QUERIES:
        desc = q_cfg['desc']
        sport = q_cfg['sport']
        print(f'Fetching {desc}...')
        try:
            results = run_sparql(q_cfg['query'])
            print(f'  -> Returned {len(results)} items')
            for r in results:
                en = r.get('enLabel', {}).get('value', '').strip()
                ru = r.get('ruLabel', {}).get('value', '').strip()
                alts_en = [a.strip() for a in r.get('altsEn', {}).get('value', '').split('|') if a.strip()]
                alts_ru = [a.strip() for a in r.get('altsRu', {}).get('value', '').split('|') if a.strip()]
                
                # We need at least one valid name
                if not en and not ru:
                    continue
                
                all_teams.append({
                    'sport': sport,
                    'name_en': en,
                    'name_ru': ru,
                    'aliases_en': alts_en,
                    'aliases_ru': alts_ru
                })
            # Sleep slightly to avoid Wikidata rate limits
            time.sleep(1.5)
        except Exception as e:
            print(f'  [ERROR] Failed to fetch {desc}: {e}')

    out_file = 'data/wikidata_canonical_teams.json'
    with open(out_file, 'w', encoding='utf-8') as f:
        json.dump(all_teams, f, ensure_ascii=False, indent=2)
        
    print(f'Successfully saved {len(all_teams)} canonical teams to {out_file}.')

if __name__ == '__main__':
    main()
