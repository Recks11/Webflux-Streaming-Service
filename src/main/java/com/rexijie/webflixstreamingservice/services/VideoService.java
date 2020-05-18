package com.rexijie.webflixstreamingservice.services;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public interface VideoService {

    Mono<ResourceRegion> getRegion(Mono<UrlResource> resource, ServerRequest request);

    Mono<UrlResource> getResourceByName(String name);

    long lengthOf(UrlResource urlResource);
}
