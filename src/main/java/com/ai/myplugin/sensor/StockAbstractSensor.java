/**
 * User: pizuricv
 * Date: 6/4/13
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;

import com.ai.myplugin.util.Rest;
import com.ai.myplugin.util.SensorResultBuilder;
import com.ai.myplugin.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class StockAbstractSensor implements SensorPlugin {

    static final String STATE_BELOW = "Below";
    static final String STATE_ABOVE = "Above";
    static final String STATE_UNDEFINED = "";

    private static final String server = "http://finance.yahoo.com/d/quotes.csv?s=";
    private static final String [] states = {STATE_BELOW, STATE_ABOVE};
    private static final String FORMAT_QUERY = "&f=l1vhgm4p2d1t1";

    private static final Gson gson = new GsonBuilder().serializeNulls().create();


    public static final String STOCK = "stock";
    public static final String THRESHOLD = "threshold";
    public static final String MOVING_AVERAGE = "moving_average";
    public static final String PRICE = "price";
    public static final String VOLUME = "volume";
    public static final String PERCENT = "percent";
    public static final String HIGH = "high";
    public static final String LOW = "low";
    public static final String FORMULA_DEFINITION = "formula";


    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, Object> propertiesMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(STOCK, new PropertyType(DataType.STRING, true, false));
        map.put(THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        return map;
    }

    @Override
    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
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
        return "Stock exchange sensor";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        Object stockObject = getProperty(STOCK);
        if(stockObject == null || stockObject.equals("")){
            return new SensorErrorMessage(STOCK + " property missing");
        }

        Object thresholdObject = getProperty(THRESHOLD);
        if(thresholdObject == null || thresholdObject.equals("")){
            return new SensorErrorMessage(THRESHOLD + " property missing");
        }

        Double threshold;
        try {
            threshold = Utils.getDouble(thresholdObject);
            log.debug("Properties are " + getProperty(STOCK) + ", " + threshold);
        }catch(NumberFormatException e){
            return new SensorErrorMessage("Could not parse " + THRESHOLD + " as number: " + e.getMessage());
        }

        final Map<String, Double> resultMap;
        try {
            resultMap = loadStock(stockObject.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new SensorErrorMessage(e.getMessage());
        }

//        Map<String, Double> withLowercasedKeys = resultMap.entrySet().stream()
//                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));

        //this might update the result map so needs to be called before creating the final raw data
        String state;
        try {
            state = getObserverState(resultMap, threshold);
        }catch(IllegalStateException e){
            log.error(e.getMessage(), e);
            return new SensorErrorMessage(e.getMessage());
        }

        String rawData = gson.toJson(gson.toJsonTree(resultMap));

        return SensorResultBuilder
                .success()
                .withRawData(rawData)
                .withObserverState(state)
                .build();
    }

    @Override
    public Map<String, RawDataType> getProducedRawData() {
        Map<String, RawDataType> map = new ConcurrentHashMap<>();
        map.put("moving_average", new RawDataType("double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        map.put("high", new RawDataType("double", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("price", new RawDataType("double", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("low", new RawDataType("double", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("percent", new RawDataType("double", DataType.DOUBLE, true, CollectedType.INSTANT));
        map.put("volume", new RawDataType("double", DataType.DOUBLE, true, CollectedType.INSTANT));
        return map;
    }

    @Override
    public String getName() {
        return getSensorName();
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet<>(Arrays.asList(states));
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }

    protected abstract String getSensorName();

    /**
     * Calculates the state and can if needed add values to the raw data
     *
     * @param rawData
     * @param threshold
     * @return the state or null if the sensor was not able to calculate it
     */
    protected abstract String getObserverState(Map<String, Double> rawData, Double threshold) throws IllegalStateException;

    private Map<String, Double> loadStock(String symbol) throws IOException{

        String urlPath = server + symbol + FORMAT_QUERY;

        Rest.RestReponse response = Rest.httpGet(urlPath);
        String responseBody = response.body();
        log.debug("Response " + response.status() + " for " + urlPath + " >>" + responseBody);

        final Map<String, Double> resultMap = new HashMap<>();

        StringTokenizer stringTokenizer = new StringTokenizer(responseBody, ",");
        parseOutput(PRICE, resultMap, stringTokenizer);
        parseOutput(VOLUME, resultMap, stringTokenizer);
        parseOutput(HIGH, resultMap, stringTokenizer);
        parseOutput(LOW, resultMap, stringTokenizer);
        parseOutput(MOVING_AVERAGE, resultMap, stringTokenizer);
        parseOutput(PERCENT, resultMap, stringTokenizer);

        // yahoo returns status OK with body 0.00,N/A,N/A,N/A,N/A,"N/A","N/A","12:11am"
        if(resultMap.get(PRICE) == 0.00){
            throw new IOException("No stock with symbol " + symbol + " found");
        }

        //date:time
        SimpleDateFormat format = new SimpleDateFormat("\"MM/dd/yyyy\" \"HH:mma\"");
        String dateString = stringTokenizer.nextToken() + " " + stringTokenizer.nextToken();
        try {
            Date parsed = format.parse(dateString);
            log.debug("Date is " + parsed.toString());
        } catch (ParseException e) {
            throw new IOException(e);
        }

        return resultMap;
    }

    private void parseOutput(String key, Map<String, Double> parsing, StringTokenizer stringTokenizer) throws IOException {
        try{
            String string = stringTokenizer.nextToken();
            String clean = string.replaceAll("%", "").replaceAll("\"", "").trim();
            Double value = null;
            if(!"N/A".equals(clean)){
                value = Double.parseDouble(clean);
            }
            log.debug(key + " = " + value);
            parsing.put(key, value);
        } catch (NumberFormatException e){
            throw new IOException("Error parsing [" + key + "] " + e.getMessage(), e);
        }
    }
}
