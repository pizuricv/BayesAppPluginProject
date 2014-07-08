var url = "http://datatank.gent.be/Mobiliteitsbedrijf/Parkings11.json";
var error;
console.log("starting parking request");
value = { observedState: "NotFound"};
request({
    url: url,
    json: true
}, function (error, response, body) {
 console.log("response code "+response.statusCode);
 if (!error && response.statusCode === 200) {
    console.log("reading locations from: "+body.Parkings11.parkings);
    value = {
       observedState: "Found",
       rawData : {
         locations: body.Parkings11.parkings
       }
    };
    console.log("done, state is: " + value.observedState);
    send(null,value);
  } else {
     console.log("error" );
     var err = { code: response.statusCode, message: response };
     send(err, value);
  }
});
