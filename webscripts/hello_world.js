var randomValue = Math.random(); 
value = {  
    observedState:  randomValue > 0.5? "hello" : "world", 
    rawData : {  message:"message", x: 2.34, random: randomValue, property: options.requiredProperties.testProperty1}  
}; 
send(null,value);
