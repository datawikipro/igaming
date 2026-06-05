import csv
import re
import difflib

# Load the CSV
aliases = []
with open("aliases.csv", "r", encoding="utf-8-sig") as f:
    reader = csv.DictReader(f)
    reader.fieldnames = [name.strip() for name in reader.fieldnames]
    for row in reader:
        aliases.append({k.strip(): v for k, v in row.items()})

def simplify(s):
    return "".join(c for c in s.lower() if c.isalnum())

def similarity(s1, s2):
    return difflib.SequenceMatcher(None, simplify(s1), simplify(s2)).ratio()

# Analysis rules (same as before)
stats_keywords = ['жк', 'угл', 'фол', 'аут', 'офсайд', 'удар', 'карточк', 'броск', 'бр', 'пенальти', 'сейв']
gender_keywords = [r'\(ж\)', r'\(жен\)', r'жен\b', r'women']
age_keywords = [r'u\d+', r'до\s*\d+', r'\(\d+\)', r'\bмол\b', r'youth', r'junior']
reserve_keywords = [r'\b2\b', r'-2', r'\bii\b', r'\bb\b', r'\(р\)', r'\(рез\)', r'reserve']

def has_marker(s, regexes):
    s_lower = s.lower()
    for r in regexes:
        if re.search(r, s_lower):
            return True
    return False

def get_age_marker(s):
    s_lower = s.lower()
    m = re.search(r'(?:u|до\s*|\()(\d+)', s_lower)
    if m:
        return int(m.group(1))
    if 'мол' in s_lower:
        return 'mol'
    return None

def extract_parentheses(s):
    return re.findall(r'\((.*?)\)', s)

unclassified = []
for row in aliases:
    raw = row['raw_name']
    display = row['default_display_name']
    sim = similarity(raw, display)
    
    if 0.80 <= sim < 1.0:
        raw_lower = raw.lower()
        display_lower = display.lower()
        
        classified = False
        
        # Check if it would be classified by any rule
        if any(kw in raw_lower for kw in stats_keywords) != any(kw in display_lower for kw in stats_keywords):
            classified = True
        elif has_marker(raw, gender_keywords) != has_marker(display, gender_keywords):
            classified = True
        elif get_age_marker(raw) != get_age_marker(display):
            classified = True
        elif has_marker(raw, reserve_keywords) != has_marker(display, reserve_keywords):
            classified = True
        elif extract_parentheses(raw) and extract_parentheses(display) and extract_parentheses(raw) != extract_parentheses(display):
            classified = True
        else:
            sim_raw = simplify(raw)
            sim_disp = simplify(display)
            if ('иран' in sim_raw and 'ирак' in sim_disp) or ('ирак' in sim_raw and 'иран' in sim_disp):
                classified = True
            elif ('либерия' in sim_raw and 'иберия' in sim_disp) or ('иберия' in sim_raw and 'либерия' in sim_disp):
                classified = True
            elif ('санмарино' in sim_raw and 'санмартин' in sim_disp) or ('санмартин' in sim_raw and 'санмарино' in sim_disp):
                classified = True
            elif ('перу' in sim_raw and ('порту' in sim_disp or 'осер' in sim_disp)):
                classified = True
                
        if not classified:
            unclassified.append((sim, row))

unclassified.sort(key=lambda x: x[0], reverse=True)
print(f"Total unclassified: {len(unclassified)}")
with open("unclassified.txt", "w", encoding="utf-8") as f:
    for sim, row in unclassified:
        f.write(f"{sim:.4f} | Raw: '{row['raw_name']}' | Display: '{row['default_display_name']}' | Sport: {row['sport_id']} | ID: {row['id']} | TeamID: {row['team_id']}\n")
