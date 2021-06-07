package com.rexijie.webflixstreamingservice.config;

import com.rexijie.webflixstreamingservice.model.Video;
import com.rexijie.webflixstreamingservice.repository.VideoRepository;
import com.rexijie.webflixstreamingservice.services.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Bootstrap implements CommandLineRunner {
    private final VideoRepository videoRepository;
    private final IFileService fileService;

    public Bootstrap(VideoRepository videoRepository, IFileService fileService) {
        this.videoRepository = videoRepository;
        this.fileService = fileService;
    }

    @Override
    public void run(String... args) {
        fileService.getAllFiles()
                .doOnNext(path -> log.debug("found file in path: " + path.toUri() + " FileName: " + path.getFileName()))
                .flatMap(path -> {
                    Video video = new Video();
                    video.setName(path.getFileName().toString());
                    video.setLocation(path);
                    return videoRepository.addVideo(video);
                })
                .subscribe();

        videoRepository.getAllVideos()
                .doOnNext(video -> log.info("Registered video: " + video.getName()))
                .subscribe();
    }
}
