// 対戦ログ周りの処理(+ スコア周りも？)

// 対戦ログ文字列から結果を取得
function parseLog(str) {
  const json = JSON.parse(str.split("\n").splice(-2, 1)[0].substr('RECV '.length));
  return json.stop.scores;
}

// マップJSONから情報を取得
function parseMap(str) {
  const json = JSON.parse(str);
  return {
    sites: json.sites.length,
    mines: json.mines.length,
    rivers: json.rivers.length,
  };
}

module.exports = {
  parseLog: parseLog,
  parseMap: parseMap,
};
