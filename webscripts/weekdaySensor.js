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
var day = parseInt(nd.getDay());
var retState = "Monday";
if(day == 0)
  retState = "Sunday";
else if(day == 1)
  retState = "Monday";
else if(day == 2)
  retState = "Tuesday";
else if(day == 3)
  retState = "Wednesday";
else if(day == 4)
  retState = "Thursday";
else if(day == 5)
  retState = "Friday";
else 
  retState = "Saturday";
  var value = {
    observedState: retState,
    rawData: {  utc: d.getTime() , year: nd.getFullYear(), month: nd.getMonth() +1 , day: nd.getDate(), hours:nd.getHours(), minutes: nd.getMinutes()}
  };
console.log(value);
send(null, value);
