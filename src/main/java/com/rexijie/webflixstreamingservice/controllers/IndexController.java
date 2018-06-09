package com.rexijie.webflixstreamingservice.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @Value("${video.location}")
    private String videoLocation;

    @GetMapping("/")
    public String index(Model model) {
        System.out.println(videoLocation);
        return "index";
    }
}
