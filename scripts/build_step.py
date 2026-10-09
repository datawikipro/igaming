import subprocess
import sys

def run_step(desc, cmd, timeout=300):
    print(f"==================================================")
    print(f"STEP: {desc}")
    print(f"COMMAND: {cmd}")
    print(f"==================================================")
    res = subprocess.run([
        'ssh', '-i', r'C:\Users\chernousov_a\.ssh\id_ed25519',
        '-o', 'BatchMode=yes', '-o', 'ConnectTimeout=10',
        'root@100.78.183.101', cmd
    ], capture_output=True, text=True, timeout=timeout)
    print("STDOUT:\n", res.stdout[-2000:] if len(res.stdout) > 2000 else res.stdout)
    if res.stderr:
        print("STDERR:\n", res.stderr[-1000:] if len(res.stderr) > 1000 else res.stderr)
    print("EXIT CODE:", res.returncode)
    if res.returncode != 0:
        sys.exit(res.returncode)

if __name__ == '__main__':
    step = sys.argv[1] if len(sys.argv) > 1 else 'core'
    if step == 'core':
        run_step("Build & Install igaming-source-core", 
                 "/usr/local/bin/mvn -f igaming-source-core/pom.xml install -DskipTests -B", timeout=180)
    elif step == 'domain':
        run_step("Build & Install aggregator-domain", 
                 "/usr/local/bin/mvn -f aggregator-domain/pom.xml install -DskipTests -B", timeout=180)
    elif step == 'surebet':
        run_step("Build & Push aggregator-surebet with Jib",
                 "/usr/local/bin/mvn -f aggregator-surebet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-aggregator-surebet:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
    elif step == 'winline':
        run_step("Build & Push igaming-source-winline with Jib",
                 "/usr/local/bin/mvn -f igaming-source-winline/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-source-winline:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
    elif step == 'fonbet':
        run_step("Build & Push igaming-source-fon-bet-ru with Jib",
                 "/usr/local/bin/mvn -f igaming-source-fon-bet-ru/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-source-fon-bet-ru:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
    elif step == 'betcity':
        run_step("Build & Push igaming-source-betcity with Jib",
                 "/usr/local/bin/mvn -f igaming-source-betcity/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-source-betcity:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
    elif step == 'marathonbet':
        run_step("Build & Push igaming-source-marathonbet with Jib",
                 "/usr/local/bin/mvn -f igaming-source-marathonbet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-source-marathonbet:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
    elif step == 'baltbet':
        run_step("Build & Push igaming-source-baltbet with Jib",
                 "/usr/local/bin/mvn -f igaming-source-baltbet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-source-baltbet:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
    elif step == 'olimpbet':
        run_step("Build & Push igaming-source-olimpbet with Jib",
                 "/usr/local/bin/mvn -f igaming-source-olimpbet/pom.xml compile com.google.cloud.tools:jib-maven-plugin:3.4.4:build "
                 "-Djib.from.image=100.78.183.101:30500/eclipse-temurin:21-jre "
                 "-Djib.to.image=100.78.183.101:30500/igaming-source-olimpbet:latest "
                 "-Djib.allowInsecureRegistries=true -DskipTests -B", timeout=300)
