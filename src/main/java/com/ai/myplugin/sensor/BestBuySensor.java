package com.ai.myplugin.sensor;


import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.myplugin.util.EmptyTestResult;
import com.ai.myplugin.util.Utils;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.logging.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.logging.LogFactory.*;

/**
 * Created by User: veselin
 * On Date: 26/12/13
 */
@PluginImplementation
public class BestBuySensor implements BNSensorPlugin {
    protected static final Log log = getLog(BestBuySensor.class);
    static final String PRODUCT = "product";
    static final String PRICE = "price";
    static final String URL = "url";
    String URL_DEFAULT = "http://tweakers.net/pricewatch/330271/samsung-ue46f7000-zilver.html";

    Map<String, Object> propertiesMap = new ConcurrentHashMap<String, Object>();

    String [] states = {"Found", "Not Found"};
    private static final String NAME = "BestBuySensor";

    @Override
    public String[] getRequiredProperties() {
        return new String[]{PRODUCT, PRICE, URL};
    }

    @Override
    public String[] getRuntimeProperties() {
        return new String[]{};
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(Arrays.asList(getRequiredProperties()).contains(string)) {
            propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    @Override
    public String getDescription() {
        return "Best buy for the product (only tweakers right now), you should put complete URL";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        if(getProperty(PRICE) == null)
            throw new RuntimeException("price not set");
        if(getProperty(PRODUCT) == null)
            throw new RuntimeException("product not set");
        String pathURL = getProperty(URL) == null ? URL_DEFAULT : (String) getProperty(URL);
        ArrayList<MyProduct> products = new ArrayList<MyProduct>();

        try{
            Document doc = Jsoup.connect(pathURL).timeout(20000)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_7) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.91 Safari/534.30")
                    .get();
            for (Element table : doc.select(".shop-listing")) {
                for (Element row : table.select("tr")) {
                    Elements tds = row.select("td");
                    if(tds.size() > 11) {
                        log.debug("Found entry " + tds.get(0).text() + "#" + tds.get(1).text() + "#" +
                                tds.get(2).text() + "#" + tds.get(3).text() + "#" + tds.get(4).text() +
                                tds.get(5).text() + "#" + tds.get(6).text() + "#" + tds.get(7).text() +
                                tds.get(8).text() + "#" + tds.get(9).text() + "#" + tds.get(10).text() +
                                tds.get(11).text() + "#" + tds.get(12).text());

                        String url = tds.get(0).select("a") != null? tds.get(0).select("a").toString(): pathURL;
                        String shop = tds.get(0).text();
                        String score = tds.get(1).text();
                        String price = tds.get(3).text().substring(0, tds.get(3).text().indexOf(",")).replace(".","");
                        String total = tds.get(4).text().substring(0, tds.get(4).text().indexOf(",")).replace(".","");
                        double p = Utils.getDouble(price.replace("€", "").replace("-", "").trim());
                        double t = Utils.getDouble(total.replace("€","").replace("-", "").trim());
                        double s = 0;

                        try{
                            s = Utils.getDouble(score.replace("Score:", "").trim());
                        } catch (Exception e){
                            log.warn(e.getMessage());
                        }
                        //TODO check what's wrong with url
                        MyProduct myProduct = new MyProduct(shop, getProperty(PRODUCT).toString(), p, t, s, pathURL);
                        products.add(myProduct);
                        log.info(myProduct.getAsJSON());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            return new EmptyTestResult();
        }
        Collections.sort(products);

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(MyProduct product : products){
            jsonArray.add(product.getAsJSON());

        }
        jsonObject.put("products", jsonArray);
        jsonObject.put("bestBuy", jsonArray.get(0));
        String state = states [1];
        if(products.get(0).total <= Utils.getDouble(getProperty(PRICE)))
            state = states [0];

        final JSONObject finalJsonObject = jsonObject;
        final String finalState = state;
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Best buy";
            }

            @Override
            public String getObserverState() {
                return finalState;
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            @Override
            public String getRawData() {
                return finalJsonObject.toJSONString();
            }
        };

    }

    @Override
    public void shutdown(TestSessionContext testSessionContext) {

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getSupportedStates() {
        return states;
    }

    public static void main(String []args)  {
        BestBuySensor bestBuySensor = new BestBuySensor();
        bestBuySensor.setProperty(PRICE, 1000);
        bestBuySensor.setProperty(PRODUCT, "ue46f7000");
        TestSessionContext testSessionContext = new TestSessionContext(1);

        TestResult testResult = bestBuySensor.execute(testSessionContext);
        System.out.println(testResult.getRawData());
        System.out.println(testResult.getObserverState());
    }

    private class MyProduct implements Comparable{
        String product;
        String shop;
        double price;
        double total;
        double score;
        String mapURL;


        private MyProduct(String shop, String product, double price, double total, double score, String mapURL) {
            this.shop = shop;
            this.product = product;
            this.price = price;
            this.total = total;
            this.score = price;
            this.mapURL = mapURL;
        }

        public JSONObject getAsJSON(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("shop", shop);
            jsonObject.put("product", product);
            jsonObject.put("url", mapURL);
            jsonObject.put("price", price);
            jsonObject.put("total", total);
            return jsonObject;
        }

        @Override
        public int compareTo(Object o) {
            MyProduct other = (MyProduct) o;
            return Double.compare(this.total, other.total);
        }
    }
}
