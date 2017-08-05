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

module.exports = {
  bucket: bucket,
  clients: () => {
    return s3List(clientPrefix);
  },
  maps: () => {
    return s3List(mapPrefix);
  },
  battles: () => {
    params = {
      TableName: 'icpf2017-battle'
    };
    return dbScan(params).then((data) => {
      return data;
    });
  }
};
