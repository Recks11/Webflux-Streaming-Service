# Webflux-Streaming-Service

Video streaming implemented with Spring WebFLux.

> All videos should be placed in the videos directory.

How to run:
-
- Clone the project
- in your terminal `cd path/to/directory`
- run `./mvnw spring-boot:run` 

After the application finishes loading navigate to 
`http://localhost:8080/videos/{name of video in videos directory}`

For example the sample video is called Kevon-Carter.mp4 so the link to resource will be
`http://localhost:8080/videos/Kevon-Carter.mp4`
The location of the video can be changed to suit your needs in the `VideoServiceImpl.java` file

Links on the `/videos/{name of video in videos directory}` route are served partially, while
links on the `/videos/{name of video in videos directory}/full` route are served as a whole.
