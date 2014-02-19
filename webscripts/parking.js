var request = require("request");
var url = "http://datatank.gent.be/Mobiliteitsbedrijf/Parkings11.json";
request({
    url: url,
    json: true
}, function (error, response, body) {

 if (!error && response.statusCode === 200) {
    var parkings = {
       observedState: "Found",
       rawData : {
         locations: body.Parkings11.parkings
       }
    };
    console.log(JSON.stringify(parkings));
  }
});
