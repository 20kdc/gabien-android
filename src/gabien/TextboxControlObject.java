/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

public class TextboxControlObject {
    public final MainActivity mainActivity;
    public final TextboxImplObject tf;
    boolean hasMaintained = false;
    String lastMaintainText = null;

    String tempMTO = null;
    boolean pendingEnter = false;
    int timeout = 0;

    public TextboxControlObject(MainActivity activity) {
        tf = new TextboxImplObject(activity);
        mainActivity = activity;
    }

    // 0: maintain
    // 1: flush (expects return of non-null for enter response)
    // 2: activity check
    // 3: half-flush
    public String code(int i, String text) {
        boolean halfFlush = false;
        boolean enterPress = false;
        if (i == 0) {
            if (timeout > 0) {
                timeout--;
                return text;
            }
            if (lastMaintainText != null) {
                if (!text.equals(lastMaintainText)) {
                    tf.setActive(text);
                    lastMaintainText = text;
                } else {
                    lastMaintainText = tf.lastKnownContents;
                }
            } else if ((tempMTO == null) && (!pendingEnter)) {
                tf.setActive(text);
                lastMaintainText = text;
            }
            hasMaintained = true;
            if (tempMTO != null) {
                String s = tempMTO;
                tempMTO = null;
                pendingEnter = true;
                timeout = 4;
                return s;
            }
            return tf.lastKnownContents;
        } else if (i == 1) {
            if (!hasMaintained) {
                if (lastMaintainText != null) {
                    tf.setInactive();
                    lastMaintainText = null;
                }
            } else {
                halfFlush = true;
                if (pendingEnter) {
                    enterPress = true;
                    pendingEnter = false;
                }
            }
            hasMaintained = false;
        } else if (i == 2) {
            return lastMaintainText;
        } else if (i == 3) {
            halfFlush = true;
        }
        if (halfFlush) {
            if (lastMaintainText != null) {
                if (!tf.checkupUsage()) {
                    tempMTO = tf.lastKnownContents;
                    tf.setInactive();
                    lastMaintainText = null;
                    hasMaintained = false;
                }
            }
        }
        return enterPress ? "" : null;
    }
}
