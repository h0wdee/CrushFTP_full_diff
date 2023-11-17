/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

public class Sounds {
    public void loadSound(Object object) {
        AudioInputStream currentSound = null;
        if (object instanceof File_S) {
            try {
                AudioInputStream stream = currentSound = AudioSystem.getAudioInputStream((File_S)object);
                AudioFormat format = stream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, stream.getFormat(), (int)stream.getFrameLength() * format.getFrameSize());
                class PlayTheClip
                implements Runnable {
                    Clip clip = null;
                    AudioInputStream stream = null;

                    public PlayTheClip(Clip clip, AudioInputStream stream) {
                        this.clip = clip;
                        this.stream = stream;
                    }

                    @Override
                    public void run() {
                        try {
                            this.clip.addLineListener(null);
                            this.clip.open(this.stream);
                            FloatControl panControl = (FloatControl)this.clip.getControl(FloatControl.Type.PAN);
                            panControl.setValue(0.0025f);
                            this.clip.start();
                            while (this.clip.isActive()) {
                                try {
                                    Thread.sleep(99L);
                                }
                                catch (Exception e) {
                                    break;
                                }
                            }
                            this.clip.stop();
                            this.clip.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                Worker.startWorker(new PlayTheClip((Clip)AudioSystem.getLine(info), stream));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        currentSound = null;
    }
}

