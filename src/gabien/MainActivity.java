/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends Activity implements Runnable {
	public static Thread gameThread = null;
	public static MainActivity last; // W:UITHREAD R:GTHREAD

    private static LinkedList<GrInDriver> controlStack = new LinkedList<GrInDriver>();
    private static ReentrantLock controlStackLock = new ReentrantLock();

	public TextboxControlObject myTCO;
    public SurfaceView mySurface;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SurfaceView surfaceview = new SurfaceView(this);
        surfaceview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                controlStackLock.lock();
                if (controlStack.size() == 0) {
                    controlStackLock.unlock();
                    return true;
                }
                GrInDriver currentOwner = controlStack.getLast();
                int acto = arg1.getAction();
                int act = (acto & MotionEvent.ACTION_MASK);
                // ACTION_POINTER_INDEX_MASK
                int ptrI = (acto >> 8) & 0xFF;
                switch (act) {
                    case MotionEvent.ACTION_DOWN:
                        mapToArea(currentOwner, true, arg1, 0);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mapToArea(currentOwner, true, arg1, ptrI);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        for (int i = 0; i < arg1.getPointerCount(); i++)
                            mapToArea(currentOwner, true, arg1, i);
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mapToArea(currentOwner, false, arg1, ptrI);
                        break;
                    // Sent "when the last pointer leaves the screen".
                    // I hope you aren't lying.
                    case MotionEvent.ACTION_UP:
                        mapToArea(currentOwner, false, arg1, 0);
                        currentOwner.peripherals.gdResetPointers();
                        break;
                }
                controlStackLock.unlock();
                return true;
            }

            private void mapToArea(GrInDriver owner, boolean mode, MotionEvent arg1, int ptrI) {
                float x = arg1.getX(ptrI);
                float y = arg1.getY(ptrI);
                x -= owner.displayArea.left;
                y -= owner.displayArea.top;
                x /= owner.displayArea.width();
                y /= owner.displayArea.height();
                x *= owner.w;
                y *= owner.h;
                owner.peripherals.gdPushEvent(mode, arg1.getPointerId(ptrI), (int) x, (int) y);
            }
        });
		setContentView(surfaceview);
		myTCO = new TextboxControlObject(this);
		mySurface = surfaceview;
		if (gameThread == null) {
			gameThread = new Thread(this);
			gameThread.start();
		}
		last = this;
	}

    public static GrInDriver getCurrentOwner() {
        controlStackLock.lock();
        GrInDriver gd = null;
        while (controlStack.size() > 0) {
            gd = controlStack.getLast();
            if (gd.wantsShutdown) {
                controlStack.removeLast();
                gd = controlStack.getLast();
                gd.peripherals.gdResetPointers();
            } else {
                break;
            }
        }
        controlStackLock.unlock();
        return gd;
    }

    public static void pushOwner(GrInDriver igd) {
        controlStackLock.lock();
        if (controlStack.size() > 0)
            controlStack.getLast().peripherals.gdResetPointers();
        controlStack.add(igd);
        controlStackLock.unlock();
    }

    @Override
	public void run() {
		try {
			GaBIenImpl.main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onBackPressed() {
        if (myTCO.tf.inTextboxMode) {
            myTCO.tf.setInactive();
        } else {
            super.onBackPressed();
        }
    }
}
