# Webflux-Streaming-Service

Video streaming implemented with Spring WebFLux.

> All videos should be placed in the videos directory.

### How to run:

- Clone the project
- in your terminal `cd path/to/directory`
- run `./mvnw spring-boot:run` 



## API
Navigate to `http://localhost:8080/videos/{name of video in videos directory}`

You can change the to suit your needs in the `application.properties` file. This service currently uses relative path.

Links on the `/videos/{name of video in videos directory}` route are served partially <br/>
links on the `/videos/{name of video in videos directory}/full` route are served as a whole.

### BUILDING THE PROJECT
To build the project run the maven package command  <br/>
This will generate a jar in the targets folder. This file can be deployed on your server. <br/>
The default video location is relative to the jar file.