package io.dhoondlay.shield.config;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * Reactive WebFilter to generate or propagate a unique Transaction/Correlation ID.
 * 
 * Note: Since ThreadLocal (MDC) doesn't propagate naturally in Reactor, 
 * we store it in the Reactor Context and apply/clear it during signal processing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalId = correlationId;
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, finalId);

        return chain.filter(exchange)
                .contextWrite(Context.of(MDC_KEY, finalId))
                .doOnEach(signal -> {
                    // This is a simplified way to apply IDs to MDC during processing
                    // For production usage, a more robust Logback Hook is recommended
                    MDC.put(MDC_KEY, finalId);
                });
    }
}
