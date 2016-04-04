
/**
 * org.hermit.android.io: Android utilities for accessing peripherals.
 *
 * These classes provide some basic utilities for accessing the builtin
 * interface, at present.
 *
 * <br>Copyright 2009 Ian Cameron Smith
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation (see COPYING).
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package pl.llp.aircasting.sensor.builtin;

import pl.llp.aircasting.util.Constants;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * A class which reads builtin input from the mic in a background thread and
 * passes it to the caller when ready.
 * <p/>
 * <p>To use this class, your application must have permission RECORD_AUDIO.
 */
public class AudioReader {

    // ******************************************************************** //
    // Public Classes.
    // ******************************************************************** //

    /**
     * Listener for builtin reads.
     */
    public static abstract class Listener {
        /**
         * Audio read error code: the builtin reader failed to initialise.
         */
        public static final int ERR_INIT_FAILED = 1;

        /**
         * Audio read error code: an builtin read failed.
         */
        public static final int ERR_READ_FAILED = 2;

        /**
         * An builtin read has completed.
         *
         * @param buffer Buffer containing the data.
         */
        public abstract void onReadComplete(short[] buffer);

        /**
         * An error has occurred.  The reader has been terminated.
         *
         * @param error ERR_XXX code describing the error.
         */
        public abstract void onReadError(int error);
    }

    // ******************************************************************** //
    // Constructor.
    // ******************************************************************** //

    /**
     * Create an AudioReader instance.
     */
    public AudioReader() {
//        audioManager = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
    }

    // ******************************************************************** //
    // Run Control.
    // ******************************************************************** //

    /**
     * Start this reader.
     *
     * @param rate     The builtin sampling rate, in samples / sec.
     * @param block    Number of samples of input to read at a time.
     *                 This is different from the system builtin
     *                 buffer size.
     * @param listener Listener to be notified on each completed read.
     */
    public void startReader(int rate, int block, Listener listener) {
        Log.i(TAG, "Reader: Start Thread");
        synchronized (this) {
            // Calculate the required I/O buffer size.
            int audioBuf = AudioRecord.getMinBufferSize(rate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            Log.d(TAG, "Will use buffer size: " + audioBuf);

            // Set up the builtin input.
            audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    rate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioBuf);
            inputBlockSize = block;
            sleepTime = (long) (1000f / ((float) rate / (float) block));

            // We double inputBlockSize because of Android 5.0 bug,
            // AudioRecord.read(short[] audioData, int offsetInShorts, int sizeInShorts)
            // writes twice as much data to a buffer than it should.
            inputBuffer = new short[2][inputBlockSize * 2];
            inputBufferWhich = 0;
            inputBufferIndex = 0;
            inputListener = listener;
            running = true;
            readerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    readerRun();
                }
            }, "Audio Reader");
            readerThread.start();
        }
    }

    /**
     * Stop this reader.
     */
    public void stopReader() {
        Log.i(TAG, "Reader: Signal Stop");
        synchronized (this) {
            running = false;
        }
        try {
            if (readerThread != null)
                readerThread.join();
        } catch (InterruptedException e) {
            //Ignore - just stop
        }
        readerThread = null;

        // Kill the builtin input.
        synchronized (this) {
            if (audioInput != null) {
                audioInput.release();
                audioInput = null;
            }
        }

        Log.i(TAG, "Reader: Thread Stopped");
    }

    // ******************************************************************** //
    // Main Loop.
    // ******************************************************************** //

    /**
     * Main loop of the builtin reader.  This runs in our own thread.
     */
    private void readerRun() {
        short[] buffer;
        int index, readSize;

        int timeout = 5000;
        try {
            while (timeout > 0 && audioInput.getState() != AudioRecord.STATE_INITIALIZED) {
                Thread.sleep(50);
                timeout -= 50;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Audio reader thread interrupted", e);
        }

        Log.d(TAG, "Audio reader state: " + audioInput.getState());

        if (audioInput.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "Audio reader failed to initialize");
            readError(Listener.ERR_INIT_FAILED);
            running = false;
            return;
        }

        try {
            Log.i(TAG, "Reader: Start Recording");
            audioInput.startRecording();
            long stime = 0;

            while (running) {
                if (inputBufferIndex == 0) {
                    stime = System.currentTimeMillis();
                }

                if (!running)
                    break;

                readSize = inputBlockSize;
                int space = inputBlockSize - inputBufferIndex;
                if (readSize > space)
                    readSize = space;
                buffer = inputBuffer[inputBufferWhich];
                index = inputBufferIndex;

                synchronized (buffer) {
                    int nread = audioInput.read(buffer, index, readSize);

                    boolean done = false;
                    if (!running)
                        break;

                    if (nread < 0) {
                        Log.e(TAG, "Audio read failed: error " + nread);
                        readError(Listener.ERR_READ_FAILED);
                        running = false;
                        break;
                    }
                    int end = inputBufferIndex + nread;
                    if (end >= inputBlockSize) {
                        inputBufferWhich = (inputBufferWhich + 1) % 2;
                        inputBufferIndex = 0;
                        done = true;
                    } else
                        inputBufferIndex = end;

                    if (done) {
                        readDone(buffer);

                        // Because our block size is way smaller than the builtin
                        // buffer, we get blocks in bursts, which messes up
                        // the builtin analyzer.  We don't want to be forced to
                        // wait until the analysis is done, because if
                        // the analysis is slow, lag will build up.  Instead
                        // wait, but with a timeout which lets us keep the
                        // input serviced.
                        long etime = System.currentTimeMillis();
                        long sleep = sleepTime - (etime - stime);
                        if (sleep < 5)
                            sleep = 5;
                        try {
                            buffer.wait(sleep);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        } finally {
            Log.i(TAG, "Reader: Stop Recording");
            if (audioInput.getState() == AudioRecord.RECORDSTATE_RECORDING)
                audioInput.stop();
        }
    }

    /**
     * Notify the client that a read has completed.
     *
     * @param buffer Buffer containing the data.
     */
    private void readDone(short[] buffer) {
        inputListener.onReadComplete(buffer);
    }

    /**
     * Notify the client that an error has occurred.  The reader has been
     * terminated.
     *
     * @param code ERR_XXX code describing the error.
     */
    private void readError(int code) {
        inputListener.onReadError(code);
    }

    // ******************************************************************** //
    // Class Data.
    // ******************************************************************** //

    // Debugging tag.
    private static final String TAG = Constants.TAG + "/AudioReader";

    // ******************************************************************** //
    // Private Data.
    // ******************************************************************** //

    // Our builtin input device.
    private AudioRecord audioInput;

    // Our builtin input buffer, and the index of the next item to go in.
    private short[][] inputBuffer = null;
    private int inputBufferWhich = 0;
    private int inputBufferIndex = 0;

    // Size of the block to read each time.
    private int inputBlockSize = 0;

    // Time in ms to sleep between blocks, to meter the supply rate.
    private long sleepTime = 0;

    // Listener for input.
    private Listener inputListener = null;

    // Flag whether the thread should be running.
    private volatile boolean running = false;

    // The thread, if any, which is currently reading.  Null if not running.
    private Thread readerThread = null;
}

