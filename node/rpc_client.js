var rpc = require('node-json-rpc');

var options = {
  // int port of rpc server, default 5080 for http or 5433 for https
  port: 5080,
  // string domain name or ip of rpc server, default '127.0.0.1'
  host: '127.0.0.1',
  // string with default path, default '/'
  path: '/',
  // boolean false to turn rpc checks off, default true
  strict: true
};

// Create a server object with options
var client = new rpc.Client(options);

client.call(
  {"jsonrpc": "2.0", "method": "waylay_rpc", "params": [new Buffer("1+3").toString('base64')], "id": 2},
  function (err, res) {
    // Did it all work ?
    if (err) { console.log(err); }
    else { console.log(res); }
  }
);
