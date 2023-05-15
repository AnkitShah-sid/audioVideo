package com.videoRecorder.Service;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.springframework.stereotype.Service;

@Service
public class VideoService extends IOService {

    private FrameGrabber grabber;
    private FrameRecorder recorder;

    public void recordVideo(String time) {
        try {
            if (recording) {
                throw new IllegalStateException("Recording is already in progress.");
            }
            grabber = FrameGrabber.createDefault(0);
            grabber.start();
            recorder = FrameRecorder.createDefault("output_video\\output" + time + ".mp4", grabber.getImageWidth(), grabber.getImageHeight());
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

    public void stopVideoRecording() throws FrameGrabber.Exception, FrameRecorder.Exception {
        grabber.stop();
        grabber.release();
        recorder.stop();
        recorder.release();
    }
}
