//var gcm = require('node-gcm');

// create a message with default values
var message = new gcm.Message();

// or with object values
var message = new gcm.Message({
    collapseKey: 'demo',
    delayWhileIdle: true,
    timeToLive: 3,
    data: {
        key1: 'message1',
        key2: 'message2'
    }
});

var sender = new gcm.Sender('insert Google Server API Key here');
var registrationIds = [];

// OPTIONAL
// add new key-value in data object
message.addDataWithKeyValue('key1','message1');
message.addDataWithKeyValue('key2','message2');

// or add a data object
message.addDataWithObject({
    key1: 'message1',
    key2: 'message2'
});

// or with backwards compability of previous versions
message.addData('key1','message1');
message.addData('key2','message2');


message.collapseKey = 'demo';
message.delayWhileIdle = true;
message.timeToLive = 3;
message.dryRun = true;
// END OPTIONAL

// At least one required
registrationIds.push('regId1');
registrationIds.push('regId2'); 

/**
 * Params: message-literal, registrationIds-array, No. of retries, callback-function
 **/
sender.send(message, registrationIds, 4, function (err, result) {
    console.log(result);
});

