var threshold = options.requiredProperties.threshold;
var API_KEY = options.requiredProperties.key;

unirest.get("https://community-bitcointy.p.mashape.com/average/USD").header("X-Mashape-Key", API_KEY).end(function (result) {
  var bitcoin = JSON.parse(result.body);
  var value = {
        observedState: bitcoin.value < threshold? "Below" : "Above",
        rawData : {
            currency: bitcoin.currency,
            price : bitcoin.value 
        }
       };
  send(null, value);
});
