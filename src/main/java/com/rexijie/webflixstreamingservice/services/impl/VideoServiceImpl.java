package com.rexijie.webflixstreamingservice.services.impl;

import com.rexijie.webflixstreamingservice.services.VideoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;

import static java.lang.Long.min;

@Service
public class VideoServiceImpl implements VideoService {

    @Value("${video.location}")
    private String videoLocation;
    private static long CHUNK_SIZE_LOW = 50000L;
    private static long CHUNK_SIZE_MED = 100000L;
    private static long CHUNK_SIZE_HIGH = 150000L;

    @Override
    public ResourceRegion getRegion(UrlResource resource, HttpHeaders headers) {

        long contentLength = 0;
        try {
            contentLength = resource.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpRange range = headers.getRange().size() != 0 ? headers.getRange().get(0) : null;

            if (range != null) {
                long start = range.getRangeStart(contentLength);
                long end = range.getRangeEnd(contentLength);
                long resourceLength = end - start + 1;
                long rangeLength = min(CHUNK_SIZE_MED, resourceLength);

                return new ResourceRegion(resource, start, rangeLength);
            } else {
                long rangeLength = min(CHUNK_SIZE_MED, contentLength);
                return new ResourceRegion(resource, 0, rangeLength);
            }
    }

    @Override
    public UrlResource getResourceByName(String name) {
        UrlResource video = null;
        try {
            video = new UrlResource("file:"+videoLocation+ '/' + name);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return video;
    }
}
