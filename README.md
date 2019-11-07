# Webflux-Streaming-Service

Video streaming implemented with Spring WebFLux.

> All videos should be placed in the videos directory.

How to run:
-
- Clone the project
- in your terminal `cd path/to/directory`
- run `./mvnw spring-boot:run` 



## API
Navigate to `http://localhost:8080/videos/{name of video in videos directory}`

You can change the to suit your needs in the `application.properties` file

Links on the `/videos/{name of video in videos directory}` route are served partially <br/>
links on the `/videos/{name of video in videos directory}/full` route are served as a whole.
