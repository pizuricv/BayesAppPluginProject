/**
 * created by: Veselin Pizurica
 * Date: 11/03/12
 */

package com.ai.myplugin;

import com.ai.bayes.model.BayesianNetwork;
import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import com.ai.util.swing.SwingUtils;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.swing.*;
@PluginImplementation
public class SwingSensor implements BNSensorPlugin {

    String nodeName = null;
    private BayesianNetwork bayesianNetwork;

    public String[] getRequiredProperties() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setProperty(String string, Object obj) {

    }

    public Object getProperty(String string) {
        return null;
    }

    public String getDescription() {
        return "Swing Sensor that let you define the return state in GUI";
    }

    public BNSensorPlugin getNewInstance() {
        return new SwingSensor();
    }

    @Override
    public void setNodeName(String node) {
        nodeName = node;
    }

    @Override
    public void setBayesianNetwork(BayesianNetwork bayesianNetwork) {
        this.bayesianNetwork = bayesianNetwork;
    }

    public String getNodeName() {
        return nodeName;
    }

    public TestResult execute(TestSessionContext testSessionContext) {
        JPanel panel = (JPanel) testSessionContext.getAttribute("panel", null);

        if(panel == null ){
            throw new RuntimeException("Missing attributes");
        }
        final int index = JOptionPane.showOptionDialog(SwingUtils.getFrame(panel),
                "What is the result of " + getNodeName(), "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                getSupportedStates(), getSupportedStates()[0]);

        return new TestResult() {

            public boolean isSuccess() {
                return true;
            }

            public String getName() {
                return nodeName;
            }

            public String getObserverState() {
                return getSupportedStates()[index];
            }
        };

    }

    public String getName() {
        return "Swing Sensor";
    }

    public String[] getSupportedStates() {
        return bayesianNetwork == null ? null: bayesianNetwork.getStates(getNodeName());
    }
}
