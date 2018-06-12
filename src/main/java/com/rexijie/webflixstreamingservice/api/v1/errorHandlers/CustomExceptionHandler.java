package com.rexijie.webflixstreamingservice.api.v1.errorHandlers;

import com.rexijie.webflixstreamingservice.model.helpers.Error;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@Order(-2)
public class CustomExceptionHandler extends WebFluxResponseStatusExceptionHandler {

    private Log logger = LogFactory.getLog(CustomExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = super.determineStatus(ex);
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");

        Error error = Error.builder()
                .timestamp(simpleDate.format(new Date()))
                .status(status.value())
                .build();

        if (status != null && exchange.getResponse().setStatusCode(status)) {
            if (status == HttpStatus.NOT_FOUND) {
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                error.setError("Route Not Found");
                logger.error(buildResponse(exchange, ex));
                logger.info(ex.getMessage());
            }
            else if (status == HttpStatus.BAD_REQUEST) {
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                error.setError("Bad Request");
                logger.warn(buildResponse(exchange, ex));
            }
            else if (status == HttpStatus.INTERNAL_SERVER_ERROR){
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                error.setError("The Server Encountered an error");
                logger.trace(buildResponse(exchange, ex));
            }
        }

        byte[] bytes = error.toString().getBytes();
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return exchange
                .getResponse()
                .writeWith(Mono.just(buffer));
    }

    private String buildResponse(ServerWebExchange exchange, Throwable ex) {
        return ("Using Custom Handler - Failed to handle request [" + exchange.getRequest().getMethod() + " " + exchange.getRequest().getURI() + "]: " + ex.getMessage());
    }
}
