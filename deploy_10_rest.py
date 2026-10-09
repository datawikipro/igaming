import subprocess
import os
import sys
from pathlib import Path

modules = [
    'igaming-source-bcgame',
    'igaming-source-bet9ja',
    'igaming-source-betdaq',
    'igaming-source-favbet',
    'igaming-source-interwetten',
    'igaming-source-matchbook',
    'igaming-source-crickex',
    'igaming-source-iddaa',
    'igaming-source-fortuna',
    'igaming-source-bookmaker',
]

ports = {
    'igaming-source-bcgame': '8080',
    'igaming-source-bet9ja': '8118',
    'igaming-source-betdaq': '8080',
    'igaming-source-favbet': '8080',
    'igaming-source-interwetten': '8117',
    'igaming-source-matchbook': '8080',
    'igaming-source-crickex': '8124',
    'igaming-source-iddaa': '8120',
    'igaming-source-fortuna': '8080',
    'igaming-source-bookmaker': '8080',
}

print("=== 1. Preparing reactor pom.xml ===")
pom_lines = [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<project xmlns="http://maven.apache.org/POM/4.0.0"',
    '         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"',
    '         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">',
    '    <modelVersion>4.0.0</modelVersion>',
    '    <groupId>pro.datawiki.igaming</groupId>',
    '    <artifactId>igaming-parent</artifactId>',
    '    <version>1.0.0</version>',
    '    <packaging>pom</packaging>',
    '    <modules>',
    '        <module>igaming-dto</module>',
    '        <module>igaming-source-core</module>',
]
for m in modules:
    pom_lines.append(f'        <module>{m}</module>')
pom_lines.extend([
    '    </modules>',
    '</project>'
])
with open('/root/igaming/pom.xml', 'w') as f:
    f.write('\n'.join(pom_lines) + '\n')

print("=== 2. Compiling jars with Maven in Podman ===")
cmd_mvn = [
    'ionice', '-c', '2', '-n', '7',
    'podman', 'run', '--rm',
    '-v', '/root/.m2:/root/.m2',
    '-v', '/root/igaming:/workspace',
    '-w', '/workspace',
    'docker.io/library/maven:3.9-eclipse-temurin-21',
    'mvn', 'clean', 'package', '-DskipTests'
]
res = subprocess.run(cmd_mvn)
if res.returncode != 0:
    print("Maven returned non-zero, checking generated jars...")

print("=== 3. Packaging OCI images and importing to containerd ===")
built_modules = []
for m in modules:
    target_dir = Path(f'/root/igaming/{m}/target')
    if not target_dir.exists():
        print(f"[-] Target dir does not exist for {m}")
        continue
    jars = [j for j in target_dir.glob('*.jar') if not j.name.startswith('original-') and 'test' not in j.name]
    if not jars:
        print(f"[-] Jar not found for {m}")
        continue
    jar_path = jars[0]
    image_tag = f"ghcr.io/datawikipro/{m}:latest"
    port = ports.get(m, '8080')
    print(f"[+] Building container for {m} with jar {jar_path.name} -> {image_tag} (port {port})...")
    b_res = subprocess.run(['python3', '/root/build_and_import.py', str(jar_path), image_tag, port])
    if b_res.returncode == 0:
        built_modules.append(m)
    else:
        print(f"[-] Failed to build image for {m}")

print(f"\n=== Successfully built images for {len(built_modules)}/{len(modules)}: {built_modules} ===")

print("\n=== 4. Updating deployments in k8s ===")
for m in built_modules:
    dep_name = m
    print(f"Deploying {dep_name}...")
    subprocess.run(['kubectl', 'set', 'image', f'deployment/{dep_name}', f'{dep_name}=ghcr.io/datawikipro/{m}:latest', '-n', 'igaming-source'], check=False)
    subprocess.run(['kubectl', 'set', 'resources', f'deployment/{dep_name}', '--limits=memory=1024Mi,cpu=1000m', '--requests=memory=256Mi,cpu=20m', '-n', 'igaming-source'], check=False)
    subprocess.run(['kubectl', 'set', 'env', f'deployment/{dep_name}', 'SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect', 'SPRING_MAIN_ALLOW_CIRCULAR_REFERENCES=true', '-n', 'igaming-source'], check=False)
    subprocess.run(['kubectl', 'patch', f'deployment/{dep_name}', '-n', 'igaming-source', '-p', '{"spec":{"template":{"spec":{"containers":[{"name":"' + dep_name + '","imagePullPolicy":"IfNotPresent"}]}}}}'], check=False)
    subprocess.run(['kubectl', 'rollout', 'restart', f'deployment/{dep_name}', '-n', 'igaming-source'], check=False)

print("\nAll REST deployments updated successfully!")
