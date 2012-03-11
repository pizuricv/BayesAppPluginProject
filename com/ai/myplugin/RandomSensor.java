package com.ai.myplugin;

import com.ai.bayes.model.BayesianNetwork;
import com.ai.bayes.model.Pair;
import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@PluginImplementation
public class RandomSensor implements BNSensorPlugin {

    private Map<String, Object> map = new HashMap<String, Object>();
    private String nodeName;
    private BayesianNetwork bayesianNetwork;

    public TestResult execute(TestSessionContext testSessionContext) {

        List<Pair<Double, String>> probs = bayesianNetwork.getProbabilities(getNodeName());

        double [] coins = new double[probs.size()];
        double incr = 0;
        double value;

        System.out.println("assign priors for the game");
        for(int i = 0; i < probs.size(); i++ ){
            value = probs.get(i).fst ;
            coins[i] = value + incr;
            incr += value;
            System.out.println("for state" + probs.get(i).snd + " assign the coin value " + value);
        }

        double val = Math.random();
        final String   observedState = probs.get(findStateIndexForVal(val, coins)).snd;
        return new TestResult() {
            public boolean isSuccess() {
                return true;
            }

            public String getName() {
                return getNodeName();
            }

            public String getObserverState() {
                return observedState;
            }
        } ;
    }

    public BNSensorPlugin getNewInstance() {
        return new RandomSensor();
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public void setBayesianNetwork(BayesianNetwork bayesianNetwork) {
        this.bayesianNetwork = bayesianNetwork;
    }

    private int findStateIndexForVal(double val, double[] coins) {
        for(int i = 0; i< coins.length; i ++){
            if(val < coins [i]) {
                return i;
            }
        }
        return coins.length - 1;
    }


    public String getName() {
        return "Random Plugin Sensor";
    }

    public String[] getSupportedStates() {
        return bayesianNetwork == null ?  null: bayesianNetwork.getStates(getNodeName());
    }

    public String[] getRequiredProperties() {
        return new String[]{};
    }

    public void setProperty(String string, Object obj) {
        map.put(string, obj);
    }

    public Object getProperty(String string) {
        return map.get(string);
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getDescription() {
        return "Random Plugin Sensor that generates random states (with prob. distribution according to the priors)";
    }
}
