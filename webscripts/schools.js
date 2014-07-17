var url = "http://datatank.gent.be/Onderwijs&Opvoeding/Basisscholen.json";
var locations;
request({
    url: url,
    json: true
}, function (error, response, body) {

 if (!error && response.statusCode === 200) {
    locations = {
       observedState: "Found",
       rawData : {
         locations: body.Basisscholen
       }
    };
    for(location in locations.rawData.locations){
      locations.rawData.locations[location].longitude = locations.rawData.locations[location].long;
      locations.rawData.locations[location].latitude = locations.rawData.locations[location].lat;
      locations.rawData.locations[location].address = locations.rawData.locations[location].straat;
   }
   send(null,locations);
  }
});
