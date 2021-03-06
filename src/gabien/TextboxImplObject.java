/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import gabien.ui.IFunction;

public class TextboxImplObject implements ITextboxImplementation {

    private final MainActivity mainActivity;
    private final EditText tf;
    private final TextView tv;
    private final LinearLayout host;
    private IFunction<String, String> lastFeedback = null;
    private String lastKnownContents = "";

    // Written on UI thread, read from MainActivity on UI thread.
    public boolean inTextboxMode;

    private boolean okay;

    public TextboxImplObject(MainActivity activity) {
        tf = new EditText(activity);
        tf.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        tv = new TextView(activity);
        host = new LinearLayout(activity);
        host.setOrientation(LinearLayout.HORIZONTAL);
        host.addView(tf);
        host.addView(tv);
        tf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (okay) {
                    String p = s.toString();
                    boolean disableOkay = false;
                    if (p.contains("\n")) {
                        p = p.replace("\n", "");
                        disableOkay = true;
                    }
                    lastKnownContents = p;
                    if (disableOkay)
                        okay = false;
                    if (lastFeedback != null) {
                        tv.setText(lastFeedback.apply(p));
                        host.requestLayout();
                    }
                }
            }
        });
        tf.setSingleLine(true);
        tf.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    okay = false;
                    return true;
                }
                return false;
            }
        });
        mainActivity = activity;
    }

    @Override
    public void setActive(final String contents, final IFunction<String, String> feedback) {
        lastKnownContents = contents;
        lastFeedback = feedback;
        okay = true;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tf.setText(contents);
                if (lastFeedback == null) {
                    tv.setText("");
                } else {
                    tv.setText(feedback.apply(contents));
                }
                host.requestLayout();
                // -- don't bother repeating this part if unnecessary --
                if (inTextboxMode)
                    return;
                inTextboxMode = true;
                System.out.println("TextBox is going active.");
                mainActivity.setContentView(host);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        host.requestFocus();
                        tf.requestFocus();
                        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null)
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    }
                });
            }
        });
    }

    @Override
    public void setInactive() {
        okay = false;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!inTextboxMode)
                    return;
                inTextboxMode = false;
                System.out.println("TextBox is going inactive.");
                mainActivity.setContentView(mainActivity.mySurface);
            }
        });
    }

    @Override
    public boolean checkupUsage() {
        return okay;
    }

    @Override
    public String getLastKnownText() {
        return lastKnownContents;
    }

    public static ITextboxImplementation getInstance() {
        if (MainActivity.last != null)
            return MainActivity.last.myTIO;
        return DEAD;
    }
    
    // I am aware it's odd to have a field down here, but it's for a good cause.
    // Used as a stand-in.
    private static ITextboxImplementation DEAD = new ITextboxImplementation() {
        @Override
        public void setInactive() {
        }
        @Override
        public void setActive(String contents, IFunction<String, String> feedback) {
        }
        @Override
        public String getLastKnownText() {
            return "";
        }
        @Override
        public boolean checkupUsage() {
            return false;
        }
    };
}

