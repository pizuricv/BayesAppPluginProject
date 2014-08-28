var threshold = options.requiredProperties.threshold;
var API_KEY = options.requiredProperties.key;

unirest.get("https://community-bitcointy.p.mashape.com/average/USD").header("X-Mashape-Key", API_KEY).end(function (result) {
  var value = {
        observedState: result.body.value < threshold? "Bellow" : "Above",
        rawData : result.body
       };
  send(null, value);
});
