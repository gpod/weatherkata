package com.od.weatherkata.subscriber;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Created by Nick E on 27/01/2015.
 */
public class WeatherSubscriber {

    private SocketSubscriber socketSubscriber = new SocketSubscriber();

    private Flux<String> precipitation = socketSubscriber.getPrecipitationObservable();
    private Flux<Integer> temperature = socketSubscriber.getTemperatureObservable();
    private Flux<Integer> windStrength = socketSubscriber.getWindStrengthObservable();
    private Flux<Integer> pressureLow = socketSubscriber.getLowPressureObservable();
    private Flux<Integer> pressureHigh = socketSubscriber.getHighPressureObservable();
    private Flux<Map<String,Integer>> pressureDeltas = socketSubscriber.getPressureDeltasObservable();

    private WeatherSubscriberControl uiControl;

    public WeatherSubscriber(WeatherSubscriberControl uiControl) {
        this.uiControl = uiControl;
        connectStatusPanel();
        connectSnowMobile();
        connectBalloon();
        connectTrain();
        connectPressure();
        connectPressureDifference();
    }

    private void connectStatusPanel() {        
        temperature.distinctUntilChanged().subscribe(uiControl::setTemperature);
        precipitation.distinctUntilChanged().subscribe(uiControl::setPrecipitation);
        windStrength.distinctUntilChanged().subscribe(uiControl::setWindStrength);        
    }

    private void connectSnowMobile() {
        temperature.map(t -> t <= 0)
                .distinctUntilChanged()
                .subscribe(uiControl::setSnowMobileEnabled);
    }

    private void connectBalloon() {
        Flux<Boolean> goodWind = windStrength.map(w -> w < 5);
        Flux<Boolean> noFish = precipitation.map(p -> !p.equals("Fish"));
        Flux.combineLatest(goodWind, noFish, (w, f) -> w && f)
                .distinctUntilChanged()
                .subscribe(uiControl::setBalloonEnabled);
    }

    private void connectTrain() {
        Flux<Boolean> goodTemp = temperature.map(t -> t == 18);
        Flux<Boolean> noWind = windStrength.map(w -> w == 0);
        Flux<Boolean> fishing = precipitation.map("Fish"::equals);

        Flux<Boolean> canCommute = goodTemp
                .withLatestFrom(noWind, (t, w) -> t && w)
                .withLatestFrom(fishing, (c, p) -> c && p);

        canCommute.distinctUntilChanged().subscribe(uiControl::setTrainEnabled);
    }

    private void connectPressure() {
        pressureLow.distinctUntilChanged().subscribe(uiControl::setLowPressure);
        pressureHigh.distinctUntilChanged().subscribe(uiControl::setHighPressure);        
    }

    private void connectPressureDifference() {
        Flux<Integer> differences = pressureDeltas
                .flatMap(d -> {
                    Mono<Integer> high = Mono.justOrEmpty(d.get("highPressure"));
                    Mono<Integer> low = Mono.justOrEmpty(d.get("lowPressure"));
                    return high.zipWith(low, (h, l) -> h - l);
                });
        
        differences.subscribe(uiControl::setPressureDifference);
    }

    public void subscribe() {
        socketSubscriber.subscribe();
    }

}
