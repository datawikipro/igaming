import os
import glob

dtos = glob.glob("/Users/aleksei.chernousov/IdeaProjects/igaming/igaming-source-888sport/src/main/java/pro/datawiki/igaming/source/sport888/dto/spectate/*.java")

for f in dtos:
    with open(f, 'r') as file:
        content = file.read()
    
    if "@Data" in content:
        # Generate getters/setters
        lines = content.split('\n')
        fields = []
        for line in lines:
            if line.strip().startswith("private"):
                parts = line.strip().split()
                if len(parts) >= 3:
                    type_name = parts[1]
                    field_name = parts[2].rstrip(';')
                    fields.append((type_name, field_name))
        
        new_content = content.replace("import lombok.Data;\n", "")
        new_content = new_content.replace("@Data\n", "")
        
        getters_setters = "\n"
        for type_name, field_name in fields:
            capitalized = field_name[0].upper() + field_name[1:]
            getters_setters += f"    public {type_name} get{capitalized}() {{ return {field_name}; }}\n"
            getters_setters += f"    public void set{capitalized}({type_name} {field_name}) {{ this.{field_name} = {field_name}; }}\n"
        
        new_content = new_content.replace("}", getters_setters + "}")
        
        with open(f, 'w') as file:
            file.write(new_content)

