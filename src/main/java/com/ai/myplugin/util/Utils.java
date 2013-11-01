package com.ai.myplugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static Double getDouble(Object obj){
        Number number = null;
        if(obj instanceof String){
            try {
                number = Double.parseDouble((String) obj);
            } catch(NumberFormatException e) {
                try {
                    number = Float.parseFloat((String) obj);
                } catch(NumberFormatException e1) {
                    try {
                        number = Long.parseLong((String) obj);
                    } catch(NumberFormatException e2) {
                        try {
                            number = Integer.parseInt((String) obj);
                        } catch(NumberFormatException e3) {
                            throw e3;
                        }
                    }
                }
            }
        } else{
            number = (Number) obj;
        }
        return number.doubleValue();
    }

    public static void main(String []args) {
        log.debug(Utils.getDouble(new Integer(23)));
        log.debug(Utils.getDouble(new Double(23)));
        log.debug(Utils.getDouble("23"));
        log.debug(Utils.getDouble(23));
    }
}
