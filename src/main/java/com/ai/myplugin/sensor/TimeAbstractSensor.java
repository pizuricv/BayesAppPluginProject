/**
 * User: pizuricv
 * Date: 5/17/13
 */

package com.ai.myplugin.sensor;


import com.ai.bayes.plugins.BNSensorPlugin;
import com.ai.bayes.scenario.TestResult;
import com.ai.util.resource.TestSessionContext;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class TimeAbstractSensor implements BNSensorPlugin{
    String timeZone;
    private String dateString;
    String [] returnStates;
    protected static final String TIME_ZONE = "timezone";
    protected static final String DATE_FORMAT = "date";
    protected static final String HOUR = "HOUR";
    protected static final String DAY = "DAY";
    protected static final String DAY_WEEK = "DAY_WEEK";
    protected static final String DATE = "DATE";
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
        else if(DATE_FORMAT.equalsIgnoreCase(s))
            dateString = (String) o;
        else {
           throw new RuntimeException("Property "+ s + " not in the required settings");
        }
    }

    @Override
    public Object getProperty(String s) {
        if(TIME_ZONE.equalsIgnoreCase(s))
            return timeZone;
        else if(DATE_FORMAT.equalsIgnoreCase(s))
            return dateString;
        else
            return null;
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
                GregorianCalendar gregorianCalendar =  getProperty(TIME_ZONE) == null? new GregorianCalendar():
                        new GregorianCalendar(TimeZone.getTimeZone((String) getProperty(TIME_ZONE)));
                if(HOUR.equalsIgnoreCase(getTag())) {
                    return String.valueOf(gregorianCalendar.get(Calendar.HOUR_OF_DAY));
                } else if(DAY_WEEK.equalsIgnoreCase(getTag())) {
                    return new DateFormatSymbols().getWeekdays()[gregorianCalendar.get(Calendar.DAY_OF_WEEK)];
                } else if(DAY.equalsIgnoreCase(getTag())) {
                    return String.valueOf(gregorianCalendar.get(Calendar.DAY_OF_MONTH));
                } else if(WEEK.equalsIgnoreCase(getTag())) {
                    return String.valueOf(gregorianCalendar.get(Calendar.WEEK_OF_MONTH));
                } else if(MONTH.equalsIgnoreCase(getTag())) {
                    return new DateFormatSymbols().getMonths()[gregorianCalendar.get(Calendar.MONTH)];
                } else if(DATE.equalsIgnoreCase(getTag())) {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        GregorianCalendar dateToCompare =  new GregorianCalendar();
                        dateToCompare.setTime(isoFormat.parse((String) getProperty(DATE_FORMAT)));
                        return gregorianCalendar.get(Calendar.YEAR) == dateToCompare.get(Calendar.YEAR)&&
                                gregorianCalendar.get(Calendar.MONTH) == dateToCompare.get(Calendar.MONTH) &&
                                gregorianCalendar.get(Calendar.DAY_OF_MONTH) == dateToCompare.get(Calendar.DAY_OF_MONTH) ?  "TRUE" : "FALSE";
                    } catch (ParseException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                return "";
            }

            @Override
            public List<Map<String, Number>> getObserverStates() {
                return null;
            }

            ;

            @Override
            public String getRawData(){
                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
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
            returnStates = new String []{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17",
                    "18","19","20","21","22","23","24"};
        } else if(getTag().equalsIgnoreCase(DAY_WEEK)) {
            returnStates = new String []{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        } else if(getTag().equalsIgnoreCase(DAY)) {
            returnStates = new String []{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17",
                    "18","19","20","21","22","23","24", "25", "26", "27", "28", "29", "30", "31"};
        } else if(getTag().equalsIgnoreCase(WEEK)) {
            returnStates = new String []{"1","2","3","4","5"};
        } else if(getTag().equalsIgnoreCase(MONTH)) {
            returnStates = new String []{"January","February","March","April","May","June","July",
                    "August","September","October","November","December"};
        } else if(getTag().equalsIgnoreCase(DATE)) {
            returnStates = new String []{"TRUE", "FALSE"};
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
