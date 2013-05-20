/**
 * User: pizuricv
 * Date: 5/17/13
 */

package com.ai.myplugin;


import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.util.Calendar;
import java.util.GregorianCalendar;

@PluginImplementation
public class TimeSensor implements BNSensorPlugin{
    String property;
    String [] returnStates = new String[]{};

    @Override
    public String[] getRequiredProperties() {
        return new String[] {"Selection [hour] [day] [week] [month]"};
    }

    @Override
    public void setProperty(String s, Object o) {
        String input = (String) o;
        if("hour".equalsIgnoreCase(input)) {
            returnStates = new String []{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
        } else  if("day".equalsIgnoreCase(input)) {
            returnStates = new String []{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        } else  if("week".equalsIgnoreCase(input)) {
            returnStates = new String []{"1","2","3","4","5"};
        }
        else if("month".equalsIgnoreCase(input)) {
            returnStates = new String []{"January","February","March","April","May","June","July",
                    "August","September","October","November","December"};
        } else {
            throw new RuntimeException("Entry not recognized "+ input);
        }
        property = input;
    }

    @Override
    public Object getProperty(String s) {
        return property;
    }

    @Override
    public String getDescription() {
        return "Returns hour, day(in a week) or month, depending on the input property";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
        final Calendar calendar = new GregorianCalendar();
        return new TestResult() {
            @Override
            public boolean isSuccess() {
                return true;
            }

            @Override
            public String getName() {
                return "Date result";
            }

            @Override
            public String getObserverState() {
                if("hour".equalsIgnoreCase(property)) {
                    return String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                } else  if("day".equalsIgnoreCase(property)) {
                    return String.valueOf(calendar.get(Calendar.DAY_OF_WEEK));
                } else  if("week".equalsIgnoreCase(property)) {
                    return String.valueOf(calendar.get(Calendar.WEEK_OF_MONTH));
                }else if("month".equalsIgnoreCase(property)) {
                    return String.valueOf(calendar.get(Calendar.MONTH));
                }
                return "";
            };
        };
    }

    @Override
    public String getName() {
        return "DateTime result";
    }

    @Override
    public String[] getSupportedStates() {
        return returnStates;
    }
}
