import os
import glob

files = glob.glob("/Users/aleksei.chernousov/IdeaProjects/igaming/igaming-source-bwin/src/main/java/pro/datawiki/igaming/source/bwin/service/*.java")

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    if "lombok" in content or "@Slf4j" in content or "@RequiredArgsConstructor" in content:
        # We will replace them
        import re
        
        # Remove imports
        content = re.sub(r'import lombok\..*;\n', '', content)
        
        # Add SLF4J Logger import if needed
        if "@Slf4j" in content:
            if "import org.slf4j.Logger;" not in content:
                content = content.replace("public class", "import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\n\npublic class", 1)
        
        # Class name
        class_match = re.search(r'public class (\w+)', content)
        if class_match:
            class_name = class_match.group(1)
            
            # Replace @Slf4j with Logger
            if "@Slf4j" in content:
                content = content.replace("@Slf4j\n", "")
                content = content.replace("public class " + class_name + " {", "public class " + class_name + " {\n    private static final Logger log = LoggerFactory.getLogger(" + class_name + ".class);\n")
                content = content.replace("public class " + class_name + " extends AbstractBaseBookmakerService {", "public class " + class_name + " extends AbstractBaseBookmakerService {\n    private static final Logger log = LoggerFactory.getLogger(" + class_name + ".class);\n")
            
            # For @RequiredArgsConstructor, we need to generate constructor manually. But we can just use Spring's @Autowired or let python script do it.
            # Actually, I will just write explicit python logic for each file because there are only 4.

