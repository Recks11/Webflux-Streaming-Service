package com.rexijie.webflixstreamingservice.services.impl;

import com.rexijie.webflixstreamingservice.exceptions.VideoNotFoundException;
import com.rexijie.webflixstreamingservice.model.Video;
import com.rexijie.webflixstreamingservice.repository.VideoRepository;
import com.rexijie.webflixstreamingservice.services.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Long.min;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);
    private static final long BYTE_LENGTH = 1024;
    private static final long CHUNK_SIZE_VERY_LOW = BYTE_LENGTH * 256;
    private static final long CHUNK_SIZE_LOW = BYTE_LENGTH * 512;
    private static final long CHUNK_SIZE_MED = BYTE_LENGTH * 1024;
    private static final long CHUNK_SIZE_HIGH = BYTE_LENGTH * 2048;
    private static final long CHUNK_SIZE_VERY_HIGH = CHUNK_SIZE_HIGH * 2;

    private final VideoRepository videoRepository;

    public VideoServiceImpl(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Override
    public Mono<ResourceRegion> getRegion(String name, ServerRequest request) {
        HttpHeaders headers = request.headers().asHttpHeaders();
        HttpRange range = !headers.getRange().isEmpty() ? headers.getRange().get(0) : null;

        AtomicInteger sizeInt = new AtomicInteger();

        request.queryParam("partial").ifPresent(val ->
                sizeInt.set(Integer.parseInt(val)));

        long chunkSize = getChunkSize(sizeInt.get());

        Mono<UrlResource> resource = getResourceByName(name);
        return resource.map(urlResource -> {
            long contentLength = lengthOf(urlResource);
            if (range != null) {
                long start = range.getRangeStart(contentLength);
                long end = range.getRangeEnd(contentLength);
                long resourceLength = end - start + 1;
                long rangeLength = min(chunkSize, resourceLength);

                return new ResourceRegion(urlResource, start, rangeLength);
            } else {
                long rangeLength = min(chunkSize, contentLength);
                return new ResourceRegion(urlResource, 0, rangeLength);
            }
        }).doOnError(throwable -> {
            throw Exceptions.propagate(throwable);
        });
    }

    @Override
    public Mono<UrlResource> getResourceByName(String name) {
        return videoRepository.getVideoByName(name)
                .flatMap(this::createUriResourceFromVideo);
    }

    private Mono<UrlResource> createUriResourceFromVideo(Video videoObj) {
        return Mono.<UrlResource>create(monoSink -> {
            try {
                UrlResource video = new UrlResource(videoObj.getLocation().toUri());
                monoSink.success(video);
            } catch (MalformedURLException e) {
                monoSink.error(e);
            }
        }).doOnError(throwable -> {
            throw Exceptions.propagate(throwable);
        });
    }

    @Override
    public Flux<Video> getAllVideos() {
        return videoRepository.getAllVideos();
    }

    @Override
    public long lengthOf(UrlResource urlResource) {
        long fileLength;
        try {
            fileLength = urlResource.contentLength();
        } catch (IOException e) {
            logger.error("service could not get resource because the resource does not exist");
            throw Exceptions.propagate(new VideoNotFoundException());
        }
        return fileLength;
    }

    public long getChunkSize(int size) {
        long responseSize;
        switch (size) {
            case 1:
                responseSize = CHUNK_SIZE_VERY_LOW;
                break;
            case 2:
                responseSize = CHUNK_SIZE_LOW;
                break;
            case 4:
                responseSize = CHUNK_SIZE_HIGH;
                break;
            case 5:
                responseSize = CHUNK_SIZE_VERY_HIGH;
                break;
            default:
                responseSize = CHUNK_SIZE_MED;
        }

        return responseSize;
    }
}
