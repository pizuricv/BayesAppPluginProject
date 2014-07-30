var fs = require("fs");
var chai = require("chai");
var server = require("../rpc_server");
var rpc = require('node-json-rpc');

var expect = chai.expect;
chai.should();

var options = {
  port: 5080,
  host: '127.0.0.1',
  path: '/',
  strict: true
};

// Create a client object with options
var client = new rpc.Client(options);


describe("server", function() {

  beforeEach(function(done){
    if(fs.existsSync("sensors.json")) {
      fs.unlinkSync("sensors.json");
    }
    if(fs.existsSync("actions.json")) {
      fs.unlinkSync("actions.json");
    }
    server.server.start(done);
  });

  afterEach(function(done){
    server.server.stop(done);
  });


  describe("list sensors", function() {
    it("should return an empty list", function(done){

      client.call (
        {"jsonrpc": "2.0", "method": "sensors", "params": [], "id": 2},
        function (err, res){
          if (err) {
            done(err)
          } else {
            //console.log(res);
            res.result.should.have.length(0);
            res.id.should.equal(2);
            done();
          }
        }
      );
    });

    it("return an error when trying to execute an unexisting sensor", function(done){
      client.call (
        {"jsonrpc": "2.0", "method": "execute_sensor", "params": ["foo"], "id": 6},
        function (err, res){
          if (err) {
            done(err)
          } else {
            res.error.message.should.equal("Invalid plug name: foo");
            done();
          }
        }
      );
    });

  });
});