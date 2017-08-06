# online bridge(日本橋)

`python3 bridge.py '起動コマンド 起動オプション' YourName port`

`python3 bridge.py 'java PassOnlyAI' Kenkoooo 9005`

# offline bridge(板橋)

`python3 bridge.py '起動コマンド 起動オプション' YourName`

`python3 bridge.py 'java PassOnlyAI' Kenkoooo`

# offline bridge(新板橋ジャンクション)

ジャンクションモードにするには、 bridge.py の先頭を `JUNCTION = True` にする

`python3 bridge.py YourName '起動コマンド1 起動オプション1' '起動コマンド2 起動オプション2' '起動コマンド3 起動オプション3'`

`python3 bridge.py Kenkoooo 'java PassOnlyAI' 'java PassOnlyAI' 'java PassOnlyAI'`

`def decision(bridge):` の内部で、超点数やプレイヤー数などで、プログラムを切り分ける(ここは提出前に調整する)
