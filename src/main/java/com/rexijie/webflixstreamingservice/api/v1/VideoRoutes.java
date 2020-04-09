package com.rexijie.webflixstreamingservice.api.v1;

import com.rexijie.webflixstreamingservice.api.v1.errorHandlers.ErrorHandler;
import com.rexijie.webflixstreamingservice.api.v1.handlers.VideoRouteHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class VideoRoutes {

    @Bean
    RouterFunction<ServerResponse> videoEndPoint(VideoRouteHandler videoRouteHandler) {

        return route(GET("/videos"), videoRouteHandler::listVideos)
                .andRoute(GET("/videos/{name}"), videoRouteHandler::getVideoRegion)
                .andRoute(GET("/videos/{name}/full"), videoRouteHandler::getFullLengthVideo)
                .filter((request, next) -> next.handle(request)
                        .onErrorResume(throwable -> ErrorHandler.handleError(throwable, request)));
    }
}
