var winston = require('winston');
var rpc = require('node-json-rpc');
var request = require('request');
var cheerio = require('cheerio');
var Promise = require('promise');
var vm = require('vm');
var fs = require('fs');
var unirest = require('unirest');
var gcm = require('node-gcm');
var und = require('underscore');


const ERROR_CODE_NOT_FOUND = 404;

var sandbox = {cheerio:cheerio, request:request, gcm: gcm, __: und, unirest:unirest, console:console};
var countTotal = 0;
var errorTotal = 0;

var logger = new (winston.Logger)({
  transports: [
    new (winston.transports.Console)({ level: 'info', colorize: 'true' }),
    new (winston.transports.File)({ filename: 'server.log' })
  ]
});

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

function handleError(err, code, callback) {
  logger.error(err.message);
  var result;
  errorTotal ++;
  var error = { code: code, message: err.message};
  logger.warn("error: " + JSON.stringify(error));
  callback(error, result);
}

function runScript (content, options, callback) {
  countTotal ++;
  logger.debug('runScript script: ' + content);
  sandbox.send = function(err, returned){
    if(err){
      handleError(err, -32000, callback);
    }
    // make sure there always is a response object, otherwise the rpc server sends an empty response
    // which does not adhere to the json-rpc specification
    if(!returned){
      callback(err, {});
    }else{
      callback(err, returned);
    }
  };
  var promise = new Promise(function(resolve, reject) {
    try {
      var script = content;
      if(options){
        logger.info('options: ' + options);
        if (typeof options === 'string') {
          options = JSON.parse(options);
        }
        sandbox.options = options;
      } else {
        if(sandbox.options !== undefined)
          delete sandbox.options;
      }
      logger.debug('executing script: ' + script);
      var context = vm.createContext(sandbox);
      vm.runInContext(script, context, 'myfile.vm');
      logger.info('script executed');
      resolve();
    } catch(err){
      reject(err);
    }
  });
  promise.then(function(result) {
    logger.info('Total number of requests: ' + countTotal + ', total number of errors ' + errorTotal);
  }, function(err) {
    handleError(err, -32603, callback);
  });
}

function fileForType(type) {
  if (type === 'sensor') {
    return './sensors.json';
  } else if (type === 'action') {
    return './actions.json';
  } else {
    throw new Error('Invalid plug type : ' + type);
  }
}

function readSyncOrEmpty(path) {
  if (fs.existsSync(path)) {
    return JSON.parse(fs.readFileSync(path));
  } else {
    logger.warn("Path does not exist: " + path);
    return {};
  }
}

function readPlugs(type) {
  var path = fileForType(type);
  return readSyncOrEmpty(path);
}


function execute_plug(type, name, options, callback){
  logger.info('execute ' + type + ' ' + name);
  countTotal ++;
  try {
    var plugs = readPlugs(type);
    if (plugs[name] === undefined) {
      handleError(new Error('No ' + type + ' with name: ' + name), ERROR_CODE_NOT_FOUND, callback);
    } else {
      if (plugs[name].file !== undefined)
        plugs[name].script = fs.readFileSync(plugs[name].file);
      var content = plugs[name].script;
      runScript(content, options, callback);
    }
  } catch(err){
    handleError(err, -32602, callback);
  }
}

function listPlugs(type, callback) {
  logger.info('list plugs: ' + type);
  countTotal ++;
  var array = [];
  try{
    var plugs = readPlugs(type);
    for(index in plugs){
      logger.info('found plug ' + index);
      if(plugs[index].metadata === undefined)
        plugs[index].metadata = {};
      plugs[index].metadata.name = index;
      array.push(plugs[index].metadata);
    }
    callback(null, array);
  } catch(err){
    handleError(err, -32603, callback);
  }
}

function getMetadataForPlug(type, name, callback) {
  logger.info('get metadata for ' + type + ' ' + name);
  countTotal ++;
  var data;
  try{
    data = readPlugs(type);
    if(data[name] === undefined){
      handleError(new Error('No ' + type + ' with name: '+ name), ERROR_CODE_NOT_FOUND, callback);
    }else {
      callback(null, data[name].metadata);
    }
  } catch(err){
    handleError(err, -32603, callback);
  }
}

function getPlug(type, name, callback) {
  logger.info('get ' + type + ' ' + name);
  countTotal ++;
  var data;
  try{
    data = readPlugs(type);
    if(data[name] === undefined){
      handleError(new Error('No ' + type + ' with name: '+ name), ERROR_CODE_NOT_FOUND, callback);
    }else {
      // TODO handle the case where the plug is defined in a file?
      callback(null, data[name]);
    }
  } catch(err){
    handleError(err, -32603, callback);
  }
}


function registerPlug(type, name, script, metadata, callback) {
  logger.info('register plug: ' + type + ", name: " +name);
  var error;
  var dataTemp;
  var data = {};
  try {
    var fileName = fileForType(type);
    var data = readSyncOrEmpty(fileName);
    if (typeof metadata === 'string'){
      logger.info("metadata is a string, parse it as JSON");
      metadata = JSON.parse(metadata);
    }
    data[name] = {
      script: script,
      metadata: metadata
    };
    logger.info('saving ...' + data);
    fs.writeFile(fileName, JSON.stringify(data), function (err) {
      if (err) {
        handleError(err, -32000, callback);
      } else{
        logger.info('Configuration saved successfully.');
        countTotal ++;
        callback(error, data);
      }
    });

  } catch(err){
    handleError(err, -32000, callback);
  }
}

var serv = new rpc.Server(options);

serv.addMethod('waylay_rpc', function (para, callback) {
  logger.info('recieved RPC call');
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
  logger.info('execute_sensor');
  execute_plug('sensor', para[0], para[1], callback);
});

//para[0] script name, para[1] options - using template
serv.addMethod('execute_action', function(para, callback){
  logger.info('execute_action');
  execute_plug('action', para[0], para[1], callback);
});

//para[0]: sensor name, para[1]: script  content, para[2] metadata
serv.addMethod('register_sensor', function(para, callback){
  logger.info('register sensor');
  if (para.length === 3) {
    registerPlug('sensor', para[0], para[1], para[2], callback);
  } else {
    handleError(new Error('Invalid params'), -32602, callback);
  }
});

//para[0]: sensor name, para[1]: script  content, para[2] metadata
serv.addMethod('register_action', function(para, callback){
  logger.info('register action');
  if (para.length === 3) {
    registerPlug('action', para[0], para[1], para[2], callback);
  } else {
    handleError(new Error('Invalid params'), -32602, callback);
  }
});

//para[0]: type, para[1] name
serv.addMethod('getMetadata', function(para, callback){
  logger.info('get metadata');
  if (para.length === 2) {
    getMetadataForPlug(para[0], para[1], callback);
  } else {
    handleError(new Error('Invalid params'), -32602, callback);
  }
});

//para[0]: name
serv.addMethod('sensor', function(para, callback){
  logger.info('get sensor');
  if (para.length === 1) {
    getPlug("sensor", para[0], callback);
  } else {
    handleError(new Error('Invalid params'), -32602, callback);
  }
});

serv.addMethod('sensors', function (para, callback) {
  logger.info('get list of all sensors');
  listPlugs('sensor', callback);
});

//para[0]: name
serv.addMethod('action', function(para, callback){
  logger.info('get action');
  if (para.length === 1) {
    getPlug("action", para[0], callback);
  } else {
    handleError(new Error('Invalid params'), -32602, callback);
  }
});

serv.addMethod('actions', function (para, callback) {
  logger.info('get list of all actions');
  listPlugs('action', callback);
});

serv.addMethod('info', function (para, callback) {
  countTotal ++;
  callback(null, {total: countTotal, errors: errorTotal});
});

// Start the server, but only if we are the main script
if(!module.parent){
  serv.start(function (error) {
    if (error) {
      throw error;
    } else{
      logger.info('Waylay node js server running ...');
    }
  });
}

module.exports = {server: serv, logger:logger};
