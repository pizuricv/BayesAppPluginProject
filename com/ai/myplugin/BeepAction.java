/**
 * User: pizuricv
 * Date: 10/29/12
 */
package com.ai.myplugin;

import com.ai.bayes.plugins.BNActionPlugin;
import com.ai.bayes.scenario.ActionResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class BeepAction implements BNActionPlugin{
    @Override
    public String[] getRequiredProperties() {
        return new String[0];
    }

    @Override
    public void setProperty(String s, Object o) {
    }

    @Override
    public Object getProperty(String s) {
        return null;
    }

    @Override
    public String getDescription() {
        return "Simple beep signal";
    }

    @Override
    public BNActionPlugin getNewInstance() {
        return new BeepAction();
    }

    @Override
    public ActionResult action(TestSessionContext testSessionContext) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        return new ActionResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getObserverState() {
                return null;
            }
        };
    }

    @Override
    public String getName() {
        return "Beep action";
    }
}
