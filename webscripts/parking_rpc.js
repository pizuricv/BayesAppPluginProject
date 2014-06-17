var url = "http://datatank.gent.be/Mobiliteitsbedrijf/Parkings11.json";
var error;
sandbox.console.log("starting parking request");
value = { observedState: "NotFound"};
sandbox.request({
    url: url,
    json: true
}, function (error, response, body) {
 sandbox.console.log("response code "+response.statusCode);
 if (!error && response.statusCode === 200) {
    sandbox.console.log("reading locations from: "+body.Parkings11.parkings);
    value = {
       observedState: "Found",
       rawData : {
         locations: body.Parkings11.parkings
       }
    };
    sandbox.console.log("done, state is: " + value.observedState);
    sandbox.send(null,value);
  } else {
     sandbox.console.log("error" );
     var err = { code: response.statusCode, message: response };
     sandbox.send(err, value);
  }
});
