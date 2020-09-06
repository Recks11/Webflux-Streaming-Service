package com.rexijie.webflixstreamingservice.repository;

import com.rexijie.webflixstreamingservice.model.Video;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VideoRepository {
    Mono<Video> getVideoByName(String name);
    Flux<Video> getAllVideos();
    Mono<Video> addVideo(Video video);
}
