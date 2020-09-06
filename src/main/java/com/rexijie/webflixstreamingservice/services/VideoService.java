package com.rexijie.webflixstreamingservice.services;

import com.rexijie.webflixstreamingservice.model.Video;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VideoService {

    Mono<ResourceRegion> getRegion(String name, ServerRequest request);

    Mono<UrlResource> getResourceByName(String name);

    Flux<Video> getAllVideos();

    long lengthOf(UrlResource urlResource);
}
