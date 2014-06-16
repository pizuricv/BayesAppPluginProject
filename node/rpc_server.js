var rpc = require('node-json-rpc');
var req = require("request");
var vm = require('vm');
var Promise = require('promise');
sandbox = {request:req, console:console};
var countTotal = 0;
var errorTotal = 0;

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
var serv = new rpc.Server(options);

serv.addMethod('waylay_rpc', function (para, callback) {
  var error, result;
  console.log("recieved request" );
//IMPORTANT: script will use the callback to send the result to the sensor
sandbox.send = callback;
if (para.length === 1) {
  var promise = new Promise(function(resolve, reject) {
    try {
      var _script = new Buffer(para[0], 'base64').toString('ascii');
      console.log("executing script: " + _script);
      var script = vm.createScript(_script,'myfile.vm');
      script.runInThisContext(sandbox);
      console.log('script executed');
      resolve();
    } catch(err){
     reject(err);
   }
 });
  promise.then(function(result) {
    countTotal ++;
    console.log("Total number of requests:" +countTotal + ", total number of errors " + errorTotal);
  }, function(err) {
    errorTotal ++;
    console.log(err);
    error = { code: -32603, message: err.message };
    callback(error, result);
    console.log("Total number of requests:" +countTotal + ", total number of errors " + errorTotal);
  });
}
else {
  error = { code: -32602, message: "Invalid params" };
  errorTotal ++;
  callback(error, result);
  console.log("Total number of requests:" +countTotal + ", total number of errors " + errorTotal);
}
});

// Start the server
serv.start(function (error) {
  if (error) throw error;
  else console.log('Server running ...');
});
