package com.oyashchenko.flink.source;

import com.oyashchenko.flink.Utils;
import com.oyashchenko.cache.model.PriceTick;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class PriceSource implements SourceFunction<PriceTick> {
    private static final Logger LOG = LoggerFactory.getLogger(PriceSource.class);
    private static final long PER_TIME = 1; //1s

    @Override
    public void run(SourceContext<PriceTick> sourceContext) throws Exception {
        try (Stream<String> lines = Files.lines(Paths.get("price.csv"))) {
            lines.forEach(line -> {
                String[] values = line.split(",");
                sourceContext.collect(
                        new PriceTick(Integer.parseInt(values[0]), values[1], Double.parseDouble(values[2]), values[3])
                );
            });
        }
        //generate(sourceContext);
    }

    private void generate(SourceContext<PriceTick> sourceContext) throws IOException, InterruptedException {
        try(FileWriter fr = new FileWriter("price.csv")) {

            long startTime = System.currentTimeMillis();
            long eventsSnap = startTime;
            long eventsPerSec = 0;
            String ccy = "USD";
            String lineEnding = System.getProperty("line.separator");
            for (int i = 0; i < 30000000; i++) {
                Integer secId = Utils.generateSecId();
                Double price = secId * 0.5d;
                fr.write(String.join(",", secId.toString(), String.valueOf(secId), price.toString(), ccy) + lineEnding);
                sourceContext.collect(
                        new PriceTick(secId, String.valueOf(secId), price, ccy)
                );

                long current = System.currentTimeMillis();
                long diff = current - eventsSnap;
                long sec = TimeUnit.MILLISECONDS.toSeconds(diff);
                long diffFromStart = current - startTime;

                if (diffFromStart > 300000 && diffFromStart < 600000) {//5 min {
                    Thread.sleep(10);
                }

                if (diffFromStart >= 600000) {//5 min {
                    Thread.sleep(10);
                }
                long volume = eventsPerSec / (sec == 0 ? 1 : sec);


                if (sec >= PER_TIME) {

                    //LOG.info("EVENTS : " + eventsPerSec  + ": " + sec  +". Per 1sec = " + volume);
             /*if (volume <=400) {
                 LOG.info("FORCE SLOWDOWN is starting: {}. Events : {}, seconds : {}",volume, eventsPerSec, sec);
                 Thread.sleep(100);
                 LOG.info("FORCE SLOWDOWN ");
                 skip = false;
             } else {
                 skip= true;
                 LOG.info("Should be Skip ric :" + i);
             }*/

                    eventsPerSec = 1;
                    eventsSnap = current;
                } else {
                    eventsPerSec++;
                }
                //   LOG.info("Added price tick" + i);
            }
        }
    }

    /*private Function<Integer, PriceTick> makeObject = ( i -> {
        Integer secId = Utils.generateSecId();
        PriceTick usd = new PriceTick(secId, String.valueOf(i), secId * 0.5d, "USD");
        return usd;
    });*/



    @Override
    public void cancel() {

    }
}
