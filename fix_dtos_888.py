import os
import re

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Remove lombok imports and @Data
    content = re.sub(r'import lombok\..*;\n', '', content)
    content = re.sub(r'[ \t]*@Data\n', '', content)

    # Find all classes (including nested)
    class_matches = list(re.finditer(r'(public\s+(?:static\s+)?class\s+(\w+).*?\{)(.*?)(?=\n[ \t]*(?:public\s+(?:static\s+)?class|\Z|\}))', content, re.DOTALL))
    
    # We will reconstruct the file
    lines = content.split('\n')
    new_lines = []
    
    i = 0
    while i < len(lines):
        line = lines[i]
        new_lines.append(line)
        
        # Check if this line is a class declaration
        m = re.match(r'^([ \t]*)public\s+(?:static\s+)?class\s+(\w+)', line)
        if m:
            indent = m.group(1)
            # Find fields in this class
            fields = []
            j = i + 1
            while j < len(lines):
                if re.match(r'^[ \t]*public\s+(?:static\s+)?class\s+', lines[j]):
                    break
                if lines[j].strip() == '}':
                    pass
                m_field = re.match(r'^[ \t]*private\s+([\w<>,\s]+)\s+(\w+);', lines[j])
                if m_field:
                    fields.append((m_field.group(1), m_field.group(2)))
                j += 1
            
            # Now we know the fields, we can insert getters and setters before the next '}' at this indent
            # Actually, let's just insert them right after fields
            pass # wait, it's easier to just insert getters/setters before the '}' of the class
            
        i += 1

