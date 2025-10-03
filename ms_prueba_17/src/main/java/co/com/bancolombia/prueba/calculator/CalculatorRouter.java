package co.com.bancolombia.prueba.calculator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class CalculatorRouter {

    @Bean
    RouterFunction<ServerResponse> routes(CalculatorHandler handler) {
        return route(GET("/compute"), handler::compute);
    }
}
