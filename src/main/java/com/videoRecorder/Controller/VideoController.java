package com.videoRecorder.Controller;

import com.videoRecorder.Service.VideoService;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/")
public class VideoController {

    @Autowired
    private VideoService videoService;

    private AudioInputStream audioInputStream;
    private TargetDataLine targetDataLine;
    private File audioFile;

    @GetMapping("")
    public String getHome(Model model) {
        model.addAttribute("pageName", "Video Recorder");
        model.addAttribute("recording", videoService.getRecording());
        return "index";
    }


    @PostMapping("/updateRecording")
    public ResponseEntity<String> updateRecording(@RequestParam("recording") Boolean toggleRecordingStart) {
        if (toggleRecordingStart) {
            if (videoService.getRecording()) {
                throw new IllegalStateException("Already got recording in progress.");
            }
            String time = String.valueOf(System.currentTimeMillis());
            videoService.recordVideo(time);
            recordAudio(time);
            return new ResponseEntity<>("Recording started successfully", HttpStatus.OK);
        } else {
            try {
                if (!videoService.getRecording()) {
                    throw new IllegalStateException("No recording in progress.");
                }
                videoService.setRecording(false);
                // STOP VIDEO RECORDING
                videoService.stopVideoRecording();
                // STOP AUDIO RECORDING
                targetDataLine.stop();
                targetDataLine.close();
                audioInputStream.close();
                return new ResponseEntity<>("Recording stopped successfully", HttpStatus.OK);
            } catch (IllegalStateException | FrameGrabber.Exception | FrameRecorder.Exception e) {
                return new ResponseEntity<>("Recording stopped error " + e, HttpStatus.I_AM_A_TEAPOT);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void recordAudio(String time) {
        System.out.println("Started Video Recording");
        try {
            audioFile = new File("output_audio\\output" + time + ".wav");
            System.out.println(audioFile);
            AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            audioInputStream = new AudioInputStream(targetDataLine);
            Thread recordingThread = new Thread(this::recordAudioFrames);
            recordingThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void recordAudioFrames() {
        System.out.println("Started Audio Recording");
        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
        while (videoService.getRecording()) {
            try {
                AudioSystem.write(audioInputStream, fileType, audioFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
