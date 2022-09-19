package com.oyashchenko.flink.source;


import com.oyashchenko.flink.Utils;
import com.oyashchenko.cache.model.Position;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class PositionSource implements SourceFunction<Position> {

    private static final String POSITIONS_CSV = "positions.csv";

    @Override
    public void run(SourceContext<Position> sourceContext) throws Exception {
       // generate(sourceContext);

        try (Stream<String> lines = Utils.readLines(POSITIONS_CSV)) {
            lines.forEach(
                    line -> {
                        String[] values = line.split(",");
                        sourceContext.collect(
                                new Position(Integer.parseInt(values[1]), Integer.parseInt(values[0]),
                                        Double.parseDouble(values[2]), values[3], Double.parseDouble(values[4]))
                        );
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }



    private void generate(SourceContext<Position> sourceContext) throws IOException {
        try (FileWriter wr = new FileWriter(POSITIONS_CSV)) {

            String ccy = "USD";
            double fx = 1d;
            String lineEnding = System.getProperty("line.separator");
            for (int i = 0; i < 30000000; i++) {
                int secId = Utils.generateSecId();
                int legalEntityId = 1;
                if (i % 2 == 0) {
                    legalEntityId = 2;
                }
                double quantity = secId * 10d;

                wr.write(String.join(",", Integer.toString(legalEntityId), Integer.toString(secId),
                    Double.toString(quantity), ccy, Double.toString(fx)) + lineEnding);

            }
        }
    }

    @Override
    public void cancel() {

    }
}




