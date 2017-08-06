import json
import sys
import random


count_of_mines = 500

map_json = json.load(sys.stdin)

site_ids = [site['id'] for site in map_json['sites']]


new_mines = random.sample(site_ids, count_of_mines)
map_json['mines'] = new_mines

json.dump(map_json, sys.stdout)
