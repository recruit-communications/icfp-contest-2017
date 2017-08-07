#!/usr/bin/python
# -*- coding: utf-8 -*-
'''
mapごとの勝率マトリックスを生成するスクリプト

./matrix/以下にcsvを作る
勝率ではなく勝数を見たい場合はrateの代わりにcountを出力すると良い

usage: ./matrix.py ['%Y-%m-%d %H:%M:%S']
例) ./matrix.py "2017-08-07 13:00:00"
'''

import collections
import commands
from datetime import datetime
import os
from pytz import timezone
import shutil
import sys

zero = datetime(1970, 1, 1, 0, 0, 0, tzinfo=timezone('UTC'))
origin = zero

if len(sys.argv) > 1:
  try: 
    origin = datetime.strptime(sys.argv[1], '%Y-%m-%d %H:%M:%S').replace(tzinfo=timezone('Asia/Tokyo'))
  except ValueError:
    pass

timestamp = int((origin - zero).total_seconds()) * 1000
print "origin timestamp: %d"%timestamp

os.system("curl -s \"http://13.112.208.142:3000/game/list?count=999999&all=1\" > /tmp/gamelist.txt")
shutil.rmtree("./matrix")
os.makedirs("./matrix")
maplist = [tuple(line.split("\t")) for line in commands.getoutput("curl -s http://13.112.208.142:3000/map/list | jq -r '.[] | [.id, .punter_num] | @tsv'").split("\n")]
puntlist = set(commands.getoutput("curl -s http://13.112.208.142:3000/punter/list | jq -r '.[].id'").split("\n"))

def cumulate(count, win, tsv):
  for line in tsv:
    ary = line.split()
    t, id, punters = ary[0], ary[1], ary[2:]
    if int(t) < timestamp: continue
    for i in xrange(len(punters)):
      for j in xrange(i+1, len(punters)):
        if punters[i] not in puntlist or punters[j] not in puntlist: continue
        count[(punters[i], punters[j])] += 1
        win[punters[i]] += 1
        win[punters[j]] += 0

  return (count, win)

def output(count, win, name):
  rate = collections.defaultdict(float)
  ratesum = collections.defaultdict(float)
  punters = win.keys()
  denom = {}

  for p1 in punters:
    for p2 in punters:
      denom[(p1, p2)] = count[(p1, p2)] + count[(p2, p1)]
      denom[(p2, p1)] = count[(p1, p2)] + count[(p2, p1)]
      rate[(p1, p2)] = 1.0 * count[(p1, p2)] / denom[(p1, p2)] if denom[(p1, p2)] > 0 else 0.5
      rate[(p2, p1)] = 1.0 * count[(p2, p1)] / denom[(p2, p1)] if denom[(p2, p1)] > 0 else 0.5
      ratesum[p1] += rate[(p1, p2)]
      ratesum[p2] += rate[(p2, p1)]

  punters = [item[0] for item in sorted(ratesum.items(), key=lambda x: -x[1])]

  ans = ["," + ",".join(punters)]
  for p1 in punters:
    ary = [p1]
    for p2 in punters:
      ary.append(str(rate[(p1, p2)]) if denom[(p1, p2)] > 0 else '-')
    ans.append(",".join(ary))

  with open("./matrix/%s.csv"%name, 'w') as f:
    f.write("\n".join(ans))

  ans = ["," + ",".join(punters)]
  for p1 in punters:
    ary = [p1]
    for p2 in punters:
      ary.append(str(count[(p1, p2)]) if denom[(p1, p2)] > 0 else '-')
    ans.append(",".join(ary))

  with open("./matrix/_%s.csv"%name, 'w') as f:
    f.write("\n".join(ans))


subtotal = {}
for i in [2,4,8,16]:
  subtotal[i] = {}
  subtotal[i]["count"] = collections.defaultdict(int)
  subtotal[i]["win"] = collections.defaultdict(int)

for mapname, punter_num in maplist:
  tsv = commands.getoutput("cat /tmp/gamelist.txt | jq -r '.[] | if .job.status == \"success\" and .map_id == \"%s\" then . else empty end | (.created_at | tostring) + \"\t\" + .id + \"\t\" + (.results | sort_by(.score) | reverse | map(.punter) | @tsv)'" % mapname).split("\n")

  if tsv[0] == '': continue

  count = collections.defaultdict(int)
  win = collections.defaultdict(int)

  count, win = cumulate(count, win, tsv)

  if punter_num <= 2:
    subtotal[2]["count"], subtotal[2]["win"] = cumulate(subtotal[2]["count"], subtotal[2]["win"], tsv)
  elif punter_num <= 4:
    subtotal[2]["count"], subtotal[2]["win"] = cumulate(subtotal[4]["count"], subtotal[4]["win"], tsv)
  elif punter_num <= 8:
    subtotal[2]["count"], subtotal[2]["win"] = cumulate(subtotal[8]["count"], subtotal[8]["win"], tsv)
  else:
    subtotal[2]["count"], subtotal[2]["win"] = cumulate(subtotal[16]["count"], subtotal[16]["win"], tsv) 

  output(count, win, mapname)

print subtotal[2]
for i in subtotal.keys():
  output(subtotal[2]["count"], subtotal[2]["win"], "subtotal%d"%i)
