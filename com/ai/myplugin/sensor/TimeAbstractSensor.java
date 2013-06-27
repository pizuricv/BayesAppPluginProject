/**
 * User: pizuricv
 * Date: 5/17/13
 */

package com.ai.myplugin.sensor;


import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class TimeAbstractSensor implements BNSensorPlugin{
    String timeZone;
    String [] returnStates;
    protected static final String TIME_ZONE = "timezone";
    protected static final String HOUR = "HOUR";
    protected static final String DAY = "TIME";
    protected static final String WEEK = "WEEK";
    protected static final String MONTH = "MONTH";


    protected abstract String getTag();
    protected abstract String getSensorName();


    @Override
    public String[] getRequiredProperties() {
        return new String[] {TIME_ZONE};
    }

    //@TODO implement the time zone
    @Override
    public void setProperty(String s, Object o) {
        if(TIME_ZONE.equalsIgnoreCase(s))
            timeZone = (String) o;
        else {
           throw new RuntimeException("Property "+ s + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String s) {
        return timeZone;
    }

    @Override
    public String getDescription() {
        return "Returns hour, day(in a week) or month";
    }

    @Override
    public TestResult execute(TestSessionContext testSessionContext) {
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
                if(HOUR.equalsIgnoreCase(getTag())) {
                    return String.valueOf(new GregorianCalendar().get(Calendar.HOUR_OF_DAY));
                } else  if(DAY.equalsIgnoreCase(getTag())) {
                    return new DateFormatSymbols().getWeekdays()[new GregorianCalendar().get(Calendar.DAY_OF_WEEK)];
                } else if(WEEK.equalsIgnoreCase(getTag())) {
                    return String.valueOf(new GregorianCalendar().get(Calendar.WEEK_OF_MONTH));
                } else if(MONTH.equalsIgnoreCase(getTag())) {
                    return new DateFormatSymbols().getMonths()[new GregorianCalendar().get(Calendar.MONTH)];
                }
                return "";
            };

            @Override
            public String getRawData(){
                SimpleDateFormat date_format = new SimpleDateFormat("ddMMyyyy");
                String date = date_format.format((new GregorianCalendar().getTime()));
                return "{" +
                        "date: " + date +
                        "}";
            }
        };
    }

    @Override
    public String getName() {
        return getSensorName();
    }

    @Override
    public String[] getSupportedStates() {
        if(getTag().equalsIgnoreCase(HOUR)) {
            returnStates = new String []{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"};
        } else if(getTag().equalsIgnoreCase(DAY)) {
            returnStates = new String []{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        } else if(getTag().equalsIgnoreCase(WEEK)) {
            returnStates = new String []{"1","2","3","4","5"};
        }
        else if(getTag().equalsIgnoreCase(MONTH)) {
            returnStates = new String []{"January","February","March","April","May","June","July",
                    "August","September","October","November","December"};
        }
        return returnStates;
    }

    public static void main(String args []){
        TimeAbstractSensor timeSensor = new TimeAbstractSensor() {
            @Override
            protected String getTag() {
                return MONTH;
            }

            @Override
            protected String getSensorName() {
                return "";
            }
        };
        timeSensor.setProperty(TIME_ZONE, "");
        System.out.println(timeSensor.execute(null).getObserverState());
        System.out.println(Arrays.toString(timeSensor.getSupportedStates()));
    }
}
