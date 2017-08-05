const uuid = require('uuid/v4');

// 対戦を実行
// id: 対戦ID
// map: マップID
// num: 対戦人数
function exec({id = uuid(), num = 2, map = "sample"}) {
  console.log(id, num, map);
}

module.exports = {
};
