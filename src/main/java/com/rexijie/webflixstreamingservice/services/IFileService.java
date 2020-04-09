package com.rexijie.webflixstreamingservice.services;

import reactor.core.publisher.Flux;

import java.nio.file.Path;

public interface IFileService {
    Flux<Path> getAllFiles();
}
