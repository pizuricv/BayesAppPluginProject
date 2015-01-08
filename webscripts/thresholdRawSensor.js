// sensors should never throw exceptions but instead send an error back
/*options.rawData = {
  "hello" : 
  {   randomValue: 5,
      state: "True"
  }
};*/
console.log("rawData sensor");
if(options.requiredProperties.node && options.requiredProperties.param) {
    try{
      var rawData =  waylayUtil.getRawData(options, options.requiredProperties.node);
      console.log(rawData || "no raw Data");
      var rawValue = rawData[options.requiredProperties.param];
      console.log(rawValue || "nothing");
      var value = {
        observedState: rawValue > options.requiredProperties.threshold ? "Above" : "Below",
        rawData:  {  value: rawValue}  
      };
      send(null, value);
    } catch(err){
        send(new Error(err));
    }
} else{
  send(new Error("Missing property testProperty1"));
}

