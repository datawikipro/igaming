import subprocess

modules = [
    'igaming-source-betnacional',
    'igaming-source-betesporte',
    'igaming-source-esportesdasorte',
    'igaming-source-vaidebet'
]

for m in modules:
    print(f"Testing compilation of {m}...")
    res = subprocess.run(
        f'mvn -f {m}/pom.xml test-compile -DskipTests -B',
        shell=True, capture_output=True, text=True
    )
    if res.returncode == 0:
        print(f"  {m}: SUCCESS")
    else:
        print(f"  {m}: FAILED!")
        print(res.stdout[-1000:])
        print(res.stderr[-500:])
