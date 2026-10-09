import urllib.request
import json
import sys

PROJECT_ID = '2df124d7-25b0-4145-a63c-aafcb0fe0041'
WORKSPACE_SLUG = 'dataplatform'

targets = [
    ('13', 'dec18472-bbc7-474d-8f1a-a49590bbe481', 'ligastavok'),
    ('12', '453d6aa4-b293-4b1e-9e72-d19915d3c773', 'leon'),
    ('85', 'ba3aad43-cfcb-4251-bfcf-0f0eb1822cb4', '1xbit'),
    ('86', '71601966-6782-4079-9d04-5670184ca85a', 'melbet-com'),
    ('82', '5d6f2860-08fe-4213-b497-8a40fb553808', 'marathonbet-by'),
    ('83', 'b169e481-0702-41e2-b5cc-6cbdc8b8235f', 'fon-bet-by'),
    ('84', '7c3c05ad-4121-458e-83c9-40e9c8105117', 'fon-bet-kz')
]

for seq_id, iss_id, name in targets:
    payload = {
        'event': 'issue_activity',
        'action': 'updated',
        'data': {
            'field': 'state',
            'new_value': 'AI разработка',
            'issue': iss_id,
            'project': PROJECT_ID,
            'workspace': WORKSPACE_SLUG
        }
    }
    req = urllib.request.Request(
        'http://100.78.183.101:30885/webhook/plane',
        data=json.dumps(payload).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST'
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as r:
            body = r.read().decode('utf-8')
            print(f'Dispatched #{seq_id} [{name}]: HTTP {r.status} -> {body}')
    except Exception as e:
        print(f'Error #{seq_id} [{name}]: {e}')
