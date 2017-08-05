const http = require ('http');

exports.handler = (event, context, callback) => {
  http.get('http://13.112.208.142:3000/battle/execute', (res) => {
    context.done(null, {});
  });
};
