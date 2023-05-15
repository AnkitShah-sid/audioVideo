package com.videoRecorder.Service;

public class IOService {
    boolean recording = false;

    public void setRecording(Boolean recording) {
        this.recording = recording;
    }
    public Boolean getRecording() {
        return recording;
    }
}
