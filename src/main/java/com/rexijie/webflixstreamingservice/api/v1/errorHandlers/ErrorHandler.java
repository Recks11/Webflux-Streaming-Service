package com.rexijie.webflixstreamingservice.api.v1.errorHandlers;

import com.rexijie.webflixstreamingservice.exceptions.VideoNotFoundException;
import com.rexijie.webflixstreamingservice.model.helpers.Error;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ErrorHandler {

    private static final Log logger = LogFactory.getLog(ErrorHandler.class);

    public ErrorHandler() {
        logger.info("Initialising error handler class");
    }

    public static Mono<ServerResponse> handleError(Throwable throwable, ServerRequest request) {

        if (throwable instanceof VideoNotFoundException) {
            return handleNotFound(request);
        }

        return ServerResponse
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Mono<ServerResponse> handleNotFound(ServerRequest request) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
        String errorDate = dateFormat.format(new Date());
        Error errorResponse = Error.builder()
                .status(404)
                .path(request.path())
                .error("Video not found")
                .timestamp(errorDate)
                .build();
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(errorResponse), Error.class);
    }
}
