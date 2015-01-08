 var d = new Date();
 console.log(d);
 var nd = d; 
 //  offset in hours
 var offset = options.requiredProperties.timeZoneOffset;
 if(options.requiredProperties.date === undefined) {
     send(new Error("Missing property date"));
 }
 else {
     var inputDate = new Date(options.requiredProperties.date);
     if(offset){
        // convert to msec, add local time zone offset and get UTC time in msec
        var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
        nd = new Date(utc + (3600000*offset));
    }
    //using underscore library
  var retValue = inputDate.getFullYear() == nd.getFullYear() && inputDate.getMonth() == nd.getMonth() && inputDate.getDate() == nd.getDate();
  var value = {
    observedState: retValue ? "TRUE" : "FALSE",
    rawData: {  utc: d.getTime() , year: nd.getFullYear(), month: nd.getMonth() +1 , day: nd.getDate(), hours:nd.getHours(), minutes: nd.getMinutes()}
  };
  console.log(value);
  send(null, value);
 }

