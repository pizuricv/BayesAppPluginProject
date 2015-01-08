var url = options.requiredProperties.url;
var API_KEY = options.requiredProperties.key || options.globalSettings.MASHAPE_KEY;


// These code snippets use an open-source library. http://unirest.io/nodejs
unirest.get("https://igor-zachetly-ping-uin.p.mashape.com/pinguin.php?address=" + url)
.header("X-Mashape-Key", API_KEY)
.end(function (result) {
  console.log(result.body);
  var pingResult = result.body;
  var value = {
      observedState: pingResult.result === "true"? "Live" : "NotAlive",
      rawData : {
            pingTime : pingResult.time 
      }
  };
  send(null, value);
});
