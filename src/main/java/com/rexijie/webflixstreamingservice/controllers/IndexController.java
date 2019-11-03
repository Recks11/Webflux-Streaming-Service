package com.rexijie.webflixstreamingservice.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    @Value("${video.location}")
    private String videoLocation;

    @GetMapping("/")
    public Mono<String> index() throws Exception{
        return Mono.just("index");
    }
}
