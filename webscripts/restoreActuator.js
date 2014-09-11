var backup = options.requiredProperties.backup;
request.post(
    'http://app.waylay.io/api/conf', { form: { action: 'restore', backupFile:backup } },
    function (error, response, body) {
        if (!error && response.statusCode == 200) {
            logger.error(JSON.parse(body));
            var value = {
                message: JSON.parse(body)
            }
            send(null, value);
        } else
            send(new Error(error));
    }
);

