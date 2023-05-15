package com.videoRecorder.Controller;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/")
public class VideoController {


    private FrameGrabber grabber;
    private FrameRecorder recorder;
    private boolean recording;

    private AudioInputStream audioInputStream;
    private TargetDataLine targetDataLine ;
    private File audioFile;

    @GetMapping("")
    public String getHome(Model model) {
        model.addAttribute("pageName", "Video Recorder");
        model.addAttribute("recording", recording);
        return "index";
    }


    @PostMapping("/updateRecording")
    public ResponseEntity<String> updateRecording(@RequestParam("recording") Boolean toggleRecordingStart) {
        if (toggleRecordingStart) {
            if (recording) {
                throw new IllegalStateException("Already got recording in progress.");
            }
            String time = String.valueOf(System.currentTimeMillis());
            recordVideo(time);
            recordAudio(time);
            return new ResponseEntity<>("Recording started successfully", HttpStatus.OK);
        } else {
            try {
                if (!recording) {
                    throw new IllegalStateException("No recording in progress.");
                }
                recording = false;
                // STOP VIDEO RECORDING
                grabber.stop();
                grabber.release();
                recorder.stop();
                recorder.release();
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

    private void recordVideo(String time) {
        try {
            if (recording) {
                throw new IllegalStateException("Recording is already in progress.");
            }
            grabber = FrameGrabber.createDefault(0);
            grabber.start();
            recorder = FrameRecorder.createDefault("output_video" + time + ".mp4", grabber.getImageWidth(), grabber.getImageHeight());
            recorder.start();
            recording = true;
            Thread recordingThread = new Thread(this::recordFrames);
            recordingThread.start();
        } catch (IllegalStateException | FrameGrabber.Exception | FrameRecorder.Exception e) {
            throw new RuntimeException("Recording start error " + e);
        }
    }

    private void recordAudio(String time) {
        System.out.println("Started Video Recording");
        try {
            audioFile = new File("output_audio/output" + time + ".wav");
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
        while (recording) {
            try {
                AudioSystem.write(audioInputStream, fileType, audioFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void recordFrames() {
        try {
            while (recording) {
                Frame frame = grabber.grab();
                BufferedImage bufferedImage = new Java2DFrameConverter().convert(frame);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] frameBytes = baos.toByteArray();
                recorder.record(frame);
                Thread.sleep(33);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 }
