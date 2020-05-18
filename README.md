# Webflux-Streaming-Service

Video streaming implemented with Spring WebFLux.

> By default all videos should be placed in the videos directory.

### How to run:

- Clone the project
- in your terminal `cd path/to/directory`
- run `./mvnw spring-boot:run` 



## API
Videos are served from the videos directory, You can change the videos directory to suit your needs in the `application.properties` file.

To see a list of all videos in the `Video` directory, navigate to `http://localhost:8080/videos` 

To play a video, navigate to `http://localhost:8080/videos/{name}`

API Routes:
 - `/video/{name}` - Serves the full length file using the whole range.
 - `/video/{name}?partial=3` - serves file in chunks using the range header chunk size is determined using the value 
 passed to the partial param. this can be an integer between 1 and 5
 
 > NOTE: This API can also serve documents and images, using the `/video/{name}` route, but the range will have to be set manually.
 it can also be adapted to serve large files in custom chunk sizes which can then be downloaded in parallel

## BUILDING THE PROJECT
To build the project run the maven package command

This will generate a jar in the targets folder. This file can be deployed on your server.

The default video location is relative to the jar file.
