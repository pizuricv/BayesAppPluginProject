package com.ai.myplugin.services;

import com.ai.myplugin.util.LatLng;
import com.ai.myplugin.util.Rest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


public class BuienradarService {

    private static final Logger log = LoggerFactory.getLogger(BuienradarService.class);

    //http://gps.buienradar.nl/getrr.php?lat=52&lon=4
    private static final String BASE_URL = "http://gps.buienradar.nl/getrr.php?";

    public Optional<RainResult> fetch(final LatLng latLng) throws IOException{
        String pathURL = BASE_URL + "lat="+latLng.latitude + "&lon="+latLng.longitude;

        // TODO do we want to return an empty Optional when we get a 503? Have already seen this case...
        String stringToParse = Rest.httpGet(pathURL).body();
        return parseResponse(stringToParse);
    }

    Optional<RainResult> parseResponse(final String stringToParse){
        log.debug("stringToParse:\n" + stringToParse);
        //000|10:20 000|10:25 000|10:30 000|10:35 000|10:40 000|10:45 000|10:50 000|10:55 000|11:00 000|11:05 000|11:10 000|11:15 000|11:20 000|11:25 000|11:30 000|11:35 000|11:40 000|11:45 000|11:50 000|11:55 000|12:00 000|12:05 000|12:10 000|12:15 000|12:20
        /*
         * Op basis van lat lon coördinaten kunt u de neerslag twee uur vooruit ophalen in tekst vorm. 0 is droog, 255 is zware regen.
         * mm/per uur = 10^((waarde -109)/32)
         * Dus 77 = 0.1 mm/uur
         */

        String[] hourParts = stringToParse.split("\\r?\\n");
        List<RainResult.RainAmount> timeAmounts = Arrays.stream(hourParts).map(amount -> {
            String[] timeParts = amount.split("\\|");
            LocalTime time = LocalTime.parse(timeParts[1]);
            if (timeParts[0].isEmpty()) {
                return new RainResult.RainAmount(Optional.empty(), time);
            } else {
                return new RainResult.RainAmount(Optional.of(Long.parseLong(timeParts[0])), time);
            }
        }).collect(Collectors.toList());

        // TODO these kinds of magic numbers are always a bad idea
        double avg = 0;
        double max = -1;
        double min = 255;
        // Could be done in a more functional way with a LongConsumer
        for(RainResult.RainAmount amount:timeAmounts){
            if(amount.amount.isPresent()) {
                if (min > amount.amount.get())
                    min = amount.amount.get();
                if (max < amount.amount.get())
                    max = amount.amount.get();
                avg += amount.amount.get();
            }
        }
        if(timeAmounts.size() == 0){
            return Optional.empty();
        }else {
            avg = avg / timeAmounts.size();
            return Optional.of(new RainResult(min, max, avg, timeAmounts));
        }
    }

}
