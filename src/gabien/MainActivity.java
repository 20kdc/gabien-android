/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
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
