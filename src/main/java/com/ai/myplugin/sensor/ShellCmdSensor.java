/**
 * User: pizuricv
 * Date: 12/20/12
 */
package com.ai.myplugin.sensor;

import com.ai.api.*;
import com.ai.myplugin.util.io.Exec;
import com.ai.myplugin.util.io.ExecResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@PluginImplementation
public class ShellCmdSensor implements SensorPlugin {

    private static final Logger log = LoggerFactory.getLogger(ShellCmdSensor.class);

    public static final String PROPERTY_THRESHOLD = "threshold";
    public static final String PROPERTY_COMMAND = "command";

    private String command;
    private List<Long> thresholds = new ArrayList<>();
    private List<String> states = new ArrayList<>();
    private static final String parseString = "result=";

    private static final String NAME = "ShellCommand";

    @Override
    public Map<String,PropertyType> getRequiredProperties() {
        Map<String,PropertyType> map = new HashMap<>();
        map.put(PROPERTY_THRESHOLD, new PropertyType(DataType.DOUBLE, true, false));
        map.put(PROPERTY_COMMAND, new PropertyType(DataType.STRING, true, false));
        return map;
    }

    @Override
    public Map<String,PropertyType> getRuntimeProperties() {
        return new HashMap<>();
    }

    //comma separated list of thresholds
    @Override
    public void setProperty(String property, Object value) {
        switch(property){
            case PROPERTY_COMMAND:
                command = value.toString();
                break;
            case PROPERTY_THRESHOLD:
                if(value instanceof String)  {
                    String input = (String) value;
                    StringTokenizer stringTokenizer = new StringTokenizer(input, ",");
                    int i = 0;
                    states.add("level_"+ i++);
                    while(stringTokenizer.hasMoreElements()){
                        thresholds.add(Long.parseLong(stringTokenizer.nextToken()));
                        states.add("level_"+ i++);
                    }
                } else {
                    thresholds.add((Long) value);
                    states.add("level_0");
                    states.add("level_1");
                }
                Collections.reverse(thresholds);
                break;
            default:
                // ignore
        }
    }

    @Override
    public Object getProperty(String property) {
        switch(property) {
            case PROPERTY_COMMAND:
                return command;
            case PROPERTY_THRESHOLD:
                return thresholds
                        .stream()
                        .sorted() // undo previous reverse sorting
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
            default:
                throw new RuntimeException("Property " + property + " not recognised by " + getName());
        }
    }

    @Override
    public String getDescription() {
        return "Shell script, in order to parse the result correctly, add the line in the script in format result=$RES\n" +
                "example: \"result=5\", and 5 will be compared to the threshold\n" +
                "the result state is in a format level_$num, and the number of states is the number_of_thresholds+1";
    }

    @Override
    public SensorResult execute(SessionContext testSessionContext) {
        log.info("execute "+ getName() + ", sensor type:" +this.getClass().getName());
        Runtime rt = Runtime.getRuntime();
        try {
            Process process = rt.exec(command);

            ExecResult execResult = Exec.awaitTermination(process, log);

            log.debug(getName() + " ExitValue: " + execResult.exitVal);

            String result = "";
            BufferedReader br = new BufferedReader(new StringReader(execResult.output));
            String line;
            while ((line = br.readLine()) != null) {
                log.debug("Found result " + line);
                result = mapResult(line.replaceAll(parseString, ""));
            }

            final String finalState = result;

            return new SensorResult() {

                @Override
                public boolean isSuccess() {
                    return  execResult.exitVal == 0 && !("error").equals("command");
                }

                @Override
                public String getObserverState() {
                    return finalState;
                }

                @Override
                public List<Map<String, Number>> getObserverStates() {
                    return Collections.emptyList();
                }

                @Override
                public String getRawData() {
                    return execResult.output;
                }
            };

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> getSupportedStates() {
        return new HashSet(states);
    }

    private String mapResult(String result) {
        Long res = Long.parseLong(result);
        int i = states.size() - 1;
        for(Long l : thresholds){
            if(res  > l){
                return "level_" + i;
            }
            i --;
        }
        return "level_0";
    }

    @Override
    public void setup(SessionContext testSessionContext) {
        log.debug("Setup : " + getName() + ", sensor : "+this.getClass().getName());
    }

    @Override
    public void shutdown(SessionContext testSessionContext) {
        log.debug("Shutdown : " + getName() + ", sensor : "+this.getClass().getName());
    }
}
