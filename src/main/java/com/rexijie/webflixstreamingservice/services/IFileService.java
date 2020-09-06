package com.rexijie.webflixstreamingservice.services;

import com.rexijie.webflixstreamingservice.exceptions.BadResourceLocationException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.BaseStream;

public interface IFileService {
    Flux<Path> getAllFiles();

    /**
     * default method to create a flux from a stream of file paths
     *
     * @param path to traverse
     * @return Flux<Path>
     */
    default Flux<Path> fromPath(Path path) {
        return Flux.using(() -> Files.walk(path, FileVisitOption.FOLLOW_LINKS),
                Flux::fromStream,
                BaseStream::close)
                .doOnDiscard(BaseStream.class, BaseStream::close)
                .doOnError(err -> {
                    throw Exceptions.propagate(
                            new BadResourceLocationException("error retrieving data from video location", (IOException) err));
                })
                .filter(filePath -> !filePath.toFile().isDirectory())
                .filter(filePath -> !filePath.getFileName().toString().startsWith("."));
    }
}
