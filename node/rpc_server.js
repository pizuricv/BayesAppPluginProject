var rpc = require('node-json-rpc');
var request = require('request');
var cheerio = require('cheerio');
var dust = require('dustjs-linkedin');
var Promise = require('promise');
var vm = require('vm');
var fs = require('fs');
sandbox = {cheerio:cheerio, request:request, console:console};
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

var serv = new rpc.Server(options);

function handleError(err, code, callback) {
  console.log("error " + err);
  var result;
  errorTotal ++;
  error = { code: code, message: err.message};
  callback(error, result);
}

function runScript (content, options, callback) {
  countTotal ++;
  console.log("runScript script: " + content);
  sandbox.send = callback;
  var promise = new Promise(function(resolve, reject) {
    try {
     // var _script = new Buffer(content, 'base64').toString('ascii');
     _script = content;
      if(options){
        console.log("compile options: " + options);
        var compiled =  dust.compile(_script, "dust");
        dust.loadSource(compiled);
        dust.render("dust", options, function(err, out) {
          console.log(out);
          _script = out;
        });
      }
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
    console.log("Total number of requests:" +countTotal + ", total number of errors " + errorTotal);
  }, function(err) {
    handleError(err, -32603, callback);
  });
}

serv.addMethod('waylay_rpc', function (para, callback) {
  countTotal ++;
  console.log("recieved RPC call" );
  if (para.length > 0) {
    var name = para[0];
    runScript(name, null, callback);
  } else {
    handleError(new Error("Invalid params "), 32602, callback);
  }
});

//para[0] script name, para[1] options - using template
serv.addMethod('execute_sensor', function(para, callback){
  countTotal ++;
  var error, result;
  if (para.length >0) {
    var name = para[0];
    console.log("execute sensor: " + name);
    var dataTemp, data;
    try{
      dataTemp = fs.readFileSync('./config.json');
      data = JSON.parse(dataTemp);
      var sensor = data[name];
      if(sensor === "undefined"){
        handleError(new Error("Invalid sensor name: " +name ), 32602, callback);
      }
      var content = data[name].script;
      var options = para[1];
      runScript(content, options, callback);
    } catch(err){
      handleError(err, -32602, callback);
   }
 }
});

//para[0]: sensor name, para[1]: script  content, para[2] metadata
serv.addMethod('register_sensor', function(para, callback){
  countTotal ++;
  var error, result;
  if (para.length === 3) {
    try {
      var name = para[0];
      var script = para[1];
      console.log("receive sensor: " + name);
      var metadata = para[2];
      var dataTemp, data;
      try{
        dataTemp = fs.readFileSync('./sensors.json');
        data = JSON.parse(dataTemp);
      } catch(err){
        console.log('Error loading configration');
        data = {};
      }
      data[name] = {
                    script: script,
                    medatada: metadata
                  };
      console.log("saving ... " + data);
      fs.writeFile('./sensors.json', JSON.stringify(data), function (err) {
      if (err) {
        handleError(err, -32000, callback);
        } else{
          console.log('Configuration saved successfully.');
          callback(error, data);
        }
      });
    } catch(err){
      handleError(err, -32000, callback);
    }
   } else {
    handleError(new Error("Invalid params "), -32602, callback);
   }
});

serv.addMethod('sensors', function (para, callback) {
  countTotal ++;
  console.log('get list of all sensors');
  var error, result, array = [];
  try{
        dataTemp = fs.readFileSync('./sensors.json');
        data = JSON.parse(dataTemp);
        for(index in data){
          console.log('found sensor ' + index);
          data[index].medatada.name = index;
          array.push(data[index].medatada);
          callback(null, array);
        }
      } catch(err){
        handleError(err, -32603, callback)
      }
});

serv.addMethod('info', function (para, callback) {
  countTotal ++;
  callback(null, {total: countTotal, errors: errorTotal});
});

// Start the server
serv.start(function (error) {
  if (error) throw error;
  else console.log('Server running ...');
});
