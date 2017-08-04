#!/usr/local/bin/python3
# -*- coding: utf-8 -*- 

# mapsディレクトリにあるjsonデータを読み込み、頂点数、辺数を数えて
# viewer.jsがマップ情報を読み込めるjson形式にまとめて出力する.
# jsonが不正な形式の場合無視する

import os, json

official_data = {
        "sample.json": "Sample from the Task Description",
        "lambda.json": "Lambda",
        "Sierpinski-triangle.json": "Sierpinski triangle",
        "circle.json": "Circle", 
        "randomMedium.json": "Random1",
        "randomSparse.json": "Random2",
        "tube.json": "London Tube",
        "oxford-center-sparse.json": "Oxford City Centre",
        "oxford2-sparse-2.json": "Oxford", 
        "edinburgh-sparse.json": "Edinburgh",
        "boston-sparse.json": "Boston",
        "nara-sparse.json": "Nara",
        "van-city-sparse.json": "Vancouver",
        "gothenburg-sparse.json": "Gothenburg",
}
 
maps = {"maps":[]}
for file_name in os.listdir("./"):
    if not file_name.endswith(".json"): continue
    with open(file_name) as f:
        try:
            jsn = json.load(f)
            num_nodes = len(jsn["sites"])
            num_edges = len(jsn["rivers"])
        except:
            # 形式不正なのでスルー
            continue
            
        if file_name in official_data:
            maps["maps"].append(
                    {
                        "filename": "../maps/" + file_name,
                        "name": "O: " + official_data[file_name],
                        "num_nodes": num_nodes,
                        "num_edges": num_edges,
                    }
            )
        else:
            maps["maps"].append(
                    {
                        "filename": "../maps/" + file_name,
                        "name": file_name[:-4],
                        "num_nodes": num_nodes,
                        "num_edges": num_edges,
                    }
            )

maps["maps"].sort(key = lambda x:x["num_nodes"])
with open("../graph-viewer/maps.json", "w") as f:
    json.dump(maps, f, ensure_ascii=False, indent=4, sort_keys=True, separators=(',', ': '))

