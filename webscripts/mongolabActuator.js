var db = options.requiredProperties.db || options.globalSettings.mongodb;
var token = options.requiredProperties.token || options.globalSettings.mongoKey;
var actuatorName = options.requiredProperties.name || "mongoLab";

var data = {
    triggeredNode: options.task.NODE_NAME,
    resource: options.task.RESOURCE,
    name : actuatorName,
    time: new Date()
}

if(token && db && actuatorName){
  var url = "https://api.mongolab.com/api/1/databases/" + db + "/collections/"+ "task_"+ options.task.TASK_ID +"?apiKey="+token;
  var options = {
    url: url,
    json: data
  };
  var callback = function(error, response, body) {
    if (!error && response.statusCode == 200) {
      console.log(body);
      send();
    }else{
      send(new Error("Calling mongolab failed: " + error + " " + body));
    }
  };
  request.post(options, callback);
}else{
  send(new Error("Missing properties"));
}
