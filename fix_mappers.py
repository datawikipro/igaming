import os
import re

def fix_mapper(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # public BetType map(String m, String o) -> public BetType map(String m, String o, Double param)
    content = re.sub(r'public BetType map\(String ([a-zA-Z_]+), String ([a-zA-Z_]+)\) \{',
                     r'public BetType map(String \1, String \2, Double param) {', content)
    
    # mapTotalRecord(..., isAsian) -> mapTotalRecord(..., isAsian, param)
    content = re.sub(r'mapTotalRecord\(([^;]+)\)', lambda m: 'mapTotalRecord(' + m.group(1) + ', param)' if 'param' not in m.group(1) else m.group(0), content)
    
    # mapHandicapRecord(..., isAsian) -> mapHandicapRecord(..., isAsian, param)
    content = re.sub(r'mapHandicapRecord\(([^;]+)\)', lambda m: 'mapHandicapRecord(' + m.group(1) + ', param)' if 'param' not in m.group(1) else m.group(0), content)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

def fix_processor(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # remove the isParametric block completely
    block_pattern = r'if \(\w+\.isParametric\(\).*?\{.*?try\s*\{.*?\w+\.withParam\(.*?catch\s*\(.*?\)\s*\{\s*\}.*?\}'
    content = re.sub(block_pattern, '', content, flags=re.DOTALL)

    # replace betTypeResolverService.resolve(...)
    # resolve(bookmaker, sportType, market, outcome) -> resolve(bookmaker, sportType, market, outcome, odd.getParam())
    # We need to find what the "odd" object is called. usually it's `odd` or `rate` or `event`?
    # Actually, we can just replace .resolve(..., market, outcome) with .resolve(..., market, outcome, rate.getParam())
    # Let's handle this carefully. We'll use a simpler regex or manual fix.
    
    # Wait, the processors typically do:
    # BetType betType = betTypeResolverService.resolve(..., market, outcome);
    # Let's replace `resolve([^,]+, [^,]+, ([^,]+), ([^,]+))` wait, `resolve` can have 4 or 5 args.
    content = re.sub(r'betTypeResolverService\.resolve\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,)]+)\)',
                     r'betTypeResolverService.resolve(\1, \2, \3, \4, rate.getParam())', content)
    
    content = re.sub(r'betTypeResolverService\.resolve\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,)]+)\)',
                     r'betTypeResolverService.resolve(\1, \2, \3, \4, rate.getParam(), \5)', content)
                     
    # What if it's called `odd.getParam()`? Let's check if `odd` exists.
    content = content.replace('rate.getParam()', 'rate != null ? rate.getParam() : odd.getParam()')
    # Actually, better to just let Java compiler tell us if `rate` or `odd` is wrong, and fix manually.

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

base_dir = 'c:/Users/chernousov_a/IdeaProjects/igaming'
for root, dirs, files in os.walk(base_dir):
    if 'target' in root or '.git' in root:
        continue
    for file in files:
        if file.endswith('Mapper.java'):
            fix_mapper(os.path.join(root, file))
        elif file.endswith('Processor.java'):
            fix_processor(os.path.join(root, file))

