package com.rexijie.webflixstreamingservice.repository.impl;

import com.rexijie.webflixstreamingservice.exceptions.VideoNotFoundException;
import com.rexijie.webflixstreamingservice.model.Video;
import com.rexijie.webflixstreamingservice.repository.VideoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryVideoRepository implements VideoRepository {

    private final Map<String, Video> videoCache = new ConcurrentHashMap<>();

    @Override
    public Mono<Video> getVideoByName(String name) {
        return Mono.create(videoMonoSink -> {
            Video video = videoCache.get(name);
            if (video != null)
                videoMonoSink.success(video);
            else
                videoMonoSink.error(new VideoNotFoundException());
        });
    }

    @Override
    public Flux<Video> getAllVideos() {
        synchronized (videoCache) {
            return Flux.fromIterable(videoCache.values());
        }
    }

    @Override
    public Mono<Video> addVideo(Video video) {
        synchronized (videoCache) {
            return Mono.fromCallable(() -> videoCache.put(video.getName(), video));
        }
    }
}
