package com.videoRecorder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VideoRecordApplication {

    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        SpringApplication.run(VideoRecordApplication.class, args);
    }
}
