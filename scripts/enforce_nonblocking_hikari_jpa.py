import glob

files = glob.glob(r"igaming-k8s/*.yaml")
count = 0

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    modified = False
    
    # Check if SPRING_JPA_PROPERTIES_HIBERNATE_TEMP_USE_JDBC_METADATA_DEFAULTS is present
    if 'SPRING_JPA_PROPERTIES_HIBERNATE_TEMP_USE_JDBC_METADATA_DEFAULTS' not in content:
        # Add non-blocking JPA properties after SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT
        target = '- name: SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT\n          value: "0"'
        replacement = target + '''
        - name: SPRING_JPA_PROPERTIES_HIBERNATE_TEMP_USE_JDBC_METADATA_DEFAULTS
          value: "false"
        - name: SPRING_JPA_DATABASE_PLATFORM
          value: org.hibernate.dialect.PostgreSQLDialect'''
        
        if target in content:
            content = content.replace(target, replacement)
            modified = True
    
    if modified:
        with open(f, 'w', encoding='utf-8') as out_f:
            out_f.write(content)
        count += 1
        print(f"Enforced non-blocking Hibernate dialect & metadata defaults in {f}")

print(f"Total manifests updated: {count}")
