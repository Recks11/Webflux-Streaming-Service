package com.rexijie.webflixstreamingservice.api.v1.handlers;

import com.rexijie.webflixstreamingservice.model.Video;
import com.rexijie.webflixstreamingservice.services.IFileService;
import com.rexijie.webflixstreamingservice.services.VideoService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class VideoRouteHandler {

    private final VideoService videoService;
    private final IFileService fileService;

    @Autowired
    public VideoRouteHandler(VideoService videoService, IFileService fileService) {
        this.videoService = videoService;
        this.fileService = fileService;
    }


    public Mono<ServerResponse> listVideos(ServerRequest request) {

        Flux<Video> videos = videoService.getAllVideos();

        Flux<VideoDetails> videoDetailsFlux = videos
                .map(video -> {
                    VideoDetails videoDetails = new VideoDetails();
                    videoDetails.setName(video.getName());
                    videoDetails.setLink(request.uri().toString() + '/' + video.getName());
                    return videoDetails;
                })
                .doOnError(t -> {
                    throw Exceptions.propagate(t);
                });

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noCache())
                .location(request.uri())
                .body(videoDetailsFlux, VideoDetails.class);
    }

    public Mono<ServerResponse> getPartialContent(ServerRequest request) {
        String name = request.pathVariable("name");
        Mono<ResourceRegion> resourceRegionMono = videoService.getRegion(name, request);

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
    public Mono<ServerResponse> getFullContent(ServerRequest request) {

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

    @Data
    private static class VideoDetails {
        private String name;
        private String link;
    }
}