package com.oyashchenko.flink.source;


import com.oyashchenko.flink.Utils;
import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PriceTick;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class PositionSource implements SourceFunction<Position> {

    @Override
    public void run(SourceContext<Position> sourceContext) throws Exception {
       // generate(sourceContext);
        Stream<String> lines = Files.lines(Paths.get("positions.csv"));
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



    private void generate(SourceContext<Position> sourceContext) throws IOException {
        try (FileWriter wr = new FileWriter("positions.csv")) {

            String ccy = "USD";
            Double fx = 1D;
            String lineEnding = System.getProperty("line.separator");
            for (int i = 0; i < 30000000; i++) {
                Integer secId = Utils.generateSecId();
                int legalEntityId = 1;
                if (i % 2 == 0) {
                    legalEntityId = 2;
                }
                Double quantity = secId * 10d;

                wr.write(String.join(",", Integer.toString(legalEntityId), secId.toString(),
                    quantity.toString(), ccy, fx.toString()
                ) + lineEnding);

            }
        }
    }

    @Override
    public void cancel() {

    }
}




