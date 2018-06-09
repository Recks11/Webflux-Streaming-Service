package com.rexijie.webflixstreamingservice.services;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;

public interface VideoService {

    ResourceRegion getRegion(UrlResource resource, HttpHeaders headers);
}
