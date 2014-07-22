//var request = require('request');
//var cheerio = require('cheerio');

var url = "http://www.imdb.com/chart/";

request({
        "uri": url
    }, function(err, resp, body){
        var $ = cheerio.load(body);
        var movies = [];
        $('th:contains(Gross)').parents('table').find('tr').each(function(index,item){
          if(index>0)
          {
            var tds = $(item).find('td');
            var movie = {
               title: $(tds.eq(1)).find('a').text().trim(),
               week: tds.eq(2).text().trim(),
               gross: tds.eq(3).text().trim(),
               weeks: tds.eq(4).text().trim()
             };
             movies.push(movie);
           }
       });
       //console.log(JSON.stringify(movies));
       var value ={
        observedState: "Found",
        rawData :{
          movies: movies,
          topMovie: movies[0]
        }
       };
       send(null, value);
});
