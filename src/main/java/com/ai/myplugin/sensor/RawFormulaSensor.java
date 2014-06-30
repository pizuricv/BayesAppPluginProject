/**
 * Created with IntelliJ IDEA.
 * User: pizuricv
 */

package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.FormulaParser;
import com.ai.myplugin.util.SensorResultBuilder;
import com.ai.myplugin.util.Utils;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@PluginImplementation
@PluginHeader(version = "1.0.1", author = "Veselin", category = "Event Processing", iconURL = "http://app.waylay.io/icons/formula.png")
public class RawFormulaSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(RawFormulaSensor.class);

    private static final String NAME = "RawFormula";

    private final FormulaParser formulaParser = new FormulaParser();

    private final String THRESHOLD = "threshold";
    private final String FORMULA = "formula";
    private final Map<String,Long> deltaMap = new ConcurrentHashMap<>();
    // if threshold is given as a list, then we will create states as the range
    private final List<String> configuredStates  = new ArrayList<>();
    private final List<Long> thresholds = new ArrayList<>();

    private final Map<String, Object> propertiesMap = new ConcurrentHashMap<>();

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String, PropertyType> map = new HashMap<>();
        map.put(THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        map.put(FORMULA, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String,PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }

    public void setProperty(String string, Object obj) {
        if(getRequiredProperties().keySet().contains(string)) {
            if(string.equalsIgnoreCase(THRESHOLD)){
                String input = obj.toString();
                input = input.replace("[","").replace("]","");
                StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                if(stringTokenizer.countTokens() > 1){
                    int i = 0;
                    configuredStates.add("level_"+ i++);
                    while(stringTokenizer.hasMoreElements()){
                        thresholds.add(Long.parseLong(stringTokenizer.nextToken().trim()));
                        configuredStates.add("level_"+ i++);
                    }
                    Collections.reverse(thresholds);
                } else
                    propertiesMap.put(string, obj);
             }else
                propertiesMap.put(string, obj);
        } else {
            throw new RuntimeException("Property "+ string + " not in the required settings");
        }
    }

    public Object getProperty(String string) {
        return propertiesMap.get(string);
    }

    private Double executeFormula(String formula) throws Exception {
        log.debug("executeFormula(" + formula + ")");
        return FormulaParser.executeFormula(formula);
    }


    @Override
    public String getDescription() {
        return "Parse raw data from the context";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        String parseFormula = (String) getProperty(FORMULA);
        log.debug("Formula to parse: "+parseFormula);

        double res = 0;
        if(parseFormula.contains("dt")) {
            Long prev = deltaMap.get("prevTime");
            if(prev == null)   {
                deltaMap.put("prevTime", System.currentTimeMillis()/1000);
                return SensorResultBuilder.failure().build();
            }
            Long currentTime = System.currentTimeMillis()/1000;
            deltaMap.put("prevTime", currentTime);
            parseFormula = parseFormula.replaceAll("dt", Long.toString(currentTime - prev));
        }

        boolean success = false;
        try {
            parseFormula = formulaParser.parseFormula(parseFormula, (Map<String, Object>) testSessionContext.getAttribute(SessionParams.RAW_DATA)) ;
            log.info("Formula to parse after processing: "+parseFormula);
            res = executeFormula(parseFormula);
            success = true;
        } catch (Exception e) {
            log.error(e.getLocalizedMessage() + ", for formula: "+ parseFormula);
        }
        if(!success) {
            return SensorResultBuilder.failure().build();
        }else {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("formulaValue", res);
            final double finalRes = res;
//            return SensorResultBuilder
//                    .success()
//                    .withObserverState(mapResult(finalRes))
//                    .withRawData(jsonObject)
//                    .build();
            // FIXME mapResult(finalRes) changes over time + tests fail when changed to above code
            return new SensorResult() {
                @Override
                public boolean isSuccess() {
                    return true;
                }

                @Override
                public String getObserverState() {
                    return mapResult(finalRes);
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return Collections.emptyList();
                }

                @Override
                public String getRawData() {
                    return jsonObject.toJSONString();
                }
            };
        }
    }

    @Override
    public Map<String, RawDataType> getRawDataTypes() {
        Map<String, RawDataType> map = new ConcurrentHashMap<>();
        map.put("formulaValue", new RawDataType("double", DataType.DOUBLE, true, CollectedType.COMPUTED));
        return map;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        if(configuredStates.isEmpty()) {
            return new HashSet<>(Arrays.asList(new String[]{"Above", "Equal", "Below"}));
        }else {
            return new HashSet<>(configuredStates);
        }
    }

    private String mapResult(Double value) {
        if(configuredStates.size() == 0){
            if(value.equals(Utils.getDouble(getProperty(THRESHOLD))))
                return "Equal";
            if(value > Utils.getDouble(getProperty(THRESHOLD)))
                return "Above";
            else
                return "Below";
        } else {
            int i = configuredStates.size() - 1;
            for(Long l : thresholds){
                if(value  > l){
                    return configuredStates.get(i);
                }
                i --;
            }
            return configuredStates.get(0);
        }
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
        formulaParser.restStats();
    }

}
