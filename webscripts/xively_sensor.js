var id = options.requiredProperties.id;
var threshold =  options.requiredProperties.threshold;
var httpOptions = {
    url: 'https://api.xively.com/v2/feeds/' + options.requiredProperties.feed
};
if(options.requiredProperties.key && options.requiredProperties.key !== ""){
    httpOptions.headers = {
        'X-ApiKey': options.requiredProperties.key
   };
}
function callback(error, response, body) {
    if (!error && response.statusCode == 200) {
        var var1 = JSON.parse(body);
        logger.info(var1);
        var data = __.find(var1.datastreams, function(d){
            return (d.id === id);
        });
        data.collectedTime = new Date(data.at).getTime();
        data.current_value = parseFloat(data.current_value);
        data.min_value = parseFloat(data.min_value);
        data.max_value = parseFloat(data.max_value);
        data.threshold = threshold;
        var raw = {};
        if(var1.location && var1.location.lat && var1.location.lon){
            raw.latitude = var1.location.lat;
            raw.longitude = var1.location.lon;
        }
        __.map(var1.datastreams, function(data){
            raw[data.id+'_current_value'] = parseFloat(data.current_value);
            raw[data.id+'_min_value'] = parseFloat(data.min_value);
            raw[data.id+'_max_value'] = parseFloat(data.max_value);
            //raw[data.id+'_collectedTime'] = new Date(data.at).getTime();
        });
        console.log(raw);
        value = {  
            observedState:  data.current_value > threshold ? "Above": "Below", 
            rawData : raw 
        }; 
        send(null, value);
    }
}
request(httpOptions, callback);
