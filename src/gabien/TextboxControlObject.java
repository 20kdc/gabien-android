/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

public class TextboxControlObject {
    public final MainActivity mainActivity;
    public final TextboxImplObject tf;
    boolean hasMaintained = false;
    String lastMaintainText = null;

    String tempMTO = null;
    boolean pendingEnter = false;

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
