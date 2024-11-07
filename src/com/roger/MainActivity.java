package com.roger;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

// add for setDisplayLayerStack
import android.os.IBinder;
import android.view.SurfaceControl;
import com.android.server.display.DisplayControl;

/**
 * 多屏幕时，如何将内容显示到指定的Display，提供两种方式
 *  1. 把Activity直接显示到指定的Dispaly(推荐);
 *  2. 使用Presentation把内容显示到指定的Display(不推荐，有限制)
 */

public class MainActivity extends Activity implements OnClickListener {
    private final static String TAG = "MainActivity";
    private Button btnStartActivity = null;
    private Button btnStartPresentation = null;
    private Button btnSameDisplay = null;
    private Button btnDifferentDisplay = null;
    
    static {
        try{
            System.loadLibrary("android_servers");
            Log.d(TAG, "load android_servers success");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "load android_servers failed");
        }
    }
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        
        //Thread.dumpStack();
        
        btnStartActivity = (Button) findViewById(R.id.btnStartActivity);
        btnStartActivity.setOnClickListener(this);
        btnStartPresentation = (Button) findViewById(R.id.btnStartPresentation);
        btnStartPresentation.setOnClickListener(this);
        btnSameDisplay = (Button) findViewById(R.id.btnSameDisplay);
        btnSameDisplay.setOnClickListener(this);
        btnDifferentDisplay = (Button) findViewById(R.id.btnDifferentDisplay);
        btnDifferentDisplay.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Thread.dumpStack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Thread.dumpStack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Thread.dumpStack();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Thread.dumpStack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Thread.dumpStack();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartActivity:
                showSecondActivity();
                break;
            case R.id.btnStartPresentation:
                showPresentation();
                break;
            case R.id.btnSameDisplay:
                showSameDisplay();
                break;
            case R.id.btnDifferentDisplay:
                showDifferentDisplay();
                break;
            default:
                Log.d(TAG, "do nothing ...");
        }
    }

    public void showSecondActivity() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        // print display info to debug
        for(Display display : displays)
            Log.d(TAG, display.toString());

        if (displays.length < 2) {
            Log.e("TAG", "no secondary display");
            Toast.makeText(this, "no secondary display, do nothing  !!!", Toast.LENGTH_SHORT).show();
            return;
        }

        ActivityOptions opts = ActivityOptions.makeBasic();
        opts.setLaunchDisplayId(displays[1].getDisplayId());
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.roger", "com.roger.SecondActivity");
        intent.setComponent(comp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 								| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent, opts.toBundle());
    }

    public class MyPresentation extends Presentation {
        public MyPresentation(Context outerContext, Display display) {
            super(outerContext,display);
    
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_second);
        }
    }

    MyPresentation mPresentation;

    private void showPresentation() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

        if (displays.length < 1) {
            Log.e("TAG", "no Presentation display");
            return;
        }

        //判断是否有多个屏，只有Display.FLAG_PRESENTATION的Display才可以显示Presentation
        // 否则错误--Attempted to add presentation window to a non-suitable display
        Display display = displays[1];
        if (mPresentation == null) {
            mPresentation = new MyPresentation(this, display);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException e) {
                mPresentation = null;
            }
        }
    }
    
    public void showSameDisplay() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        // print display info to debug
        for(Display display : displays)
            Log.d(TAG, display.toString());

        if (displays.length < 2) {
            Log.e("TAG", "no secondary display");
            Toast.makeText(this, "no secondary display, do nothing  !!!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        long[] ids = DisplayControl.getPhysicalDisplayIds();
        for(long id : ids) {
            Log.d(TAG, "id = " + id);
        
            final IBinder displayToken = DisplayControl.getPhysicalDisplayToken(id);
            if (displayToken == null) {
                Log.e(TAG, "Display token is null.");
                continue;
            }
            
            SurfaceControl.Transaction t = new SurfaceControl.Transaction();
            t.setDisplayLayerStack(displayToken, 0);
            t.apply();
            Log.e("TAG", "setDisplayLayerStack 0");
        }
    }

    public void showDifferentDisplay() {
        DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();
        // print display info to debug
        for(Display display : displays)
            Log.d(TAG, display.toString());

        if (displays.length < 2) {
            Log.e("TAG", "no secondary display");
            Toast.makeText(this, "no secondary display, do nothing  !!!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int layerStack = 0;
        long[] ids = DisplayControl.getPhysicalDisplayIds();
        for(long id : ids) {
            Log.d(TAG, "id = " + id);
        
            final IBinder displayToken = DisplayControl.getPhysicalDisplayToken(id);
            if (displayToken == null) {
                Log.e(TAG, "Display token is null.");
                continue;
            }
            
            SurfaceControl.Transaction t = new SurfaceControl.Transaction();
            t.setDisplayLayerStack(displayToken, layerStack++);
            t.apply();
            Log.e("TAG", "setDisplayLayerStack 1");
        }
    }

}
