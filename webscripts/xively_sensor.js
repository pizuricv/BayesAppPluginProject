var options = {
    url: 'https://api.xively.com/v2/feeds/' + options.requiredProperties.feed,
    headers: {
        'X-ApiKey': options.requiredProperties.key
    }
};

function callback(error, response, body) {
    if (!error && response.statusCode == 200) {
        var var1 = JSON.parse(body);
        value = {  
            observedState:  "OK", 
            rawData : var1  
        }; 
        send(null, value);
    }
}

request(options, callback);
