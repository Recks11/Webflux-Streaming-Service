package com.rexijie.webflixstreamingservice.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class IndexController {

    @Value("${video.location}")
    private String videoLocation;

    @GetMapping("/")
    public String index(Model model) throws Exception{
        List<String> videos = Files.list(Paths.get(videoLocation))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());

        System.out.println(videos.toString());
        return "index";
    }
}
