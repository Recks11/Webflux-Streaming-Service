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
 - `/videos/{name}` - Serves the full length file using the whole range.
 - `/videos/{name}?partial=3` - serves file in chunks using the range header chunk size is determined using the value 
 passed to the partial param. this can be an integer between 1 and 5
 
 > NOTE: This API can also serve documents and images, using the `/video/{name}` route, but the range will have to be set manually.
 it can also be adapted to serve large files in custom chunk sizes which can then be downloaded in parallel


## DOCKER IMAGE
the image is available in docker hub at rexijie/webflix:tagname
```sh
$ docker pull rexijie/webflix:1.2.0
```

to run the image, you need to specify  the active profile to docker using `spring.profiles.active`,  then specify the video location within the container using the `service.video.location` environment  variable and then bind mount that location to your video directory


an example
```sh
$ docker container run \
--name webflix \
-e spring.profiles.active=docker \
-e service.video.location=/mnt/videos
--mount type=bind,source=/path/to/local/files,target=/mnt/videos \
-p8080:8080 \
rexijie/webflix:1.2.0
```

## BUILDING THE PROJECT
To build the project run the maven package command

This will generate a jar in the targets folder. This file can be deployed on your server.

The default video location is relative to the jar file.
