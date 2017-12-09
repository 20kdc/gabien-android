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

import gabien.ui.UILabel;
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
        UILabel.fontOverride = "Nautilus";
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

    public IGrInDriver makeGrIn(String name, int w, int h, WindowSpecs ws) {
        if (GrInDriver.instance == null)
        	GrInDriver.instance = new GrInDriver(w, h);
        return GrInDriver.instance;
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

    private IImage getImageInternal(String a, String id, int i) {
        if (ht.containsKey(id))
            return ht.get(id);
        IImage r = GaBIEn.getErrorImage();
        try {
            Bitmap b = BitmapFactory.decodeStream(GaBIEn.getFile(a));
            int w = b.getWidth();
            int h = b.getHeight();
            int[] data = new int[w * h];
            b.getPixels(data, 0, w, 0, 0, w, h);
            for (int j = 0; j < data.length; j++)
                if ((data[j] & 0xFFFFFF) == i)
                    data[j] = 0;
            r = new OsbDriver(w, h, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ht.put(id, r);
        return r;
    }


    @Override
    public IImage getImage(String a) {
        return getImageInternal(a, "~" + a, -1);
    }

    @Override
    public IImage getImageCK(String a, int r, int g, int b) {
        r &= 0xFF;
        g &= 0xFF;
        b &= 0xFF;
        return getImageInternal(a, "X" + r + " " + g + " " + b + "~" + a, (r << 16) | (g << 8) | b);
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
