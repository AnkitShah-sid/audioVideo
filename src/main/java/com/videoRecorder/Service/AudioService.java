package com.videoRecorder.Service;

import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

@Service
public class AudioService {

    private AudioInputStream audioInputStream;
    private TargetDataLine targetDataLine;
    private File audioFile;
    public Boolean recording;


    public void startRecordingAudio(String time) {
        try {
            System.out.println("Started Audio Recording");
            audioFile = new File("output_audio\\output" + time + ".wav");
            AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            audioInputStream = new AudioInputStream(targetDataLine);
            Thread recordingThread = new Thread(this::recordAudioFrames);
            recordingThread.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void recordAudioFrames() {
        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
        while (recording) {
            try {
                AudioSystem.write(audioInputStream, fileType, audioFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopAudioRecording() {
        try {
            recording = false;
            targetDataLine.stop();
            targetDataLine.close();
            audioInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
