var winston = require('winston');
var fs = require("fs");
var chai = require("chai");
var Q = require("q");
var rpc = require('node-json-rpc');
var server = require("../rpc_server");

// disable logging
server.logger.clear();

var expect = chai.expect;
chai.should();

var options = {
  port: 5080,
  host: '127.0.0.1',
  path: '/',
  strict: true
};

var idCounter = 1;

// Create a client object with options
var client = new rpc.Client(options);

function call(method, params, callback){
  var request = {
    method:method,
    params:params,
    jsonrpc: "2.0",
    id: idCounter++
  };
  //console.log(request.id, request.method, request.params);
  client.call(request, callback);
}

function callf(method, params){
  return function() {
    var deferred = Q.defer();
    call(method, params, function (err, result) {
      if (err) {
        console.log("  => ERROR " + err);
        deferred.reject(err);
      } else {
        if (result && result.error) {
          console.log("  => ERROR " + result.error.message);
          deferred.reject(new Error(result.error.message));
        } else {
//          if(result && result.result) {
//            console.log("  => " + JSON.stringify(result.result));
//          }else{
//            console.log("  => " + result);
//          }
          deferred.resolve(result);
        }
      }
    });
    return deferred.promise;
  }
}


function cleanStorage() {
  if (fs.existsSync("sensors.json")) {
    fs.unlinkSync("sensors.json");
  }
  if (fs.existsSync("actions.json")) {
    fs.unlinkSync("actions.json");
  }
}

describe("server", function() {

  beforeEach(function(done){
    cleanStorage();
    server.server.start(done);
  });

  afterEach(function(done){
    server.server.stop(done);
    cleanStorage();
  });


  describe("list sensors", function() {

    it("should return an empty list", function(done){
      call ("sensors", [], function (err, res){
        if (err) {
          done(err)
        } else {
          //console.log(res);
          res.result.should.have.length(0);
          done();
        }
      });
    });

  });

  describe("get sensor", function() {

    it("should return the sensor", function(done){
      var registerSensor = callf("register_sensor", ["sensorX", "var value = { observedState: \"teststate\", rawData: \"testdata\"};send(null, value);", {author: "Veselin"} ]);
      registerSensor()
        .then(callf("sensor", ["sensorX"]))
        .then(function (res) {
          //console.log(res);
          res.result.script.should.be.a('string');
          done();
        })
        .catch(function (error) {
          done(error);
        })
        .done();
    });

    it("return a 404 error if there is no such sensor", function(done){
      call ("sensor", ["foo"], function (err, res){
        if (err) {
          done(err)
        } else {
          res.error.code.should.equal(404);
          done();
        }
      });
    });

  });

  describe("get metadata", function() {

    it("return a 404 error if there is no such action", function(done){
      call ("getMetadata", ["sensor", "foo"], function (err, res){
        if (err) {
          done(err)
        } else {
          res.error.code.should.equal(404);
          done();
        }
      });
    });

  });

  describe("register sensor", function() {

    it("should work for a simple sensor", function(done){
      var name = "test";
      var script = "var value = { observedState: \"hello\", rawData: \"hello2\"};sandbox.send(null, value);";
      var metadata = {author:"dido"};
      call ("register_sensor", [name, script, metadata], function (err, res){
        if (err) {
          done(err)
        } else {
          //console.log(res);
          res.result.test.metadata.author.should.equal("dido");
          done();
        }
      });
    });

  });

  describe("execute sensors", function() {

    it("return the correct result", function(done){
      var registerSensor = callf("register_sensor", ["sensorX", "var value = { observedState: \"teststate\", rawData: \"testdata\"};send(null, value);", {author: "Veselin"} ]);
      registerSensor()
        .then(callf("execute_sensor", ["sensorX"]))
        .then(function (res) {
          res.result.observedState.should.equal("teststate");
          res.result.rawData.should.equal("testdata");
          done();
        })
        .catch(function (error) {
          done(error);
        })
        .done();
    });

    it("should not share context between invocations", function(done){
      var registerSensor = callf("register_sensor", ["sensorX", "if(typeof variable === 'undefined'){ i = 0; send(null,'defined i');}else{send('shared context!',null);}", {author: "Veselin"} ]);
      registerSensor()
        .then(callf("execute_sensor", ["sensorX"]))
        .then(callf("execute_sensor", ["sensorX"]))
        .then(function (res) {
          res.should.not.have.property('error');
          done();
        })
        .catch(function (error) {
          done(error);
        })
        .done();
    });

    it("return a 404 error if there is no such sensor", function(done){
      call ("execute_sensor", ["foo"], function (err, res){
        if (err) {
          done(err)
        } else {
          res.error.code.should.equal(404);
          done();
        }
      });
    });

  });

  describe("chain", function() {

    it("should work for this chain of requests", function(done) {

      var rpc = callf("waylay_rpc", ["var value = {observedState:\"hello\",rawData:{}};send(null,value);"]);
      rpc()
        .then(callf("register_sensor", ["sensorX", "var value = { observedState: \"hello\", rawData: \"hello2\"};send(null, value);", {author: "Veselin"} ]))
        .then(callf("sensor", ["sensorX"]))
        .then(callf("execute_sensor", ["sensorX"]))
        .then(callf("info", []))
        .then(callf("sensors", []))
        .then(callf("register_action", ["actionX", "var error; var value;console.log('test actuator executing');send(error, value);", {author: "Veselin" } ]))
        .then(callf("action", ["actionX"]))
        .then(callf("info", []))
        .then(callf("actions", []))
        .then(callf("execute_action", ["actionX"]))
        .then(function (value) {
          done();
        })
        .catch(function (error) {
          done(error);
        })
        .done();
    });
  });

});
