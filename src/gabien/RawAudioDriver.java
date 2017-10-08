/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class RawAudioDriver implements IRawAudioDriver {
    public boolean keepAlive = true;

    public RawAudioDriver() {
        Thread t = new Thread() {
            @Override
            public void run() {
                AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 22050, AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT, 1024,
                        AudioTrack.MODE_STREAM);
                while (keepAlive) {
                    at.write(ras.pullData(512), 0, 1024);
                    if (at.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
                        at.play();
                }
            }
        };
        t.start();
    }

    private IRawAudioSource ras = new IRawAudioSource() {
        @Override
        public short[] pullData(int samples) {
            return new short[samples * 2];
        }
    };

    @Override
    public IRawAudioSource setRawAudioSource(IRawAudioSource src) {
        IRawAudioSource last = ras;
        ras = src;
        return last;
    }
}
