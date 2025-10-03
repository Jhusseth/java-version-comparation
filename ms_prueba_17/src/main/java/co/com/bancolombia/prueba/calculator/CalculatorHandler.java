package co.com.bancolombia.prueba.calculator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class CalculatorHandler {

    private final Timer computeTimer;
    private final Counter eventCounter;

    public CalculatorHandler(MeterRegistry registry) {
        this.computeTimer = Timer.builder("calculator.compute.timer")
            .description("Tiempo de cómputo de cálculos pesados")
            .register(registry);

        this.eventCounter = Counter.builder("calculator_events_sent_total")
            .description("Total de eventos procesados")
            .register(registry);
    }

    public Mono<ServerResponse> compute(ServerRequest req) {
        var complexity = Integer.parseInt(req.queryParam("complexity").orElse(String.valueOf(50_000)));
        return Mono.defer(() -> {
            long start = System.nanoTime();
            return Mono.fromCallable(() -> {
                    heavyCalculation(complexity);
                    eventCounter.increment();
                    return "OK";
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(r -> computeTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS));
        }).flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private void heavyCalculation(int complexity) throws InterruptedException {
        int size = 10_000 + ThreadLocalRandom.current().nextInt(complexity);
        double[] data = new double[size];
        Random rnd = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            data[i] = Math.sin(rnd.nextDouble()) * Math.log1p(rnd.nextDouble());
        }
        Thread.sleep(Duration.ofMillis(100 + rnd.nextInt(400)).toMillis());
    }
}
