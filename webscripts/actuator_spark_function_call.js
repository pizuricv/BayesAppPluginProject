// name: SparkFunctionCall
// icon: https://lh4.ggpht.com/wy1RxASYixSM4HGDLV72sTuJN36ycSZaXNions_v3Etu5vWbtmYR8VPAnz3FHcQzFHxG=w300-rw
// properties:
//   * access_token
//   * functionName
//   * param
//   * device

var props = options.requiredProperties;

if(props.access_token && props.functionName && props.param && props.device){

  var options = {
    url: 'https://api.spark.io/v1/devices/' + props.device + "/" + props.functionName,
    form:    { param: props.param, access_token: props.access_token }
  };

  var callback = function(error, response, body) {
    if (!error && response.statusCode == 200) {
      var bodyJson = JSON.parse(body);
      send();
    }else{
      send(new Error("Calling api.spark.io failed: " + error + " " + body));
    }
  };

  request.post(options, callback);
}else{
  send(new Error("Missing properties"));
}
