#!/usr/local/bin/python3
# -*- coding: utf-8 -*- 

# battlesディレクトリにあるjsonデータを読み込み、頂点数、辺数などを数えて
# battle.jsがマップ情報を読み込めるjson形式にまとめて出力する.
# jsonが不正な形式の場合無視する

import os, json

official_data = {
        "sample.json": "Sample from the Desc",
}
 
battles = {"battles":[]}
for file_name in os.listdir("./"):
    if not file_name.endswith(".json"): continue
    with open(file_name) as f:
        try:
            jsn = json.load(f)
            num_nodes = len(jsn["settings"]["map"]["sites"])
            num_edges = len(jsn["settings"]["map"]["rivers"])
            num_punters = jsn["settings"]["punters"]
        except:
            print(file_name + " is invalid json format")
            continue
            
        if file_name in official_data:
            battles["battles"].append(
                    {
                        "filename": "../battles/" + file_name,
                        "name": "O: " + official_data[file_name],
                        "num_nodes": num_nodes,
                        "num_edges": num_edges,
                        "num_punters": num_punters,
                    }
            )
        else:
            battles["battles"].append(
                    {
                        "filename": "../battles/" + file_name,
                        "name": file_name[:-5],
                        "num_nodes": num_nodes,
                        "num_edges": num_edges,
                        "num_punters": num_punters,
                    }
            )

battles["battles"].sort(key = lambda x:x["num_nodes"])
with open("../battle-viewer/battles.json", "w") as f:
    json.dump(battles, f, ensure_ascii=False, indent=4, sort_keys=True, separators=(',', ': '))

