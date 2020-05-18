package com.rexijie.webflixstreamingservice.api.v1.errors;

import com.rexijie.webflixstreamingservice.model.helpers.Error;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.handler.WebFluxResponseStatusExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.channels.ClosedChannelException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@Order(-2)
public class CustomExceptionHandler extends WebFluxResponseStatusExceptionHandler {

    private static final Log logger = LogFactory.getLog(CustomExceptionHandler.class);
    final SimpleDateFormat simpleDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public CustomExceptionHandler() {
        logger.info("Initialising custom exception handler");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex.getCause() instanceof ClosedChannelException)
            return Mono.empty();

        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();

        HttpHeaders responseHeaders = response.getHeaders();

        HttpStatus status = super.determineStatus(ex);

        Error error = Error.builder()
                .timestamp(simpleDate.format(new Date()))
                .status(status != null ? status.value() : 0)
                .message(ex.getMessage())
                .path(exchange.getRequest().getURI().toString())
                .build();

        if (status != null) {
            if (status == HttpStatus.NOT_FOUND) {
                error.setErr("Route Not Found");
                logger.error(buildResponse(exchange, ex));
            } else if (status == HttpStatus.BAD_REQUEST) {
                error.setErr("Bad Request");
                logger.warn(buildResponse(exchange, ex));
            } else if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                error.setErr("The Server Encountered an error");
                logger.trace(buildResponse(exchange, ex));
            }
        } else {
            if (ex instanceof NumberFormatException) {
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                error.setErr("The value passed is not an integer");
                error.setStatus(HttpStatus.BAD_REQUEST.value());

                MultiValueMap<String, String> queryParams = request.getQueryParams();

                if (!queryParams.isEmpty() && queryParams.containsKey("partial")) {
                    error.setMessage("param " + queryParams.getFirst("partial") + " is not an integer. " +
                            "please provide a number between 1 and 5");
                }
            }
        }

        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        responseHeaders.setCacheControl(CacheControl.noCache());
        response.setRawStatusCode(error.getStatus());

        byte[] bytes = error.toString().getBytes();
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response
                .writeWith(Mono.just(buffer));
    }

    private String buildResponse(ServerWebExchange exchange, Throwable ex) {
        return ("Using Custom Handler - Failed to handle request [" + exchange.getRequest().getMethod() + " " + exchange.getRequest().getURI() + "]: " + ex.getMessage());
    }
}
