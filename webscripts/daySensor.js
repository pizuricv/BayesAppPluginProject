 var d = new Date();
 console.log(d);
 var nd = d; 
 //  offset in hours
 var offset = options.requiredProperties.timeZoneOffset;
 var day = options.requiredProperties.day;
 if(day === undefined) {
     send(new Error("Missing property day"));
 }
 else {
     if(offset){
        // convert to msec, add local time zone offset and get UTC time in msec
        var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
        nd = new Date(utc + (3600000*offset));
    }
    //using underscore library
  var retValue = __.find(day.split(","), function(num){ return num == nd.getDate(); });
  var value = {
    observedState: retValue ? "TRUE" : "FALSE",
    rawData: {  utc: d.getTime() , year: nd.getFullYear(), month: nd.getMonth() +1 , day: nd.getDate(), hours:nd.getHours(), minutes: nd.getMinutes()}
  };
  console.log(value);
  send(null, value);
 }

