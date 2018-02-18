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
    public static GrInDriver instance;
    protected boolean enterPressed;
    public Peripherals peripherals;
    public Object securityCode;
    public GrInDriver(int w, int h) {
        super(w, h, false);
        peripherals = new Peripherals(this);
        securityCode = peripherals.gdResetPointers();
    }

    @Override
    public boolean stillRunning() {
        return true;
    }

    @Override
    public boolean flush() {
        enterPressed = enterPressed || (sendMaintenanceCode(1, null) != null);
        while (true) {
            if (MainActivity.sv != null) {
                try {
                    SurfaceHolder sh = MainActivity.sv.getHolder();
                    if (sh != null) {
                        Canvas c = sh.lockCanvas();
                        Rect r = sh.getSurfaceFrame();

                        int letterboxing2 = 0;
                        double realAspectRatio = w / (double) h;
                        int goodWidth = (int)(realAspectRatio * r.height());
                        // work out letterboxing from widths
                        int letterboxing = (r.width() - goodWidth) / 2;

                        final Rect displayArea = new Rect(letterboxing, letterboxing2, r.width() - letterboxing, r.height() - letterboxing2);
                        c.drawBitmap(bitmap, new Rect(0, 0, w, h), displayArea, globalPaint);

                        MainActivity.sv.setOnTouchListener(new OnTouchListener() {
                            @Override
                            public boolean onTouch(View arg0, MotionEvent arg1) {
                                int acto = arg1.getAction();
                                int act = (acto & MotionEvent.ACTION_MASK);
                                // ACTION_POINTER_INDEX_MASK
                                int ptrI = (acto >> 8) & 0xFF;
                                switch (act) {
                                    case MotionEvent.ACTION_DOWN:
                                        mapToArea(true, arg1, 0);
                                        break;
                                    case MotionEvent.ACTION_POINTER_DOWN:
                                        mapToArea(true, arg1, ptrI);
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        for (int i = 0; i < arg1.getPointerCount(); i++)
                                            mapToArea(true, arg1, i);
                                        break;
                                    case MotionEvent.ACTION_POINTER_UP:
                                        mapToArea(false, arg1, ptrI);
                                        break;
                                        // Sent "when the last pointer leaves the screen".
                                        // I hope you aren't lying.
                                    case MotionEvent.ACTION_UP:
                                        mapToArea(false, arg1, 0);
                                        securityCode = peripherals.gdResetPointers();
                                        break;
                                    default:
                                        return true;
                                }
                                return true;
                            }

                            private void mapToArea(boolean mode, MotionEvent arg1, int ptrI) {
                                float x = arg1.getX(ptrI);
                                float y = arg1.getY(ptrI);
                                x -= displayArea.left;
                                y -= displayArea.top;
                                x /= displayArea.width();
                                y /= displayArea.height();
                                x *= w;
                                y *= h;
                                peripherals.gdPushEvent(securityCode, mode, arg1.getPointerId(ptrI), (int) x, (int) y);
                            }
                        });
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
    }

    @Override
    public IPeripherals getPeripherals() {
        return peripherals;
    }

    protected String sendMaintenanceCode(int i, String text) {
        MainActivity ma = MainActivity.last;
        if (ma != null)
            return ma.tco.code(i, text);
        return null;
    }

    @Override
    public void shutdown() {
        // If the app really wants to quit, it'll call the right method.
        // Obviously it just wants a resolution change :)
    }

}
