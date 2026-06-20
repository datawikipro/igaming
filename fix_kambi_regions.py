import os
import glob

files = glob.glob('igaming-k8s/*.yaml')
target = 'APP_TARGET_HOST'
region_env = '- name: APP_BOOKMAKER_REGIONS\n          value: EU'

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    if 'eu-offering.kambicdn.org' in content and 'APP_BOOKMAKER_REGIONS' not in content:
        lines = content.split('\n')
        out = []
        for line in lines:
            if 'name: APP_TARGET_HOST' in line:
                out.append('        ' + region_env)
            out.append(line)
        with open(f, 'w') as file:
            file.write('\n'.join(out))
        print(f"Updated {f}")
