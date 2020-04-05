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

    public Mono<ServerResponse> getPartialVideoByName(ServerRequest request) {
        String name = request.pathVariable("name");
        HttpHeaders requestHeaders = request.headers().asHttpHeaders();
        Mono<UrlResource> videoResourceMono = videoService.getResourceByName(name);
        Mono<ResourceRegion> resourceRegionMono = videoService.getRegion(videoResourceMono, requestHeaders);

        return resourceRegionMono.zipWith(videoResourceMono, (resourceRegion, video) -> ServerResponse
                .status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(resourceRegion.getCount())
                .headers(headers -> headers.setCacheControl(CacheControl.noCache()))
                .body(resourceRegionMono, ResourceRegion.class))
                .flatMap(serverResponseMono -> serverResponseMono)
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });

//        return ServerResponse
//                .status(HttpStatus.PARTIAL_CONTENT)
//                .contentType(MediaTypeFactory.getMediaType(video)
//                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
//                .contentLength(resourceRegion.getCount())
//                .headers(headers -> headers.setCacheControl(CacheControl.noCache()))
//                .body(Mono.just(resourceRegion), ResourceRegion.class)
//                .flatMap(response -> {
//                    if (response.headers().getContentLength() == 0) {
//                        return Mono.error(new VideoNotFoundException());
//                    }
//                    return Mono.just(response);
//                });
    }

    /**
     * This function gets a file from the file system and returns it as a whole
     * videoResource.contentLength() is a blocking call, therefore it is wrapped in a Mono.
     * it returns a FileNotFound exception which is wrapped and propagated down the stream
     */
    public Mono<ServerResponse> getFullLengthVideo(ServerRequest request) {

        String fileName = request.pathVariable("name");

        Mono<UrlResource> videoResourceMono = videoService.getResourceByName(fileName);
        Mono<Long> videoContentLength = videoService.getSafeContentLengthForResource(videoResourceMono);

        return videoContentLength
                .zipWith(videoResourceMono,
                        (fileLength, urlResource) -> ServerResponse
                                .ok()
                                .contentType(MediaTypeFactory.getMediaType(urlResource)
                                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                                .contentLength(fileLength)
                                .headers(httpHeaders -> httpHeaders.setCacheControl(CacheControl.noCache()))
                                .body(videoResourceMono, UrlResource.class))
                .flatMap(serverResponseMono -> serverResponseMono);

    }
}