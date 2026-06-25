import sys
import json

try:
    data = json.load(sys.stdin)
except Exception as e:
    sys.stderr.write(f"Error loading JSON from stdin: {e}\n")
    sys.exit(1)

metadata = data.get('metadata', {})
for k in ['uid', 'resourceVersion', 'creationTimestamp', 'generation', 'annotations', 'ownerReferences', 'selfLink']:
    metadata.pop(k, None)
metadata['name'] = 'igaming-aggregator-middles'
metadata['labels'] = {'app': 'igaming-aggregator-middles'}

spec = data.get('spec', {})
spec['selector']['matchLabels'] = {'app': 'igaming-aggregator-middles'}
spec['template']['metadata']['labels'] = {'app': 'igaming-aggregator-middles'}
spec['template']['metadata'].pop('annotations', None)

pod_spec = spec['template']['spec']
container = pod_spec['containers'][0]
container['name'] = 'middles'
container['resources'] = {
    'limits': {'cpu': '1', 'memory': '1536Mi'},
    'requests': {'cpu': '100m', 'memory': '768Mi'}
}

env = container.get('env', [])
env = [e for e in env if e.get('name') != 'APP_SUREBET_ROLE']
env.append({'name': 'APP_SUREBET_ROLE', 'value': 'middles'})
container['env'] = env

data.pop('status', None)
print(json.dumps(data, indent=2))
