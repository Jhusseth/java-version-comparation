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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CalculatorHandler {
    private static final int DEFAULT_COMPLEXITY = 50_000;
    private static final int BASE_SIZE = 10_000;
    private static final int MIN_SLEEP_MS = 100;
    private static final int MAX_ADDITIONAL_SLEEP_MS = 400;

    private final Timer computeTimer;
    private final Counter eventCounter;
    private final ExecutorService virtualThreadExecutor;

    public CalculatorHandler(MeterRegistry registry) {
        this.computeTimer = Timer.builder("calculator.compute.timer")
            .description("Tiempo de cómputo de cálculos pesados")
            .register(registry);

        this.eventCounter = Counter.builder("calculator_events_sent_total")
            .description("Total de eventos procesados")
            .register(registry);

        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public Mono<ServerResponse> compute(ServerRequest req) {
        var complexity = parseComplexity(req);

        return Mono.fromCallable(() -> executeComputation(complexity))
            .subscribeOn(Schedulers.fromExecutor(virtualThreadExecutor))
            .flatMap(result -> ServerResponse.ok().bodyValue(result))
            .onErrorResume(this::handleError);
    }

    private int parseComplexity(ServerRequest req) {
        return req.queryParam("complexity")
            .map(Integer::parseInt)
            .filter(c -> c > 0 && c <= 1_000_000)
            .orElse(DEFAULT_COMPLEXITY);
    }

    private String executeComputation(int complexity) throws Exception {
        return computeTimer.recordCallable(() -> {
            performHeavyCalculation(complexity);
            eventCounter.increment();
            return "OK";
        });
    }

    private void performHeavyCalculation(int complexity) throws InterruptedException {
        var random = ThreadLocalRandom.current();
        var size = BASE_SIZE + random.nextInt(complexity);
        var data = new double[size];
        for (int i = 0; i < size; i++) {
            double randValue1 = random.nextDouble();
            double randValue2 = random.nextDouble();
            data[i] = Math.sin(randValue1) * Math.log1p(randValue2);
        }
        var sleepDuration = Duration.ofMillis(MIN_SLEEP_MS + random.nextInt(MAX_ADDITIONAL_SLEEP_MS));
        Thread.sleep(sleepDuration);
    }

    private Mono<ServerResponse> handleError(Throwable error) {
        return ServerResponse.badRequest()
            .bodyValue("Error en el cálculo: " + error.getMessage());
    }
}