package com.ai.myplugin.services;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RainResult {
    public final double min;
    public final double max;
    public final double avg;

    public final List<RainAmount> results;

    public RainResult(final double min, final double max, final double avg, final List<RainAmount> results) {
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.results = Collections.unmodifiableList(results);
    }



    @Override
    public String toString() {
        return "RainResult{" +
                "min=" + min +
                ", max=" + max +
                ", avg=" + avg +
                ", results=" + results +
                '}';
    }

    public static class RainAmount{
        public final Optional<Long> amount;
        public final LocalTime time;

        public RainAmount(Optional<Long> amount, LocalTime time) {
            this.amount = amount;
            this.time = time;
        }

//        0 is droog, 255 is zware regen.
//        mm/per uur = 10^((waarde -109)/32)
//        Dus 77 = 0.1 mm/uur


        @Override
        public String toString() {
            return "RainAmount{" +
                    "amount=" + amount +
                    ", time=" + time +
                    '}';
        }
    }
}
