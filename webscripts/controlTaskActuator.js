var taskID = options.requiredProperties.taskId || options.task.TASK_ID;
var command = options.requiredProperties.command;
var username =  options.globalSettings.API_KEY;
var password = options.globalSettings.API_PASS;

request.post(
    'https://'+ username + ':' + password +'@app.waylay.io/api/tasks/'+taskID+'/command/'+command,
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            send();
        } else
            send(new Error("Error executing the action"));
    }
);

