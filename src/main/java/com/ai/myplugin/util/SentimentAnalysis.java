package com.ai.myplugin.util;

/**
 * Created by User: veselin
 * On Date: 09/11/13
 */
public class SentimentAnalysis {
    public static String SEPARATOR = "( )|(\\|)";
    private static String [] positiveTerms = new String[] {":)", "great", "super", "awesome", "nice", "lol", "cute", "happy", "good", "love"};
    private static String [] negativeTerms = new String[] {":(", "bad", "ugly", "fuck", "sad", "shit", "nasty", "unhappy", "hate"};


    public static boolean isMatching(String searchTerms, String text) {
        final String [] searchSep = searchTerms.split(SentimentAnalysis.SEPARATOR);
        for (String token: searchSep){
            if (text.toLowerCase().indexOf(token.toLowerCase()) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return 0 for neutral, 1 for positive and -1 for negative
     * @param searchItem
     * @return
     */
    public static int sentimentForString(String searchItem) {
        for(String positiveString : positiveTerms){
            if(searchItem.indexOf(positiveString) > -1)
                if(searchItem.indexOf(" not ") > -1)
                    return -1;
                else
                    return 1;
        }
        for(String negativeString : negativeTerms){
            if(searchItem.indexOf(negativeString) > -1)
                if(searchItem.indexOf(" not ") > -1)
                    return 1;
                else
                    return -1;
        }
        return 0;
    }
}
