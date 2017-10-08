/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabien;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class TextboxImplObject {

    public final MainActivity mainActivity;
    public final EditText tf;
    public String lastKnownContents;

    public boolean okay;

    public TextboxImplObject(MainActivity activity) {
        tf = new EditText(activity);
        tf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String p = s.toString();
                boolean disableOkay = false;
                if (p.contains("\n")) {
                    p = p.replace("\n", "");
                    disableOkay = true;
                }
                lastKnownContents = p;
                if (disableOkay)
                    okay = false;
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

    public void setActive(final String contents) {
        lastKnownContents = contents;
        okay = true;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setContentView(tf);
                tf.setText(contents);
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tf.requestFocus();
                    }
                });
            }
        });
    }

    public void setInactive() {
        okay = false;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setContentView(mainActivity.msv);
            }
        });
    }

    public boolean checkupUsage() {
        return okay;
    }
}

