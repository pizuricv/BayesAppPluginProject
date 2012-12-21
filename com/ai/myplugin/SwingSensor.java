/**
 * created by: Veselin Pizurica
 * Date: 11/03/12
 */

package com.ai.myplugin;

import com.ai.bayes.model.BayesianNetwork;
import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.NodeSessionParams;
import com.ai.util.resource.TestSessionContext;
import com.ai.util.swing.SwingUtils;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.swing.*;
@PluginImplementation
public class SwingSensor implements BNSensorPlugin {

    private String question = null;

    public String[] getRequiredProperties() {
        return new String[] {"Question to ask"};
    }

    public void setProperty(String string, Object obj) {
        question = obj.toString();

    }

    public Object getProperty(String string) {
        return question;
    }

    public String getDescription() {
        return "Swing Sensor that let you define the return state in GUI";
    }

    public BNSensorPlugin getNewInstance() {
        return new SwingSensor();
    }
    
    private String getQuestion(String node){
        return question == null || question.startsWith("No Value")? "What is the result of " + node: question;
    }

    public TestResult execute(TestSessionContext testSessionContext) {
        final String nodeName = (String) testSessionContext.getAttribute(NodeSessionParams.NODE_NAME);
        final BayesianNetwork bayesianNetwork = (BayesianNetwork) testSessionContext.getAttribute(NodeSessionParams.BN_NETWORK);
        JPanel panel = (JPanel) testSessionContext.getAttribute("panel", null);

        if(panel == null ){
            throw new RuntimeException("Missing attributes");
        }
        final int index = JOptionPane.showOptionDialog(SwingUtils.getFrame(panel),
                getQuestion(nodeName), "", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                bayesianNetwork.getStates(nodeName), bayesianNetwork.getStates(nodeName)[0]);

        return new TestResult() {

            public boolean isSuccess() {
                return true;
            }

            public String getName() {
                return "Swing Result";
            }

            public String getObserverState() {
                return bayesianNetwork.getStates(nodeName)[index];
            }
        };

    }

    public String getName() {
        return "Swing Test";
    }

    public String[] getSupportedStates() {
        return new String[] {};
    }
}
