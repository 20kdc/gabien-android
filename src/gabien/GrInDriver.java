/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import java.util.HashSet;
import java.util.LinkedList;


import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

public class GrInDriver extends OsbDriver implements IGrInDriver {
    public static GrInDriver instance;
    private boolean mouseDown, mouseJustDown, mouseJustUp, enterPressed;
    private int mouseX, mouseY;

    public GrInDriver(int w, int h) {
        super(w, h, false);
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
                                switch (act) {
                                    case MotionEvent.ACTION_DOWN:
                                    case MotionEvent.ACTION_POINTER_DOWN:
                                        mapToArea(arg1.getX(), arg1.getY());
                                        mouseJustDown = true;
                                        mouseDown = true;
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        mapToArea(arg1.getX(), arg1.getY());
                                        break;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_POINTER_UP:
                                        mouseDown = false;
                                        mouseJustUp = true;
                                        break;
                                    default:
                                        return true;
                                }
                                return true;
                            }

                            private void mapToArea(float x, float y) {
                                x -= displayArea.left;
                                y -= displayArea.top;
                                x /= displayArea.width();
                                y /= displayArea.height();
                                x *= w;
                                y *= h;
                                mouseX = (int) x;
                                mouseY = (int) y;
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
    public boolean isKeyDown(int KEYID) {
        return false;
    }

    @Override
    public boolean isKeyJustPressed(int KEYID) {
        if (KEYID == VK_ENTER) {
            boolean virtualEnterPress = enterPressed;
            enterPressed = false;
            return virtualEnterPress;
        }
        return false;
    }

    @Override
    public void clearKeys() {
        enterPressed = false;
        mouseJustDown = false;
        mouseJustUp = false;
    }

    @Override
    public int getMouseX() {
        return mouseX;
    }

    @Override
    public int getMouseY() {
        return mouseY;
    }

    @Override
    public HashSet<Integer> getMouseDown() {
        HashSet<Integer> b = new HashSet<Integer>();
        if (mouseDown)
            b.add(1);
        return b;
    }

    @Override
    public HashSet<Integer> getMouseJustDown() {
        HashSet<Integer> b = new HashSet<Integer>();
        if (mouseJustDown)
            b.add(1);
        mouseJustDown = false;
        return b;
    }

    @Override
    public HashSet<Integer> getMouseJustUp() {
        HashSet<Integer> b = new HashSet<Integer>();
        if (mouseJustUp)
            b.add(1);
        mouseJustUp = false;
        return b;
    }

    @Override
    public boolean getMousewheelJustDown() {
        return false;
    }

    @Override
    public boolean getMousewheelDir() {
        return false;
    }

    @Override
    public String maintain(int x, int y, int width, String text) {
        return sendMaintenanceCode(0, text);
    }

    private String sendMaintenanceCode(int i, String text) {
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
