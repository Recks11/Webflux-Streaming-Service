package com.rexijie.webflixstreamingservice.services;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public interface VideoService {

    Mono<ResourceRegion> getRegion(Mono<UrlResource> resource, HttpHeaders headers);

    Mono<UrlResource> getResourceByName(String name);

    Mono<Long> getSafeContentLengthForResource(Mono<UrlResource> urlResource);
}
