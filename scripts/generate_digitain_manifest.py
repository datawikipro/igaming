with open(r"igaming-k8s/baltbet.ru.yaml", "r", encoding="utf-8") as f:
    content = f.read()

# Replace baltbet -> digitain
digitain_content = content.replace("baltbet", "digitain").replace("igaming_baltbet", "igaming_digitain")

with open(r"igaming-k8s/digitain.yaml", "w", encoding="utf-8") as f:
    f.write(digitain_content)

print("Generated complete digitain.yaml manifest.")
