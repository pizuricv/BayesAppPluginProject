var taskID = options.task.SCENARIO_ID;
var command = options.requiredProperties.command;

request.post(
    'http://app.waylay.io/api/tasks/'+taskID+'/command/'+command,
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            send();
        } else
            send(new Error("Missing property testProperty1"));
    }
);

