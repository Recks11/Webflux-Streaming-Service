package com.rexijie.webflixstreamingservice.api.v1.handlers;

import com.rexijie.webflixstreamingservice.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@Component
public class VideoRouteHandler {

    private final VideoService videoService;

    @Autowired
    public VideoRouteHandler(VideoService videoService) {
        this.videoService = videoService;
    }

    public Mono<ServerResponse> returnPath(ServerRequest request) {
        return ServerResponse.ok().body(Mono.just(request.path()), String.class);
    }

    public Mono<ServerResponse> getVideoRegion(ServerRequest request) {
        String name = request.pathVariable("name");
        HttpHeaders requestHeaders = request.headers().asHttpHeaders();
        Mono<UrlResource> videoResourceMono = videoService.getResourceByName(name);
        Mono<ResourceRegion> resourceRegionMono = videoService.getRegion(videoResourceMono, requestHeaders);

        return resourceRegionMono
                .flatMap(resourceRegion -> ServerResponse
                        .status(HttpStatus.PARTIAL_CONTENT)
                        .contentLength(resourceRegion.getCount())
                        .headers(headers -> headers.setCacheControl(CacheControl.noCache()))
                        .body(resourceRegionMono, ResourceRegion.class))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * This function gets a file from the file system and returns it as a whole
     * videoResource.contentLength() is a blocking call, therefore it is wrapped in a Mono.
     * it returns a FileNotFound exception which is wrapped and propagated down the stream
     */
    public Mono<ServerResponse> getFullLengthVideo(ServerRequest request) {

        String fileName = request.pathVariable("name");

        Mono<UrlResource> videoResourceMono = videoService.getResourceByName(fileName);


        return videoResourceMono
                .flatMap(urlResource -> {
                    long contentLength = videoService.lengthOf(urlResource);
                    return ServerResponse
                            .ok()
                            .contentLength(contentLength)
                            .headers(httpHeaders -> httpHeaders.setCacheControl(CacheControl.noCache()))
                            .body(videoResourceMono, UrlResource.class);
                });

    }
}