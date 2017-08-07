# 使い方
```python
# python3でも実行できるが遅いのでpypy3推奨. sparse degreeが大きいほど辺が疎になる.
 
pypy3 ./random_plane_grapy.py <num nodes> <num mines> <sparse degree>
pypy3 ./random_plane_grapy.py 500 30 70
 
# => 出来上がったグラフを標準出力、またファイル(randomPlain_N<num_nodes>_M<num mines>_S<sparse degree>.json)に保存
```

2000頂点くらいまではそれなりの速度で生成できます。  
グラフの形はサンプルの街の地図に近い感じになります。  
ソース先頭にパラメータをまとめているので、グラフの形を更に調整したいときは弄ってください。
