package com.rexijie.webflixstreamingservice.services;

import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.BaseStream;

public interface IFileService {
    Flux<Path> getAllFiles();

    /**
     * default method to create a flux from a stream of file paths
     * @param path to traverse
     * @return Flux<Path>
     */
    default Flux<Path> fromPath(Path path) {
        return Flux.using(() -> Files.list(path),
                Flux::fromStream,
                BaseStream::close);
    }
}
