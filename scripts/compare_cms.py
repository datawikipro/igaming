import subprocess
import difflib
import re

nl = subprocess.check_output(['ssh', '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10', 'root@100.78.183.101',
                             'kubectl get cm purevpn-nl-direct-config -n proxy -o jsonpath={.data.client\\\\.ovpn}']).decode()
uk = subprocess.check_output(['ssh', '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10', 'root@100.78.183.101',
                             'kubectl get cm purevpn-uk-direct-config -n proxy -o jsonpath={.data.client\\\\.ovpn}']).decode()

def get_block(text, tag):
    m = re.search(r'<' + tag + r'>(.*?)</' + tag + r'>', text, re.DOTALL)
    return m.group(1) if m else None

for tag in ['ca', 'cert', 'key', 'tls-auth']:
    b_nl = get_block(nl, tag)
    b_uk = get_block(uk, tag)
    print(tag, 'len nl:', len(b_nl) if b_nl else None, 'len uk:', len(b_uk) if b_uk else None)
    if b_nl != b_uk:
        print('DIFFERENCE in', tag)
        diff = list(difflib.unified_diff((b_nl or '').splitlines(), (b_uk or '').splitlines()))
        print('\n'.join(diff[:20]))
