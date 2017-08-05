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

// path -> id
function p2i(path, prefix, suffix) {
  const len = path.length - prefix.length - suffix.length;
  return path.substr(prefix.length, len);
}

module.exports = {
  bucket: bucket,
  p2i: p2i,
  s3List: s3List,
  punters: (params = {}) => {
    params.TableName = 'icfp-punter';
    return dbScan(params);
  },
  addPunter: ({id, created_at, punter_num = 2}) => {
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
  addMap: ({id, created_at, punter_num}) => {
    const params = {
      TableName: 'icfp-map',
      Item: {
        id: id,
        created_at: created_at,
        punter_num: punter_num
      }
    };
    return dbPut(params);
  },
  games: (params = {}) => {
    params.TableName = 'icfp-game';
    return dbScan(params);
  },
  addGame: ({id, league_id, created_at, punters, map}) => {
    const params = {
      TableName: 'icpf-game',
      Item: {
        id: id,
        league_id: league_id,
        created_at: created_at,
        punters: punters,
        map: map
      }
    };
    return dbPut(params);
  },
  updateGame: ({id, league_id, created_at, punters, map}) => {
    const params = {
      TableName: 'icpf-game',
      ExpressionAttributeNames: {
      },
      ExpressionAttributeValues: {
      },
      UpdateExpression: '',
      Key: {
        id: id,
        created_at: created_at
      },
      Item: {
        league_id: league_id,
        punters: punters,
        map: map
      }
    };
    return dbPut(params);
  },
};
