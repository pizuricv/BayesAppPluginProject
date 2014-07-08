var rpc = require('node-json-rpc');
var request = require('request');
var cheerio = require('cheerio');
var Promise = require('promise');
var vm = require('vm');
var fs = require('fs');
var sandbox = {cheerio:cheerio, request:request, console:console};
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
  console.log('error ' + err);
  var result;
  errorTotal ++;
  var error = { code: code, message: err.message};
  callback(error, result);
}

function runScript (content, options, callback) {
  countTotal ++;
  console.log('runScript script: ' + content);
  sandbox.send = callback;
  var promise = new Promise(function(resolve, reject) {
    try {
     // var _script = new Buffer(content, 'base64').toString('ascii');
     _script = content;
      if(options){
        console.log('options: ' + options);
        if (typeof options === 'string')
          options = JSON.parse(options);
        sandbox.options = options;
      } else {
        if(sandbox.options !== undefined)
          delete sandbox.options;
      }
      console.log('executing script: ' + _script);
      var context = vm.createContext(sandbox);
      vm.runInContext(_script, context, 'myfile.vm');
      console.log('script executed');
      resolve();
      } catch(err){
       reject(err);
     }
   });
  promise.then(function(result) {
    console.log('Total number of requests: ' +countTotal + ', total number of errors ' + errorTotal);
  }, function(err) {
    handleError(err, -32603, callback);
  });
}

function execute_plug(type, name, options, callback){
  console.log('execute plug: ' + name);
  countTotal ++;
  var error, result;
  var dataTemp, data;
  try{
    if(type === 'sensor')
      dataTemp = fs.readFileSync('./sensors.json');
    else if(type === 'action')
      dataTemp = fs.readFileSync('./actions.json');
    else
      throw new Error('Invalid plug type : '  + type);
    data = JSON.parse(dataTemp);
    if(data[name] === undefined)
      throw new Error('Invalid plug name: '+ name);
    if(data[name].file !== undefined)
      data[name].script = fs.readFileSync(data[name].file);
    var content = data[name].script;
    runScript(content, options, callback);
  } catch(err){
    handleError(err, -32602, callback);
  }
}

function listPlugs(type, callback) {
  console.log('list plugs: ' + type);
  countTotal ++;
  var error, result, array = [];
  var dataTemp, data;
  try{
    if(type === 'sensor')
      dataTemp = fs.readFileSync('./sensors.json');
    else if(type === 'action')
      dataTemp = fs.readFileSync('./actions.json');
    else
      throw new Error('Invalid plug type : '  + type);
    data = JSON.parse(dataTemp);
    for(index in data){
      console.log('found plug ' + index);
      if(data[index].metadata === undefined)
        data[index].metadata = {};
      data[index].metadata.name = index;
      array.push(data[index].metadata);
    }
    callback(null, array);
  } catch(err){
    handleError(err, -32603, callback);
  }
}

function getMetadataForPlug(type, plug, callback) {
  console.log('get metadata for ' +plug);
  countTotal ++;
  var error, result;
  var dataTemp, data;
  try{
    if(type === 'sensor')
      dataTemp = fs.readFileSync('./sensors.json');
    else if(type === 'action')
      dataTemp = fs.readFileSync('./actions.json');
    else
      throw new Error('Invalid plug type : '  + type);
    data = JSON.parse(dataTemp);
    if(data[plug] === undefined)
      throw new Error('Invalid plug name: '+ plug);
    callback(null, data[plug].metadata);
  } catch(err){
    handleError(err, -32603, callback);
  }
}

function registerPlug(type, name, script, metadata, callback) {
  console.log('register plug: ' + type + ", name: " +name);
  countTotal ++;
  var error, result;
  var dataTemp, data, fileName;

  try {
      try{
        if(type === 'sensor')
          fileName = './sensors.json';
        else if(type === 'action')
          fileName = './sensors.json';
        else
          throw new Error('Invalid plug type : '  + type);
        dataTemp = fs.readFileSync(fileName);
        data = JSON.parse(dataTemp);
      } catch(err){
        console.log('Error loading configuration');
        data = {};
      }
      if (typeof metadata === 'string'){
        console.log("metadata is a string, parse it as JSON");
        metadata = JSON.parse(metadata);
      }
      data[name] = {
                    script: script,
                    metadata: metadata
                  };
      console.log('saving ...' + data);
      fs.writeFile(fileName, JSON.stringify(data), function (err) {
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
}
serv.addMethod('waylay_rpc', function (para, callback) {
  console.log('recieved RPC call');
  countTotal ++;
  if (para.length > 0) {
    var name = para[0];
    runScript(name, null, callback);
  } else {
    handleError(new Error('Invalid params'), 32602, callback);
  }
});

//para[0] script name, para[1] options - using template
serv.addMethod('execute_sensor', function(para, callback){
  console.log('execute_sensor');
  execute_plug('sensor', para[0], para[1], callback);
});

//para[0] script name, para[1] options - using template
serv.addMethod('execute_action', function(para, callback){
  console.log('execute_action');
  execute_plug('action', para[0], para[1], callback);
});

//para[0]: sensor name, para[1]: script  content, para[2] metadata
serv.addMethod('register_sensor', function(para, callback){
  console.log('register sensor');
    if (para.length === 3) {
        registerPlug('sensor', para[0], para[1], para[2], callback);
   } else {
    handleError(new Error('Invalid params'), -32602, callback);
   }
});

//para[0]: sensor name, para[1]: script  content, para[2] metadata
serv.addMethod('register_action', function(para, callback){
  console.log('register action');
    if (para.length === 3) {
        registerPlug('action', para[0], para[1], para[2], callback);
   } else {
    handleError(new Error('Invalid params'), -32602, callback);
   }
});

//para[0]: sensor name, para[1]: script  content, para[2] metadata
serv.addMethod('getMetadata', function(para, callback){
  console.log('get metadata');
    if (para.length === 2) {
        getMetadataForPlug(para[0], para[1], callback);
   } else {
    handleError(new Error('Invalid params'), -32602, callback);
   }
});

serv.addMethod('sensors', function (para, callback) {
  console.log('get list of all sensors');
  listPlugs('sensor', callback);
});

serv.addMethod('actions', function (para, callback) {
  console.log('get list of all actions');
  listPlugs('action', callback);
});

serv.addMethod('info', function (para, callback) {
  countTotal ++;
  callback(null, {total: countTotal, errors: errorTotal});
});

// Start the server
serv.start(function (error) {
  if (error) throw error;
  else console.log('Waylay node js server running ...');
});
