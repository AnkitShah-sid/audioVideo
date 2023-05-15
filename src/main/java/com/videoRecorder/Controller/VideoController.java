package com.videoRecorder.Controller;

import com.videoRecorder.Service.AudioService;
import com.videoRecorder.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private AudioService audioService;


    @GetMapping("")
    public String getHome(Model model) {
        model.addAttribute("pageName", "Video Recorder");
        model.addAttribute("recording", videoService.recording);
        model.addAttribute("recording_audio", audioService.recording);
        return "index";
    }


    @PostMapping("/updateRecording")
    public ResponseEntity<String> updateRecording(@RequestParam("recording") Boolean toggleRecordingStart) {
        if (toggleRecordingStart) {
            if (videoService.recording) {
                throw new IllegalStateException("Already got recording in progress.");
            }
            if (audioService.recording) {
                throw new IllegalStateException("Already got recording in progress.");
            }
            String time = String.valueOf(System.currentTimeMillis());
            videoService.startRecordingVideo(time);
            audioService.startRecordingAudio(time);
            return new ResponseEntity<>("Recording started successfully", HttpStatus.OK);
        } else {
            try {
                if (!videoService.recording) {
                    throw new IllegalStateException("No recording in progress.");
                }
                // STOP VIDEO RECORDING
                videoService.stopVideoRecording();
                if (!audioService.recording) {
                    throw new IllegalStateException("No recording in progress.");
                }
                // STOP AUDIO RECORDING
                audioService.stopAudioRecording();
                return new ResponseEntity<>("Recording stopped successfully", HttpStatus.OK);
            } catch (IllegalStateException e) {
                return new ResponseEntity<>("Recording stopped error " + e, HttpStatus.I_AM_A_TEAPOT);
            }
        }
    }
}
