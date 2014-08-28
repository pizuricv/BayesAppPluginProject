var id = options.requiredProperties.id;
var threshold =  options.requiredProperties.threshold;

var options = {
    url: 'https://api.xively.com/v2/feeds/' + options.requiredProperties.feed,
    headers: {
        'X-ApiKey': options.requiredProperties.key
    }
};


function callback(error, response, body) {
    if (!error && response.statusCode == 200) {
        var var1 = JSON.parse(body);
        var data = __.find(var1.datastreams, function(d){
            return (d.id === id);
        });
        var collectedTime = new Date(data.at).getTime();
        data.collectedTime = collectedTime;
        data.current_value = parseFloat(data.current_value);
        data.min_value = parseFloat(data.min_value);
        data.max_value = parseFloat(data.max_value);
        value = {  
            observedState:  data.current_value > threshold ? "Above": "Bellow", 
            rawData : data  
        }; 
        send(null, value);
    }
}

request(options, callback);
