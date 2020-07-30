package com.rexijie.webflixstreamingservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.FileNotFoundException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VideoNotFoundException extends FileNotFoundException {

    public VideoNotFoundException() {
        super("video was not found");
    }
}
