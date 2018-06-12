package com.rexijie.webflixstreamingservice.api.v1.handlers;

import com.rexijie.webflixstreamingservice.exceptions.VideoNotFoundException;
import com.rexijie.webflixstreamingservice.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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

        UrlResource video = videoService.getResourceByName(name);
        ResourceRegion resourceRegion = videoService.getRegion(video, requestHeaders);

        return ServerResponse
                .status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(resourceRegion.getCount())
                .headers(headers -> headers.setCacheControl(CacheControl.noCache()))
                .body(Mono.just(resourceRegion), ResourceRegion.class)
                .flatMap(response -> {
                    if (response.headers().getContentLength() == 0) {
                        return Mono.error(new VideoNotFoundException());
                    }
                    return Mono.just(response);
                });
    }

    public Mono<ServerResponse> getFullLengthVideo(ServerRequest request) {
        String fileName = request.pathVariable("name");
        UrlResource video = videoService.getResourceByName(fileName);
        long fileLength = 0L;
        try {
            fileLength = video.contentLength();
        } catch (Exception e) {
            //@TODO (Handle Exception)
        }
        return ServerResponse
                .ok()
                .contentType(MediaTypeFactory.getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(fileLength)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(Mono.just(video), UrlResource.class)
                .flatMap(response -> {
                    if (response.headers().getContentLength() == 0)
                        return Mono.error(new VideoNotFoundException());
                    return Mono.just(response);
                });
    }
}
