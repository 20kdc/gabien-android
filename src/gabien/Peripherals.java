/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import gabien.ui.IPointer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Just your ordinary IPCRESS peripherals implementation.
 * Created on 18th February 2018.
 */
public class Peripherals implements IPeripherals {
    // The parent GrInDriver. (GTHREAD)
    public GrInDriver parent;

    // The current offset. (GTHREAD)
    public int offsetX, offsetY;

    public ReentrantLock pointersLock = new ReentrantLock();
    public HashSet<UPointer> pointersAppendDown = new HashSet<UPointer>();
    public HashMap<Integer, UPointer> pointersMap = new HashMap<Integer, UPointer>();

    public Peripherals(GrInDriver gd) {
        parent = gd;
    }

    @Override
    public void performOffset(int x, int y) {
        offsetX += x;
        offsetY += y;
    }

    @Override
    public void clearOffset() {
        offsetX = 0;
        offsetY = 0;
    }

    @Override
    public HashSet<IPointer> getActivePointers() {
        pointersLock.lock();
        HashSet<IPointer> r = new HashSet<IPointer>(pointersMap.values());
        r.addAll(pointersAppendDown);
        pointersAppendDown.clear();
        pointersLock.unlock();
        return r;
    }

    @Override
    public void clearKeys() {
        parent.enterPressed = false;
    }

    @Override
    public String maintain(int x, int y, int width, String text) {
        return parent.sendMaintenanceCode(0, text);
    }

    @Override
    public boolean isEnterJustPressed() {
        boolean bv = parent.enterPressed;
        parent.enterPressed = false;
        return bv;
    }

    public void gdResetPointers() {
        pointersLock.lock();
        pointersMap.clear();
        pointersLock.unlock();
    }

    public void gdPushEvent(boolean mode, int pointerId, int x, int y) {
        pointersLock.lock();
        if (!mode) {
            pointersMap.remove(pointerId);
        } else {
            UPointer up = pointersMap.get(pointerId);
            if (up == null) {
                up = new UPointer();
                pointersMap.put(pointerId, up);
                pointersAppendDown.add(up);
            }
            up.actualX = x;
            up.actualY = y;
        }
        pointersLock.unlock();
    }

    private static class UPointer implements IPointer {
        public int offsetX, offsetY, actualX, actualY;
        @Override
        public int getX() {
            return offsetX + actualX;
        }

        @Override
        public int getY() {
            return offsetY + actualY;
        }

        @Override
        public PointerType getType() {
            return PointerType.Generic;
        }

        @Override
        public void performOffset(int x, int y) {
            offsetX += x;
            offsetY += y;
        }
    }
}
