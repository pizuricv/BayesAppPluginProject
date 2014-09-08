var TWILIO_ACCOUNT_SID = options.requiredProperties.TWILIO_ACCOUNT_SID;
var TWILIO_AUTH_TOKEN = options.requiredProperties.TWILIO_AUTH_TOKEN;
var message = options.requiredProperties.message;
var to = options.requiredProperties.to;
var from = options.requiredProperties.from;
 
var client = new twilio.RestClient(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
 
// Pass in parameters to the REST API using an object literal notation. The
// REST client will handle authentication and response serialzation for you.
client.sms.messages.create({
    to: to,
    from: from,
    body: message
}, function(error, message) {
    if (!error) {
        logger.info('Success! The SID for this SMS message is:');
        logger.info(message.sid);
        logger.info('Message sent on:');
        logger.info(message.dateCreated);
        send();
    } else {
        logger.error('Oops! There was an error.');
        send(new Error("Oops! There was an error."));
    }
});
