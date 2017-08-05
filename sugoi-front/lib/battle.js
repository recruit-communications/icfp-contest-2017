const uuid = require('uuid/v4');
const db = require('./db');

// 対戦を実行
// id: 対戦ID
// num: 対戦人数
// map: マップID
function exec({id = uuid(), num = 2, map = "sample"} = {}) {
  return pick(num).then((cids) => {
    // TODO: post rundeck
    return cids;
  }).then((cids) => {
    const params = {
      id: id,
      clients: cids,
      map: map,
    };
    return db.addBattle(params).then(() => params);
  }).then((params) => {
    return params
  });
}

// クライアントをランダム選択
// num: 選択数
// return: 選択クライアントIDs
function pick(num) {
  return db.clients().then((data) => {
    let res = [];
    for (let i=0; i<num; i++) {
      Array.prototype.push.apply(res, data.splice(data.length*Math.random()<<0, 1));
    }
    return res;
  });
}

module.exports = {
  pick: pick,
  exec: exec,
};
