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
      err ? reject(err) : fulfill(data.Contents.slice(1).map((v) => v.Key));
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
  clients: () => {
    return s3List(clientPrefix).then((list) => {
      return list.map((p) => p2i(p, clientPrefix, '.tar.gz'))
    });
  },
  maps: () => {
    return s3List(mapPrefix).then((list) => {
      return list.map((p) => p2i(p, mapPrefix, '.json'))
    });
  },
  battles: () => {
    const params = {
      TableName: 'icpf2017-battle'
    };
    return dbScan(params);
  },
  addBattle: ({id, clients, map}) => {
    const params = {
      TableName: 'icpf2017-battle',
      Item: {
        id: id,
        clients: clients,
        map: map
      }
    };
    return dbPut(params);
  },
};
