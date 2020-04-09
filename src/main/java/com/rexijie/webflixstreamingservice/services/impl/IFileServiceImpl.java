package com.rexijie.webflixstreamingservice.services.impl;

import com.rexijie.webflixstreamingservice.services.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
@Slf4j
public class IFileServiceImpl implements IFileService {

    @Value("${video.location}")
    private String videoLocation;
    private Flux<Path> file;

    public IFileServiceImpl() {
        file = Flux.create(emitter -> {
            Stream<Path> list = Stream.<Path>builder().build();
            try {
                list = Files.list(Paths.get(videoLocation));
                list.forEach(emitter::next);
            } catch (IOException e) {
                emitter.error(e);
            } finally {
                list.close();
                emitter.complete();
            }
        });
        log.info("file service created");
    }

    @Override
    public Flux<Path> getAllFiles() {
        return file;
    }
}
