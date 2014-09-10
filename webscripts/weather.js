var url ="http://api.openweathermap.org/data/2.5/weather?q=" + options.requiredProperties.city + "&units=metric";

request({
        "uri": url
    }, function(err, resp, body){
        var data = JSON.parse(body);
        var state = data.weather[0].main;
        var value = {
        observedState: state,
        rawData :{
          data: {
              temperature: data.main.temp,
              pressure: data.main.pressure,
              humidity: data.main.humidity,
              temp_min: data.main.temp_min,
              temp_max: data.main.temp_max,
              wind_speed: data.wind.speed,
              clouds_coverage: data.clouds.all,
              sunrise: data.sys.sunrise,
              sunset: data.sys.sunset,
              longitude: data.coord.lon,
              latitude:data.coord.lat 
              
          }
        }
       };
       //console.log(JSON.stringify(value));
       send(null, value);
});
