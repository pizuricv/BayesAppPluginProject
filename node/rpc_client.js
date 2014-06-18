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
console.log('call 2: waylay_rpc');
client.call (
  {"jsonrpc": "2.0", "method": "waylay_rpc", "params": ["var error, value = {observedState:\"hello\",rawData:{}};sandbox.send(null,value);"], "id": 2},
  function (err, res){
    // Did it all work ?
    if (err) { console.log(err); }
    else { console.log(res); }
  });
console.log('call 3: register_sensor');
client.call (
  {"jsonrpc": "2.0", "method": "register_sensor", "params": ["sensorX", "var error, value = { observedState: \"hello\", rawData: \"hello2\"};sandbox.send(null, value);", {author
  : "Veselin" } ], "id": 3},
  function (err, res){
    // Did it all work ?
    if (err) { console.log(err); }
    else { console.log(res); }
  });

setTimeout(function(){
console.log('call 4: execute_sensor');
client.call (
  {"jsonrpc": "2.0", "method": "execute_sensor", "params": ["sensorX"], "id": 4},
  function (err, res){
    // Did it all work ?
    if (err) { console.log(err); }
    else { console.log(res); }
  });
}, 1000);

console.log('call 5: info');
client.call (
  {"jsonrpc": "2.0", "method": "info", "params": [], "id": 5},
  function (err, res){
    // Did it all work ?
    if (err) { console.log(err); }
    else { console.log(res); }
  }
);

setTimeout(function(){
console.log('call 6: sensors');
client.call (
  {"jsonrpc": "2.0", "method": "sensors", "params": [], "id": 6},
  function (err, res){
    // Did it all work ?
    if (err) { console.log(err); }
    else { console.log(res); }
  }
);
}, 1500);
