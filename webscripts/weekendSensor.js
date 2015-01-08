 var d = new Date();
 console.log(d);
 var nd = d; 
 //  offset in hours
 var offset = options.requiredProperties.timeZoneOffset;
 if(offset){
    // convert to msec, add local time zone offset and get UTC time in msec
    var utc = d.getTime() + (d.getTimezoneOffset() * 60000);
    nd = new Date(utc + (3600000*offset));
 }
  var value = {
    observedState: nd.getDay() === 0 ||  nd.getDay() === 6 ? "TRUE" : "FALSE",
    rawData: {  utc: d.getTime() , year: nd.getFullYear(), month: nd.getMonth() +1 , day: nd.getDate(), hours:nd.getHours(), minutes: nd.getMinutes()}
  };
  console.log(value);
  send(null, value);
