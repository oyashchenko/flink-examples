package com.oyashchenko.flink;

import com.oyashchenko.cache.model.Position;
import com.oyashchenko.cache.model.PriceTick;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class Utils {


    public static List<PriceTick> generatePrice() {
        return new Generator<PriceTick>().generate(1, true,
            (Integer i) -> {
                Integer secId = generateSecId();
                return  new PriceTick(secId,String.valueOf(i), secId * 0.5d, "USD");
            });
    }

    public static List<Position> generatePosition() {
        return new Generator<Position>().generate(
                100, true,
                (Integer i) -> {
                    Integer secId = generateSecId();
                    return  new Position(secId,1,secId * 10d, "USD", 1d);
                });
    }

    private static class Generator<T> {


        List<T> generate(long delay, boolean isLivePlay, Function<Integer, T> makeObj) {
            List<T> list = new CopyOnWriteArrayList<>();
            for (int i = 0; i< 1000; i++) {
                list.add(makeObj.apply(i));
            }
            if (isLivePlay) {
                Thread thread = new Thread(()-> {
                      for (int i = 0 ; i < 30000000 ; i++) {

                          list.add(makeObj.apply(i));
                          try {
                              Thread.sleep(delay);
                          } catch (InterruptedException e) {
                              throw new RuntimeException(e);
                          }
                      }

                });
                thread.start();

            }
            return list;
        }
    }

    public static int generateSecId() {
        //[1-100]
        return 1 + (int) (Math.random() * (100 -1));
    }
}
