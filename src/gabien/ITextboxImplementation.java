/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package gabien;

import gabien.ui.IFunction;

public interface ITextboxImplementation {
    // Gets the last known text.
    String getLastKnownText();
    // Makes the textbox active, overwriting text/etc.
    // Immediately updates last known text.
    void setActive(final String contents, final IFunction<String, String> feedback);
    // Makes the textbox inactive.
    void setInactive();
    // Returns true if the textbox is currently active, false otherwise.
    boolean checkupUsage();
}
