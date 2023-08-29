package audio;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import signals.Signal;

// See https://docs.oracle.com/javase/tutorial/sound/capturing.html

public class Recorder {

    private TargetDataLine line = null;
    private boolean stopped = false;
    private float sampleRate = Signal.SAMPLE_RATE;

    public Recorder() {
    }

    public Recorder(int sampleRate) {
        this.setSampleRate(sampleRate);
    }

    AudioFormat getAudioFormat() {
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(getSampleRate(), sampleSizeInBits, channels, signed, bigEndian);
        return format;
    }

    LineListener lineListener = new LineListener() {

        @Override
        public void update(LineEvent event) {
            // TODO Auto-generated method stub
            if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                stopped = true;
            }
        }
    };

    public void getLine() {
        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error ...
            return;
        }
        // Obtain and open the line.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.addLineListener(lineListener);
            line.open(format);
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void read() {
        // Assume that the TargetDataLine, line, has already
        // been obtained and opened.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numBytesRead;
        byte[] data = new byte[line.getBufferSize() / 5];

        // Begin audio capture.
        line.start();

        // Here, stopped is a global boolean set by another thread.
        while (!stopped) {
            // Read the next chunk of data from the TargetDataLine.
            numBytesRead = line.read(data, 0, data.length);
            // Save this chunk of data.
            out.write(data, 0, numBytesRead);
        }
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }
}
