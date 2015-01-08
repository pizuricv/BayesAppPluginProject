 var d = new Date();
 console.log(d);
 var nd = d; 
 //  offset in hours
 var offset = options.requiredProperties.timeZoneOffset;
 var month = options.requiredProperties.month;
 if(month === undefined) {
     send(new Error("Missing property month"));
 }
 else {
     if(offset){
        // convert to msec, add local time zone offset and get UTC time in msec
        var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
        nd = new Date(utc + (3600000*offset));
    }
    //using underscore library
  var retValue = __.find(month.split(","), function(num){ return num == nd.getMonth() +1; });
  var value = {
    observedState: retValue ? "TRUE" : "FALSE",
    rawData: {  utc: d.getTime() , year: nd.getFullYear(), month: nd.getMonth() +1, day: nd.getDate(), hours:nd.getHours(), minutes: nd.getMinutes()}
  };
  console.log(value);
  send(null, value);
 }

