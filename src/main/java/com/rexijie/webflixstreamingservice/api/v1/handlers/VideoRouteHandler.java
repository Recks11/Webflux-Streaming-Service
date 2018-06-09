package com.rexijie.webflixstreamingservice.api.v1.handlers;

import com.rexijie.webflixstreamingservice.services.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

@Component
public class VideoRouteHandler {

    @Value("${video.location}")
    private String videoLocation;
    private VideoService videoService;

    @Autowired
    public VideoRouteHandler(VideoService videoService) {
        this.videoService = videoService;
    }

    public Mono<ServerResponse> getPartialVideoByName(ServerRequest request) {

        String name = request.pathVariable("name");
        UrlResource video = getVideos(name);
        ResourceRegion resourceRegion =
                videoService.getRegion(video, request.headers().asHttpHeaders());
        return ServerResponse
                .status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(resourceRegion.getCount())
                .headers(headers -> headers.setCacheControl(CacheControl.noCache()))
                .body(Mono.just(resourceRegion), ResourceRegion.class);
    }

    private UrlResource getVideos(String name) {
        UrlResource video = null;
        try {
            video = new UrlResource("file:"+videoLocation+ '/' + name);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return video;
    }
}
