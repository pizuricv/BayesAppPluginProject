var rpc = require('node-json-rpc');
var vm = require('vm');
sandbox = {};

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
  if (para.length === 1) {
    try {
          var _script = new Buffer(para[0], 'base64').toString('ascii');
          console.log("executing script: " + _script);
          var script = vm.createScript(_script,'myfile.vm');
          result = script.runInThisContext(sandbox);
          console.log('result: '+result);
       } catch(err){
         console.log("error: " + err.message);
         error = { code: -32603, message: err.message };
     }
   } 
   else {
      error = { code: -32602, message: "Invalid params" };
   }
  callback(error, result);
});

// Start the server
serv.start(function (error) {
  if (error) throw error;
  else console.log('Server running ...');
});
