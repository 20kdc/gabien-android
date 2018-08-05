/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

import gabien.ui.IConsumer;
import gabien.ui.WindowCreatingUIElementConsumer;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of the runtime.
 */
public final class GaBIenImpl implements IGaBIEn {
    //In case you get confused.
    private long startup = System.currentTimeMillis();
    public HashMap<String, IImage> ht = new HashMap<String, IImage>();
    private double lastdt = getTime();
    private RawAudioDriver rad;

    public static void main() throws Exception {
        FontManager.fontsReady = true;
    	GaBIEn.internal = new GaBIenImpl();
    	Class.forName("gabienapp.Application").getDeclaredMethod("gabienmain").invoke(null);
    }
    public double getTime() {
        return (System.currentTimeMillis() - startup) / 1000.0d;
    }

    public double timeDelta(boolean reset) {
        double dt = getTime() - lastdt;
        if (reset)
            lastdt = getTime();
        return dt;
    }

    @Override
    public IImage createImage(int[] colours, int width, int height) {
        return new OsbDriver(width, height, colours);
    }

    @Override
    public void hintFlushAllTheCaches() {
        ht.clear();
    }

    @Override
    public int measureText(int i, String text) {
        // *hmm*... something seems off here.
        // figure out what units this uses, do the usual awful hax
        Paint p = new Paint();
        p.setTextSize(i);
        return (int) p.measureText(text + " "); // about the " " : it gets it wrong somewhat, by about this amount
    }

    @Override
    public String[] getFontOverrides() {
        return new String[] {
                "Nautilus"
        };
    }

    @Override
    public String[] listEntries(String s) {
        return getFileObj(s).list();
    }

    @Override
    public void makeDirectories(String s) {
        getFileObj(s).mkdirs();
    }

    @Override
    public boolean fileOrDirExists(String s) {
        return getFileObj(s).exists();
    }

    @Override
    public boolean dirExists(String s) {
        return getFileObj(s).isDirectory();
    }

    @Override
    public boolean tryStartTextEditor(String fpath) {
        // Maybe autodetect OI Notepad for this.
        return false;
    }

    public InputStream getResource(String resource) {
        AssetManager am = MainActivity.last.getAssets();
        try {
            return am.open(resource);
        } catch (Exception e) {
            return null;
        }
//        return ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
    }

    public File getFileObj(String fd) {
        return new File("/sdcard/" + fd);
    }

    public InputStream getFile(String FDialog) {
    	try {
    		return new FileInputStream(getFileObj(FDialog));
    	} catch (Exception e) {
    		Logger.getLogger("gabien-android").log(Level.SEVERE, "Error opening "+FDialog, e);
    		return null;
    	}
    }

    public void rmFile(String f) {
        getFileObj(f).delete();
    }

    @Override
    public void setBrowserDirectory(String b) {
        // Maybe one day.
    }

    @Override
    public void startFileBrowser(String text, boolean saving, String exts, IConsumer<String> result) {
        // Need to setup an environment for a file browser.
        final WindowCreatingUIElementConsumer wc = new WindowCreatingUIElementConsumer();
        // if this crashes, you're pretty doomed
        int heightPix = MainActivity.last.mySurface.getHeight();
        UIFileBrowser fb = new UIFileBrowser(result, text, "<-", saving ? GaBIEn.wordSave : GaBIEn.wordLoad, GaBIEn.sysCoreFontSize, GaBIEn.sysCoreFontSize);
        wc.accept(fb);
        final Runnable tick = new Runnable() {
            double lastTime = GaBIEn.getTime();
            @Override
            public void run() {
                double newTime = GaBIEn.getTime();
                double dT = newTime - lastTime;
                lastTime = newTime;
                wc.runTick(dT);
                if (wc.runningWindows().size() > 0)
                    GaBIEn.pushLaterCallback(this);
            }
        };
        GaBIEn.pushCallback(tick);
    }

    public IGrInDriver makeGrIn(String name, int w, int h, WindowSpecs ws) {
        GrInDriver gd = new GrInDriver(w, h);
        MainActivity.pushOwner(gd);
        return gd;
    }

    @Override
    public IGrDriver makeOffscreenBuffer(int w, int h, boolean alpha) {
        return new OsbDriver(w, h, alpha);
    }

    @Override
    public WindowSpecs defaultWindowSpecs(String name, int w, int h) {
        WindowSpecs ws = new WindowSpecs();
        ws.scale = 1;
        return ws;
    }

    private IImage getImageInternal(String a, boolean res, String id, boolean ck, int i) {
        if (ht.containsKey(id))
            return ht.get(id);
        IImage r = GaBIEn.getErrorImage();
        try {
            Bitmap b = BitmapFactory.decodeStream(res ? getResource(a) : getFile(a));
            int w = b.getWidth();
            int h = b.getHeight();
            int[] data = new int[w * h];
            b.getPixels(data, 0, w, 0, 0, w, h);
            if (ck)
                for (int j = 0; j < data.length; j++)
                    if ((data[j] & 0xFFFFFF) == i) {
                        data[j] = 0;
                    } else {
                        data[j] |= 0xFF000000;
                    }
            r = new OsbDriver(w, h, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ht.put(id, r);
        return r;
    }


    @Override
    public IImage getImage(String a, boolean res) {
        return getImageInternal(a, res, (res ? 'R' : 'F') + "~" + a, false, -1);
    }

    @Override
    public IImage getImageCK(String a, boolean res, int r, int g, int b) {
        r &= 0xFF;
        g &= 0xFF;
        b &= 0xFF;
        return getImageInternal(a, res, (res ? 'R' : 'F') + "X" + r + " " + g + " " + b + "~" + a, true, (r << 16) | (g << 8) | b);
    }

    @Override
    public OutputStream getOutFile(String resource) {
        try {
            return new FileOutputStream(getFileObj(resource));
        } catch (Exception e) {
            Logger.getLogger("gabien-android").log(Level.SEVERE, "Error opening FO "+resource, e);
            return null;
        }
    }

    public boolean singleWindowApp()
    {
    	return true;
    }

    @Override
    public IRawAudioDriver getRawAudio() {
        if (rad == null)
            rad = new RawAudioDriver();
        return rad;
    }

    @Override
    public void hintShutdownRawAudio() {
        if (rad != null) {
            rad.keepAlive = false;
            rad = null;
        }
    }

    public void ensureQuit() {
        System.exit(0);
    }

}
