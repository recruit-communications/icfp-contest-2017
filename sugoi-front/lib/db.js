const aws = require('aws-sdk');
aws.config.update({
  region: 'ap-northeast-1'
});
const db = new aws.DynamoDB.DocumentClient();
const s3 = new aws.S3();

const bucket = 'icfp2017-kst3-jp';
const clientPrefix = 'clients/';
const mapPrefix = 'maps/';

function dbScan(params) {
  return new Promise((fulfill, reject) => {
    db.scan(params, (err, data) => {
      err ? reject(err) : fulfill(data.Items);
    });
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
    return dbScan(params);
  },
  addGame: ({id, league_id, created_at = (new Date).getTime(), punter_ids, map_id, job}) => {
    const params = {
      TableName: 'icfp-game',
      Item: {
        id: id,
        league_id: league_id,
        created_at: created_at,
        punter_ids: punter_ids,
        map: map_id,
        job: job,
      }
    };
    return dbPut(params);
  },
  updateGame: ({id, created_at, results}) => {
    const params = {
      TableName: 'icfp-game',
      ExpressionAttributeNames: {
        '#R': 'results'
      },
      ExpressionAttributeValues: {
        ':r': results
      },
      UpdateExpression: 'SET #R = :r',
      Key: {
        id: id,
        created_at: created_at
      }
    };
    return dbUpdate(params);
  },
};
