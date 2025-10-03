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

record Complexity(int value) {
    private static final int MIN = 1;
    private static final int MAX = 1_000_000;
    private static final int DEFAULT = 50_000;

    public Complexity {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                "Complexity must be between %d and %d".formatted(MIN, MAX)
            );
        }
    }

    public static Complexity fromString(String str) {
        try {
            return new Complexity(Integer.parseInt(str));
        } catch (IllegalArgumentException _) {
            return defaultValue();
        }
    }

    public static Complexity defaultValue() {
        return new Complexity(DEFAULT);
    }
}

record ComputationResult(String status) {
    public static ComputationResult success() {
        return new ComputationResult("OK");
    }
}

record CalculationConfig(int baseSize, int minSleepMs, int maxAdditionalSleepMs) {
    private static final CalculationConfig DEFAULT =
        new CalculationConfig(10_000, 100, 400);

    public static CalculationConfig defaultConfig() {
        return DEFAULT;
    }
}

@Component
public class CalculatorHandler {

    private final Timer computeTimer;
    private final Counter eventCounter;
    private final CalculationConfig config;
    private final ExecutorService virtualThreadExecutor;

    public CalculatorHandler(MeterRegistry registry) {
        this.computeTimer = Timer.builder("calculator.compute.timer")
            .description("Tiempo de cómputo de cálculos pesados")
            .register(registry);

        this.eventCounter = Counter.builder("calculator_events_sent_total")
            .description("Total de eventos procesados")
            .register(registry);

        this.config = CalculationConfig.defaultConfig();
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public Mono<ServerResponse> compute(ServerRequest req) {
        var complexity = req.queryParam("complexity")
            .map(Complexity::fromString)
            .orElse(Complexity.defaultValue());

        return Mono.fromCallable(() -> executeComputation(complexity))
            .subscribeOn(Schedulers.fromExecutor(virtualThreadExecutor))
            .flatMap(result -> ServerResponse.ok().bodyValue(result))
            .onErrorResume(this::handleError);
    }

    private ComputationResult executeComputation(Complexity complexity) throws Exception {
        return computeTimer.recordCallable(() -> {
            performHeavyCalculation(complexity);
            eventCounter.increment();
            return ComputationResult.success();
        });
    }

    private void performHeavyCalculation(Complexity complexity) throws InterruptedException {
        var random = ThreadLocalRandom.current();
        var size = config.baseSize() + random.nextInt(complexity.value());
        var _ = generateComputationalData(size, random);
        simulateProcessingDelay(random);
    }

    private double[] generateComputationalData(int size, ThreadLocalRandom random) {
        var data = new double[size];

        for (int i = 0; i < size; i++) {
            data[i] = computeValue(random.nextDouble(), random.nextDouble());
        }

        return data;
    }

    private double computeValue(double val1, double val2) {
        return Math.sin(val1) * Math.log1p(val2);
    }

    private void simulateProcessingDelay(ThreadLocalRandom random) throws InterruptedException {
        var sleepMs = config.minSleepMs() + random.nextInt(config.maxAdditionalSleepMs());
        Thread.sleep(Duration.ofMillis(sleepMs));
    }

    private Mono<ServerResponse> handleError(Throwable error) {
        return switch (error) {
            case IllegalArgumentException e ->
                ServerResponse.badRequest()
                    .bodyValue("Invalid complexity parameter: " + e.getMessage());
            case InterruptedException _ -> {
                Thread.currentThread().interrupt();
                yield ServerResponse.status(503)
                    .bodyValue("Computation interrupted");
            }
            default ->
                ServerResponse.status(500)
                    .bodyValue("Computation error: " + error.getMessage());
        };
    }
}