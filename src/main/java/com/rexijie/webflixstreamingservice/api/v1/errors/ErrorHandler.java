package com.rexijie.webflixstreamingservice.api.v1.errors;

import com.rexijie.webflixstreamingservice.exceptions.VideoNotFoundException;
import com.rexijie.webflixstreamingservice.model.helpers.Error;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ErrorHandler {

    private static final Log logger = LogFactory.getLog(ErrorHandler.class);
    private final SimpleDateFormat dateFormat;

    public ErrorHandler() {
        logger.info("initializing error handler");
        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }

    public Mono<ServerResponse> handleError(Throwable throwable, ServerRequest request) {

        if (throwable instanceof VideoNotFoundException) {
            return handleNotFound(request, throwable);
        }

        if (throwable instanceof NumberFormatException) {
            return handleStringConversion(request, throwable);
        }

        return ServerResponse
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

    private Mono<ServerResponse> handleNotFound(ServerRequest request, Throwable throwable) {

        String errorDate = dateFormat.format(new Date());
        Error errorResponse = Error.builder()
                .status(404)
                .path(request.path())
                .err("Video not found. It most likely does not exist")
                .message(throwable.getMessage())
                .timestamp(errorDate)
                .build();

        logger.error("The video at [" + request.path() + "] could not be found");
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(errorResponse), Error.class);
    }

    private Mono<ServerResponse> handleStringConversion(ServerRequest request, Throwable throwable) {
        Error error = Error.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.uri().toString())
                .err("The value is not an integer")
                .message(throwable.getMessage())
                .timestamp(dateFormat.format(new Date()))
                .build();

        return ServerResponse.status(error.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(error), Error.class);
    }
}
