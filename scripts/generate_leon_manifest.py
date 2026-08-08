with open(r"igaming-k8s/baltbet.ru.yaml", "r", encoding="utf-8") as f:
    content = f.read()

# Replace baltbet -> leon
leon_content = content.replace("baltbet", "leon").replace("igaming_baltbet", "igaming_leon")

with open(r"igaming-k8s/leon.ru.yaml", "w", encoding="utf-8") as f:
    f.write(leon_content)

print("Generated complete leon.ru.yaml with DB service, crawler, and loader deployments.")
