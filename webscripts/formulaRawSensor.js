console.log("rawData sensor");
if(options.requiredProperties.formula) {
    try{
      //console.log(options);
      var rawValue =  waylayUtil.evaluateData(options, options.requiredProperties.formula);
      var value = {
        observedState:  rawValue > options.requiredProperties.threshold  ? "Above": "Below",
        rawData:  {  rawValue: rawValue}  
      };
      //console.log("value was " + rawValue);
      send(null, value);
    } catch(err){
        send(new Error(err));
    }
} else{
  send(new Error("Missing property testProperty1"));
}

