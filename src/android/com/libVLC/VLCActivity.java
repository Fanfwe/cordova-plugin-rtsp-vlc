package com.libVLC;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.webmons.disono.vlc.VlcListener;
import com.webmons.disono.vlc.VlcVideoLibrary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: Archie, Disono (webmonsph@gmail.com)
 * Website: http://www.webmons.com
 * <p>
 * Created at: 1/09/2018
 */

public class VLCActivity extends Activity implements VlcListener, View.OnClickListener {
    private Activity activity;
    public static final String BROADCAST_LISTENER = "com.libVLC.Listener";
    public final String TAG = "VLCActivity";

    SurfaceView surfaceView;
    private VlcVideoLibrary vlcVideoLibrary;

    private String _url;

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String method = intent.getStringExtra("method");

                if (method != null) {
                    if (method.equals("pause")) {
                        if (vlcVideoLibrary.isPlaying()) {
                            vlcVideoLibrary.pause();
                        }
                    }
                    else if (method.equals("resume")) {
                        if (vlcVideoLibrary.isPlaying()) {
                            vlcVideoLibrary.getPlayer().play();
                        }
                    }
                    else if (method.equals("stop")) {
                        if (vlcVideoLibrary.isPlaying()) {
                            vlcVideoLibrary.stop();
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        activity = this;
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(_getResource("vlc_player", "layout"));
        _UIListener();
        _broadcastRCV();

        Intent intent = getIntent();
        _url = intent.getStringExtra("url");

        // play
        _initPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (vlcVideoLibrary.isPlaying()) {
            vlcVideoLibrary.getPlayer().stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (vlcVideoLibrary.isPlaying()) {
            vlcVideoLibrary.getPlayer().play();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.unregisterReceiver(br);

        vlcVideoLibrary.stop();
        _sendBroadCast("onDestroyVlc");
    }

    @Override
    public void onClick(View v) {
        if (!vlcVideoLibrary.isPlaying()) {
            vlcVideoLibrary.play(_url);
        } else {
            vlcVideoLibrary.pause();
        }
    }

    @Override
    public void onPlayVlc() {
        _sendBroadCast("onPlayVlc");
    }

    @Override
    public void onPauseVlc() {
        _sendBroadCast("onPauseVlc");
    }

    @Override
    public void onStopVlc() {
        _sendBroadCast("onStopVlc");
    }

    @Override
    public void onVideoEnd() {
        _sendBroadCast("onVideoEnd");
    }

    @Override
    public void onError() {
        _sendBroadCast("onError");

        if (vlcVideoLibrary != null) {
            vlcVideoLibrary.stop();
        }
    }

    private void _initPlayer() {
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    if (vlcVideoLibrary != null && _url != null) {
                        if (vlcVideoLibrary.isPlaying()) {
                            vlcVideoLibrary.stop();
                        }

                        vlcVideoLibrary.play(_url);
                    }
                }
            },
            300
        );
    }

    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter(VideoPlayerVLC.BROADCAST_METHODS);
        activity.registerReceiver(br, filter);
    }

    private void _UIListener() {
        surfaceView = (SurfaceView) findViewById(_getResource("vlc_surfaceView", "id"));

        vlcVideoLibrary = new VlcVideoLibrary(this, this, surfaceView);
    }

    /**
     * Resource ID
     *
     * @param name
     * @param type layout, drawable, id
     * @return
     */
    private int _getResource(String name, String type) {
        String package_name = getApplication().getPackageName();
        Resources resources = getApplication().getResources();
        return resources.getIdentifier(name, type, package_name);
    }

    private void _sendBroadCast(String methodName) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }

    private void _sendBroadCast(String methodName, JSONObject object) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        intent.putExtra("data", object.toString());
        activity.sendBroadcast(intent);
    }
}
