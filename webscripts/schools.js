var request = require("request");
var url = "http://datatank.gent.be/Onderwijs&Opvoeding/Basisscholen.json"
request({
    url: url,
    json: true
}, function (error, response, body) {

 if (!error && response.statusCode === 200) {
    var locations = {
       observedState: "Found",
       rawData : {
         locations: body.Basisscholen
       }
    };
    for(location in locations.rawData.locations){
      locations.rawData.locations[location].longitude = locations.rawData.locations[location].long;
      locations.rawData.locations[location].latitude = locations.rawData.locations[location].lat;  
   } 
    console.log(JSON.stringify(locations, null, 4));
  }
});
