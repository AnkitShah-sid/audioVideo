package com.videoRecorder.Service;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class VideoService {

    public boolean recording;
    private FrameGrabber grabber;
    private FrameRecorder recorder;

    public void startRecordingVideo(String time) {
        try {
            System.out.println("Started video Recording");
            if (recording) {
                throw new IllegalStateException("Recording is already in progress.");
            }
            String outputFolderPath = "output_video";
            String outputFileName = "output" + time + ".mp4";
            File outputFolder = new File(outputFolderPath); // Create the output folder if it doesn't exist
            if (!outputFolder.exists()) {
                boolean created = outputFolder.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create the output folder.");
                }
            }
            String outputFilePath = outputFolderPath + File.separator + outputFileName;
            grabber = FrameGrabber.createDefault(0);
            grabber.start();
            recorder = FrameRecorder.createDefault(outputFilePath, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.start();
            recording = true;
            Thread recordingThread = new Thread(this::recordFrames);
            recordingThread.start();
        } catch (IllegalStateException | FrameGrabber.Exception | FrameRecorder.Exception e) {
            throw new RuntimeException("Recording start error " + e);
        }
    }

    private void recordFrames() {
        try {
            while (recording) {
                Frame frame = grabber.grab();
                recorder.record(frame);
                Thread.sleep(33);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopVideoRecording() {

        System.out.println("Stop video Recording");
        recording = false;
        try {
            grabber.stop();
            grabber.release();
            recorder.stop();
            recorder.release();
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        }
    }
}
