import glob
import re

files = glob.glob("/Users/aleksei.chernousov/IdeaProjects/igaming/igaming-source-888sport/src/main/java/pro/datawiki/igaming/source/sport888/**/*.java", recursive=True)

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    original = content
    if "lombok" in content:
        content = re.sub(r'import lombok\..*;\n', '', content)
        
        if "@Slf4j" in original:
            if "import org.slf4j.Logger;" not in content:
                content = content.replace("public class", "import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\n\npublic class", 1)
        
        class_match = re.search(r'public class (\w+)', content)
        if class_match:
            class_name = class_match.group(1)
            if "@Slf4j" in original:
                content = content.replace("@Slf4j\n", "")
                content = content.replace(f"public class {class_name} {{", f"public class {class_name} {{\n    private static final Logger log = LoggerFactory.getLogger({class_name}.class);\n")
                content = content.replace(f"public class {class_name} extends AbstractBaseBookmakerService {{", f"public class {class_name} extends AbstractBaseBookmakerService {{\n    private static final Logger log = LoggerFactory.getLogger({class_name}.class);\n")
            
            if "@RequiredArgsConstructor" in original:
                content = content.replace("@RequiredArgsConstructor\n", "")
                
                # Try to find all final fields to generate constructor
                fields = re.findall(r'private final ([\w<>,\s]+) (\w+);', content)
                if fields:
                    params = ", ".join([f"{t} {n}" for t, n in fields])
                    assigns = "\n".join([f"        this.{n} = {n};" for t, n in fields])
                    constructor = f"\n    public {class_name}({params}) {{\n{assigns}\n    }}\n"
                    # Insert constructor before first method
                    content = re.sub(r'(    public (?!class|interface)[\w<>\s]+ \w+\()', constructor + r'\1', content, 1)

        # Config specific
        if "@Getter" in original:
            content = content.replace("@Getter\n", "")
            content = content.replace("@Setter\n", "")
            if class_name == "Sport888Config":
                content = content.replace("private Api api = new Api();", "private Api api = new Api();\n    public Api getApi() { return api; }\n    public void setApi(Api api) { this.api = api; }")
                content = content.replace("private Fetch fetch = new Fetch();", "private Fetch fetch = new Fetch();\n    public Fetch getFetch() { return fetch; }\n    public void setFetch(Fetch fetch) { this.fetch = fetch; }")
                content = content.replace("private String baseUrl;", "private String baseUrl;\n        public String getBaseUrl() { return baseUrl; }\n        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }")
                content = content.replace("private long delayMs = 15000;", "private long delayMs = 15000;\n        public long getDelayMs() { return delayMs; }\n        public void setDelayMs(long delayMs) { this.delayMs = delayMs; }")
            
        with open(f, 'w') as file:
            file.write(content)

