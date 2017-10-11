/*
 * gabien-android - gabien backend for Android
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

public class MainActivity extends Activity implements Runnable {
	public static Thread t = null;
	public static SurfaceView sv = null;
	public static MainActivity last;
	public TextboxControlObject tco;
    public SurfaceView msv;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SurfaceView surfaceview = new SurfaceView(this);
		setContentView(surfaceview);
		tco = new TextboxControlObject(this);
		msv = sv = surfaceview;
		if (t == null) {
			t = new Thread(this);
			t.start();
		}
		last = this;
	}
	
	@Override
	public void run() {
		try {
			GaBIenImpl.main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
