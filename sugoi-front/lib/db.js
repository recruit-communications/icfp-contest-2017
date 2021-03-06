const aws = require('aws-sdk');
aws.config.update({
  region: 'ap-northeast-1'
});
const db = new aws.DynamoDB.DocumentClient();
const s3 = new aws.S3();

const bucket = 'icfp2017-kst3-jp';
const clientPrefix = 'clients/';
const mapPrefix = 'maps/';

// allFlg: true時は全件取得
function dbScan(params, allFlg) {
  // ページング対応
  function scan(fulfill, reject, params, items) {
    db.scan(params, (err, data) => {
      if (err) {
        reject(err)
        return
      }
      Array.prototype.push.apply(items, data.Items);
      if (allFlg && data.LastEvaluatedKey) {
        params.ExclusiveStartKey = data.LastEvaluatedKey;
        scan(fulfill, reject, params, items);
      } else {
        fulfill(items);
      }
    });
  }
  return new Promise((fulfill, reject) => {
    scan(fulfill, reject, params, []);
  });
}

function dbGet(params) {
  return new Promise((fulfill, reject) => {
    db.get(params, (err, data) => {
      err ? reject(err) : fulfill(data);
    });
  });
}

function dbUpdate(params) {
  return new Promise((fulfill, reject) => {
    db.update(params, (err, data) => {
      err ? reject(err) : fulfill(data);
    });
  });
}

function dbPut(params) {
  return new Promise((fulfill, reject) => {
    db.put(params, (err, data) => {
      err ? reject(err) : fulfill(data);
    });
  });
}

function dbDelete(params) {
  return new Promise((fulfill, reject) => {
    db.delete(params, (err, data) => {
      err ? reject(err) : fulfill(data);
    });
  });
}

function s3List(prefix) {
  return new Promise((fulfill, reject) => {
    s3.listObjects({
      Bucket: bucket,
      Prefix: prefix
    }, (err, data) => {
      err ? reject(err) : fulfill(data.Contents.slice(1));
    });
  });
}

function s3Get(key) {
  return new Promise((fulfill, reject) => {
    s3.getObject({
      Bucket: bucket,
      Key: key
    }, (err, data) => {
      err ? reject(err) : fulfill(data);
    });
  });
}

// path -> id
function p2i(path, prefix, suffix) {
  const len = path.length - prefix.length - suffix.length;
  return path.substr(prefix.length, len);
}

// id -> path
function i2p(id, prefix, suffix) {
  return `${prefix}${id}${suffix}`;
}

module.exports = {
  bucket: bucket,
  p2i: p2i,
  i2p: i2p,
  s3List: s3List,
  s3Get: s3Get,
  punters: (params = {}) => {
    params.TableName = 'icfp-punter';
    return dbScan(params);
  },
  addPunter: ({id, created_at}) => {
    const params = {
      TableName: 'icfp-punter',
      Item: {
        id: id,
        created_at: created_at
      }
    };
    return dbPut(params);
  },
  maps: (params = {}) => {
    params.TableName = 'icfp-map';
    return dbScan(params);
  },
  addMap: ({id, created_at, punter_num = 2, info}) => {
    const params = {
      TableName: 'icfp-map',
      Item: {
        id: id,
        created_at: created_at,
        info: info,
        punter_num: punter_num,
      }
    };
    return dbPut(params);
  },
  games: (params = {}) => {
    params.TableName = 'icfp-game';

    if (params.map_id) {
      params.ExpressionAttributeValues = {':m': params.map_id};
      params.FilterExpression = 'map_id = :m';
    }
    return dbScan(params, params.all);
  },
  addGame: ({id, league_id, created_at = (new Date).getTime(), punter_ids, map_id, job = {}, results = null}) => {
    const params = {
      TableName: 'icfp-game',
      Item: {
        id: id,
        league_id: league_id,
        created_at: created_at,
        punter_ids: punter_ids,
        map_id: map_id,
        job: job,
      }
    };
    if (results) params.Item.results = results;
    return dbPut(params);
  },
  updateGame: ({id, created_at, results, job}) => {
    const params = {
      TableName: 'icfp-game',
      ExpressionAttributeNames: {
        '#R': 'results',
        '#J': 'job',
      },
      ExpressionAttributeValues: {
        ':r': results,
        ':j': job
      },
      UpdateExpression: 'SET #R = :r',
      Key: {
        id: id,
        created_at: created_at
      }
    };
    return dbUpdate(params);
  },
  deletePunter: (id) => {
    s3.deleteObject({
      Bucket: "icfp2017-kst3-jp", 
      Key: "clients/" + id + ".tar.gz"
    }, function(err, data) {
      if (err) console.log(err, err.stack);
      else     console.log(data);
    });
    const params = {
      TableName: 'icfp-punter',
      Key: {
        id: id
      }
    }
    return dbDelete(params);
  }
};
