"""
Download raw KyleBing data (json/ + json-sentence/) and convert to structured dictionary JSON.
Structured format:
{
  "word": "abandon",
  "usPhonetic": "əˈbændən",
  "ukPhonetic": "əˈbændən",
  "meanings": [
    {"pos": "v", "def": "放弃；抛弃", "examples": [{"en": "...", "cn": "..."}]}
  ],
  "phrases": [{"phrase": "with abandon", "translation": "恣意地"}],
  "difficulty": 3
}
"""
import urllib.request, urllib.parse, json, os, time

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
VOCAB_DIR = os.path.dirname(SCRIPT_DIR)
RAW_DIR = os.path.join(SCRIPT_DIR, 'raw')
os.makedirs(RAW_DIR, exist_ok=True)

# ======== CONFIG ========
JSON_BASE = 'https://raw.githubusercontent.com/KyleBing/english-vocabulary/master/json/'
SENT_BASE = 'https://raw.githubusercontent.com/KyleBing/english-vocabulary/master/json_original/json-sentence/'

# Categories: (label, json_file, sentence_files[], difficulty)
CATEGORIES = [
    ('CET4', '3-CET4-顺序.json', ['CET4_1.json','CET4_2.json','CET4_3.json','CET4luan_1.json','CET4luan_2.json'], 1),
    ('CET6', '4-CET6-顺序.json', ['CET6_1.json','CET6_2.json','CET6_3.json','CET6luan_1.json'], 3),
    ('考研', '5-考研-顺序.json', ['KaoYan_1.json','KaoYan_2.json','KaoYan_3.json','KaoYanluan_1.json'], 3),
    ('TOEFL', '6-托福-顺序.json', ['TOEFL_2.json','TOEFL_3.json'], 4),
    ('GRE', '7-SAT-顺序.json', ['GRE_2.json','GRE_3.json'], 5),
    ('IELTS', None, ['IELTS_2.json','IELTS_3.json','IELTSluan_2.json'], 4),
    ('商务英语', None, ['BEC_2.json','BEC_3.json'], 3),
]

# ======== DOWNLOAD ========
def download(url, fname):
    path = os.path.join(RAW_DIR, fname)
    if os.path.exists(path):
        return path
    print(f'  DL: {fname} ... ', end='', flush=True)
    try:
        urllib.request.urlretrieve(url, path)
        print(f'{os.path.getsize(path):,}b')
    except Exception as e:
        print(f'FAILED: {e}')
        return None
    return path

print('=== Downloading json/ files ===')
for label, json_f, sent_fs, diff in CATEGORIES:
    if json_f:
        url = JSON_BASE + urllib.parse.quote(json_f)
        download(url, json_f)

print('=== Downloading json-sentence/ files ===')
for label, json_f, sent_fs, diff in CATEGORIES:
    for f in sent_fs:
        url = SENT_BASE + f
        download(url, f)

# ======== CONVERT ========
print('\n=== Converting ===')

def load_json(fname):
    path = os.path.join(RAW_DIR, fname)
    if not os.path.exists(path):
        return None
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def build_meaning_from_simple(tr):
    """Convert simple translation {type, translation} to Meaning dict."""
    return {
        'pos': tr.get('type', ''),
        'def': tr.get('translation', ''),
        'examples': []
    }

def convert_category(label, json_f, sent_fs, difficulty):
    """Merge json/ + json-sentence/ into structured dictionary format."""
    
    # Step 1: Build base from json/ file (has most words, merge duplicates)
    word_map = {}  # word_lower -> dict
    
    if json_f:
        data = load_json(json_f)
        if data:
            for item in data:
                w = item.get('word', '').strip().lower()
                if not w:
                    continue
                if w in word_map:
                    # Merge: same word appears multiple times with different translations
                    existing = word_map[w]
                    new_meanings = [build_meaning_from_simple(t) for t in (item.get('translations') or [])]
                    # Avoid exact duplicate meanings
                    existing_defs = {m['pos'] + '|' + m['def'] for m in existing['meanings']}
                    for nm in new_meanings:
                        key = nm['pos'] + '|' + nm['def']
                        if key not in existing_defs:
                            existing['meanings'].append(nm)
                            existing_defs.add(key)
                    # Merge phrases
                    existing_ph = {p['phrase'] for p in existing['phrases']}
                    for p in (item.get('phrases') or []):
                        ph = p.get('phrase', '')
                        if ph and ph not in existing_ph:
                            existing['phrases'].append({'phrase': ph, 'translation': p.get('translation', '')})
                            existing_ph.add(ph)
                else:
                    translations = item.get('translations', [])
                    word_map[w] = {
                        'word': item.get('word', ''),
                        'usPhonetic': '',
                        'ukPhonetic': '',
                        'meanings': [build_meaning_from_simple(t) for t in translations],
                        'phrases': [{'phrase': p.get('phrase',''), 'translation': p.get('translation','')}
                                   for p in (item.get('phrases') or [])],
                        'difficulty': difficulty
                    }
    
    # Step 2: Enrich from json-sentence files (phonetics, sentences, more meanings)
    for sf in sent_fs:
        data = load_json(sf)
        if not data:
            continue
        for item in data:
            w = item.get('word', '').strip().lower()
            if not w:
                continue
            
            if w not in word_map:
                # New word from sentence file
                translations = item.get('translations', [])
                sentences = item.get('sentences', [])
                examples = [{'en': s.get('sentence',''), 'cn': s.get('translation','')} for s in sentences]
                
                meanings = []
                for t in translations:
                    m = build_meaning_from_simple(t)
                    if not meanings and examples:
                        m['examples'] = examples[:3]
                    meanings.append(m)
                
                word_map[w] = {
                    'word': item.get('word', ''),
                    'usPhonetic': item.get('us', '') or '',
                    'ukPhonetic': item.get('uk', '') or '',
                    'meanings': meanings,
                    'phrases': [{'phrase': p.get('phrase',''), 'translation': p.get('translation','')}
                               for p in (item.get('phrases') or [])],
                    'difficulty': difficulty
                }
            else:
                # Enrich existing word
                existing = word_map[w]
                
                # Add phonetics
                if not existing['usPhonetic'] and item.get('us'):
                    existing['usPhonetic'] = item.get('us', '')
                if not existing['ukPhonetic'] and item.get('uk'):
                    existing['ukPhonetic'] = item.get('uk', '')
                
                # Merge meanings (avoid duplicates)
                existing_defs = {m['pos'] + '|' + m['def'] for m in existing['meanings']}
                for t in (item.get('translations') or []):
                    nm = build_meaning_from_simple(t)
                    key = nm['pos'] + '|' + nm['def']
                    if key not in existing_defs:
                        existing['meanings'].append(nm)
                        existing_defs.add(key)
                
                # Add sentences to first meaning that lacks examples
                sentences = item.get('sentences', [])
                if sentences:
                    for m in existing['meanings']:
                        if not m.get('examples'):
                            m['examples'] = []
                        if len(m['examples']) < 3:
                            for s in sentences:
                                if len(m['examples']) >= 3:
                                    break
                                m['examples'].append({
                                    'en': s.get('sentence', ''),
                                    'cn': s.get('translation', '')
                                })
                            break
                
                # Merge phrases
                existing_ph = {p['phrase'] for p in existing['phrases']}
                for p in (item.get('phrases') or []):
                    ph = p.get('phrase', '')
                    if ph and ph not in existing_ph:
                        existing['phrases'].append({'phrase': ph, 'translation': p.get('translation', '')})
                        existing_ph.add(ph)
    
    # Convert to list, ensure word field preserves original case
    result = list(word_map.values())
    
    # Sort alphabetically
    result.sort(key=lambda x: x['word'].lower())
    
    return result

for label, json_f, sent_fs, difficulty in CATEGORIES:
    print(f'  {label}: ', end='', flush=True)
    items = convert_category(label, json_f, sent_fs, difficulty)
    
    # Determine output filename
    fname_map = {
        'CET4': 'cet4.json', 'CET6': 'cet6.json', '考研': 'kaoyan.json',
        'TOEFL': 'toefl.json', 'GRE': 'gre.json', 'IELTS': 'ielts.json',
        '商务英语': 'biz.json'
    }
    out_name = fname_map.get(label, label + '.json')
    out_path = os.path.join(VOCAB_DIR, out_name)
    
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(items, f, ensure_ascii=False, separators=(',', ':'))
    
    # Stats
    has_us = sum(1 for i in items if i['usPhonetic'])
    has_uk = sum(1 for i in items if i['ukPhonetic'])
    has_ex = sum(1 for i in items if any(m.get('examples') for m in i['meanings']))
    has_ph = sum(1 for i in items if i['phrases'])
    print(f'{len(items):,} words | US:{has_us} UK:{has_uk} examples:{has_ex} phrases:{has_ph}')

# ======== SUMMARY ========
total = 0
for f in os.listdir(VOCAB_DIR):
    if f.endswith('.json'):
        with open(os.path.join(VOCAB_DIR, f), 'r', encoding='utf-8') as fh:
            n = len(json.load(fh))
        total += n
        print(f'  {f}: {n:,} words')
print(f'\nTotal: {total:,} words')
print('Done!')
