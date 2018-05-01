package com.od.weatherkata.subscriber;

import javafx.application.Platform;
import org.zeromq.ZMQ;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick E on 29/01/2015.
 */
public class SocketSubscriber {

    private EmitterProcessor<String> precipitationObservable = EmitterProcessor.create();
    private EmitterProcessor<Integer> temperatureObservable = EmitterProcessor.create();
    private EmitterProcessor<Integer> windStrengthObservable = EmitterProcessor.create();

    private EmitterProcessor<Integer> lowPressureObservable = EmitterProcessor.create();
    private EmitterProcessor<Integer> highPressureObservable = EmitterProcessor.create();
    private EmitterProcessor<Map<String, Integer>> pressureDeltasObservable = EmitterProcessor.create();

    private ZMQ.Socket subscriber;
    private ZMQ.Context context;

    public void subscribe() {
        Runnable runnable = new Runnable() {
            public void run() {
                context = ZMQ.context(1);

// Socket to talk to server
                System.out.println("Collecting updates from weather server");
                subscriber = context.socket(ZMQ.SUB);
                subscriber.connect("tcp://localhost:5556");

                subscriber.subscribe("temp".getBytes());
                subscriber.subscribe("wind".getBytes());
                subscriber.subscribe("precipitation".getBytes());
                subscriber.subscribe("pressure".getBytes());
                System.out.println("Subscribed to all messages");

                Pattern p = Pattern.compile("(.+):\\{([-,\\w]+)\\}");

                int lastLow = -1, lastHigh = -1;

// Process 100 updates
                while(true) {
        // Use trim to remove the tailing '0' character
                    String message = subscriber.recvStr(0).trim();
                    System.out.println("Received " + message);

                    Matcher matcher = p.matcher(message);
                    boolean match = matcher.matches();
                    if ( match ) {
                        String type = matcher.group(1);
                        String val = matcher.group(2);
                        switch(type) {
                            case "temp" :
                                runLater(() -> {
                                    temperatureObservable.onNext(Integer.valueOf(val));
                                });
                                break;
                            case "wind" :
                                runLater(() -> {
                                    windStrengthObservable.onNext(Integer.valueOf(val));
                                });
                                break;
                            case "precipitation" :
                                runLater(() -> {
                                    precipitationObservable.onNext(val);
                                });
                                break;
                            case "pressure" :
                                String[] lowAndHigh = val.split(",");
                                int low = Integer.parseInt(lowAndHigh[0]);
                                int high = Integer.parseInt(lowAndHigh[1]);

                                runLater(() -> {
                                    lowPressureObservable.onNext(low);
                                });

                                runLater(() -> {
                                    highPressureObservable.onNext(high);
                                });

                                Map<String,Integer> deltas = new HashMap<String,Integer>();
                                if ( lastLow != low) {
                                    deltas.put("lowPressure", low);
                                    lastLow = low;
                                }

                                if ( lastHigh != high) {
                                    deltas.put("highPressure", high);
                                    lastHigh = high;
                                }

                                if ( ! deltas.isEmpty()) {
                                    pressureDeltasObservable.onNext(deltas);
                                }

                        }
                    }
                }
            }
        };

        new Thread(runnable).start();
    }

    //run on the JFX thread
    private void runLater(Runnable r) {
        Platform.runLater(r);
    }

    public Flux<String> getPrecipitationObservable() {
        return precipitationObservable;
    }

    public Flux<Integer> getTemperatureObservable() {
        return temperatureObservable;
    }

    public Flux<Integer> getWindStrengthObservable() {
        return windStrengthObservable;
    }
    
    public Flux<Integer> getHighPressureObservable() {
        return highPressureObservable;
    }

    public Flux<Integer> getLowPressureObservable() {
        return lowPressureObservable;
    }

    public Flux<Map<String, Integer>> getPressureDeltasObservable() {
        return pressureDeltasObservable;
    }
}
