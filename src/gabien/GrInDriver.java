/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;


import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

public class GrInDriver extends OsbDriver implements IGrInDriver {
    protected boolean enterPressed;
    public Peripherals peripherals;
    public boolean wantsShutdown = false;
    public Rect displayArea = new Rect(0, 0, 1, 1);

    public GrInDriver(int w, int h) {
        super(w, h, false);
        peripherals = new Peripherals(this);
    }

    @Override
    public boolean stillRunning() {
        return !wantsShutdown;
    }

    @Override
    public boolean flush() {
        enterPressed = enterPressed || (sendMaintenanceCode(1, null) != null);
        while (MainActivity.getCurrentOwner() == this) {
            MainActivity last = MainActivity.last;
            if (last != null) {
                try {
                    SurfaceHolder sh = last.mySurface.getHolder();
                    if (sh != null) {
                        Canvas c = sh.lockCanvas();
                        Rect r = sh.getSurfaceFrame();

                        int letterboxing2 = 0;
                        double realAspectRatio = w / (double) h;
                        int goodWidth = (int)(realAspectRatio * r.height());
                        // work out letterboxing from widths
                        int letterboxing = (r.width() - goodWidth) / 2;

                        displayArea = new Rect(letterboxing, letterboxing2, r.width() - letterboxing, r.height() - letterboxing2);
                        c.drawBitmap(bitmap, new Rect(0, 0, w, h), displayArea, globalPaint);

                        sh.unlockCanvasAndPost(c);
                        // Has side-effects if a textbox is up
                        if (sendMaintenanceCode(2, null) == null)
                            if ((r.width() != w) || (r.height() != h)) {
                                resize(r.width(), r.height());
                                return true;
                            }
                        return false;
                    }
                } catch (Exception e) {

                }
            }
            try {
                Thread.sleep(100);
                // Keyboard holding things up?
                sendMaintenanceCode(3, null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public IPeripherals getPeripherals() {
        return peripherals;
    }

    protected String sendMaintenanceCode(int i, String text) {
        MainActivity ma = MainActivity.last;
        if (ma != null)
            return ma.myTCO.code(i, text);
        return null;
    }

    @Override
    public void shutdown() {
        // Bye-bye, GrInDriver.
        wantsShutdown = true;
    }

}
