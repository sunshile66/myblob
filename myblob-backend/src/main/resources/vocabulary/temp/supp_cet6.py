"""Supplement CET6 with unique words from TOEFL + kaoyan to reach ~5500."""
import json, os

VOCAB_DIR = os.path.dirname(os.path.abspath(__file__))  # temp/
VOCAB_DIR = os.path.dirname(VOCAB_DIR)  # vocabulary/

# Load CET6
with open(os.path.join(VOCAB_DIR, 'cet6.json'), 'r', encoding='utf-8') as f:
    cet6 = json.load(f)
cet6_words = {item['word'].lower() for item in cet6}
print(f'CET6 base: {len(cet6):,} unique words')

# Supplement from TOEFL
with open(os.path.join(VOCAB_DIR, 'toefl.json'), 'r', encoding='utf-8') as f:
    toefl = json.load(f)

added_toefl = 0
for item in toefl:
    w = item['word'].lower()
    if w not in cet6_words:
        cet6.append(item)
        cet6_words.add(w)
        added_toefl += 1
        if len(cet6) >= 5500:
            break

print(f'From TOEFL: +{added_toefl} = {len(cet6):,}')

# Supplement from kaoyan if still short
if len(cet6) < 5400:
    with open(os.path.join(VOCAB_DIR, 'kaoyan.json'), 'r', encoding='utf-8') as f:
        kaoyan = json.load(f)
    added_ky = 0
    for item in kaoyan:
        w = item['word'].lower()
        if w not in cet6_words:
            cet6.append(item)
            cet6_words.add(w)
            added_ky += 1
            if len(cet6) >= 5500:
                break
    print(f'From kaoyan: +{added_ky} = {len(cet6):,}')

# Sort
cet6.sort(key=lambda x: x['word'].lower())

# Save
with open(os.path.join(VOCAB_DIR, 'cet6.json'), 'w', encoding='utf-8') as f:
    json.dump(cet6, f, ensure_ascii=False, separators=(',', ':'))

print(f'Final CET6: {len(cet6):,} words')
