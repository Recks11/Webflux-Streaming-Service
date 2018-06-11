package com.rexijie.webflixstreamingservice.api.v1;

import com.rexijie.webflixstreamingservice.api.v1.errorHandlers.ErrorHandler;
import com.rexijie.webflixstreamingservice.api.v1.handlers.VideoRouteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
public class VideoRoutes {

    private final VideoRouteHandler videoRouteHandler;

    @Autowired
    public VideoRoutes(VideoRouteHandler videoRouteHandler) {
        this.videoRouteHandler = videoRouteHandler;
    }

    @Bean
    RouterFunction<ServerResponse> videoEndPoint() {

        return route(GET("/video/{name}"), videoRouteHandler::getPartialVideoByName)
                .andRoute(GET("/video"), request -> ServerResponse.ok().body(Mono.just("Lol"), String.class))
                .filter((request, next) -> next.handle(request)
                        .onErrorResume(ErrorHandler::handleError));
    }
}
