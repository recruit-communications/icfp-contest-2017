// 対戦ログ周りの処理(+ スコア周りも？)

// 対戦ログ文字列から結果を取得
function parse(str) {
  const json = JSON.parse(str.split("\n").splice(-2, 1)[0].substr('RECV '.length));
  return json.stop.scores;
}

module.exports = {
  parse: parse,
};
