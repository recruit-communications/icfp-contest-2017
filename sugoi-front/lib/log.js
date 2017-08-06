// 対戦ログ周りの処理(+ スコア周りも？)

const request = require('request');

// 対戦ログ文字列から結果を取得
function parseLog(str) {
  const json = JSON.parse(str.split("\n").splice(-2, 1)[0].substr('RECV '.length));
  // ログ末尾に結果が含まれていなければ空で返す
  return json.stop ? json.stop.scores : [];
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

// 作成時のqueueURLからジョブURLを取得
function getBuildUrl(queueUrl) {
  return new Promise((fulfill, reject) => {
    request.get({uri: queueUrl}, (err, res, body) => {
      if (err || res.statusCode >= 400) {
        reject(err || res);
        return;
      }
      fulfill(JSON.parse(body).executable.url);
    });
  });
}

module.exports = {
  parseLog: parseLog,
  parseMap: parseMap,
  getBuildUrl: getBuildUrl,
};
