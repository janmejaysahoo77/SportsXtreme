import sys
with open('patch_homescreen2.py', 'r', encoding='utf-8') as f:
    content = f.read()
idx = content.find('drawer_methods = """')
end_idx = content.find('"""', idx + 20)
methods = content[idx:end_idx]
print('Delta:', methods.count('{') - methods.count('}'))
