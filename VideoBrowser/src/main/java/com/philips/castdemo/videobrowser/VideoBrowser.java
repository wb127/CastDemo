/*
 * Copyright (C) 2013 Woox Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.philips.castdemo.videobrowser;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IVideoCastConsumer;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.sample.castcompanionlibrary.utils.Utils;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.philips.castdemo.videobrowser.mediaplayer.LocalPlayerActivity;
import com.philips.castdemo.videobrowser.settings.CastPreference;
import com.philips.castdemo.videobrowser.utils.VideoProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VideoBrowser extends ActionBarActivity {
    private static final String TAG = "VideoBrowser";
    private String mPath;
    private int stramType = MediaInfo.STREAM_TYPE_INVALID;
    private String mediaType;
    private EditText videopath;
    private Spinner videolink;
    //private VideoView mVideoView;

    private MediaInfo mMediaInfo;
    private MediaPlayer mMediaPlayer;
    private static final String VIDEO_URL0 = "http://137.116.170.232:3000/record/20004/stream.m3u8";
    private static final String VIDEO_URL1 = "http://137.116.170.232:3000/record/20012/stream.m3u8";
    private static final String VIDEO_URL2 = "http://download.clockworkmod.com/cast/bitrate-500000-bps.m3u8";
    private static final String VIDEO_URL3 = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
    private static final String VIDEO_URL4 = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
    private static final String VIDEO_URL5 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    private static final String VIDEO_URL6 = "/sdcard/Movies/bbb_short.ffmpeg.720x480.mp4.libx264_1350kbps_30fps.libfaac_stereo_192kbps_44100Hz.mp4";

    private static final String[] VIDEO_URL={
            VIDEO_URL0,
            VIDEO_URL1,
            VIDEO_URL2,
            VIDEO_URL3,
            VIDEO_URL4,
            VIDEO_URL5,
            VIDEO_URL6
    };
    private static final String[] PLAY_TYPE={
            "VideoView",
            "Intent",
            "MediaPlayer"
    };
    private int item_selected = 0;
    private static ArrayAdapter<String> adapter;

    //CCL
    private VideoCastManager mCastManager;
    private IVideoCastConsumer mCastConsumer;
    private MiniController mMini;
    private MenuItem mediaRouteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean result = VideoCastManager.checkGooglePlayServices(this);
        setContentView(R.layout.activity_video_browser);
        ActionBar actionBar = getSupportActionBar();
        mCastManager = CastApplication.getCastManager(this);

        // -- Adding MiniController
        mMini = (MiniController) findViewById(R.id.miniController1);
        mCastManager.addMiniController(mMini);

        mCastConsumer = new VideoCastConsumerImpl() {

            @Override
            public void onFailed(int resourceId, int statusCode) {

            }

            @Override
            public void onConnectionSuspended(int cause) {
                Log.d(TAG, "onConnectionSuspended() was called with cause: " + cause);
                com.philips.castdemo.videobrowser.utils.Utils.
                        showToast(VideoBrowser.this, R.string.play);
            }

            @Override
            public void onConnectivityRecovered() {
                com.philips.castdemo.videobrowser.utils.Utils.
                        showToast(VideoBrowser.this, R.string.connection_recovered);
            }

            @Override
            public void onCastDeviceDetected(final RouteInfo info) {
                if (!CastPreference.isFtuShown(VideoBrowser.this)) {
                    CastPreference.setFtuShown(VideoBrowser.this);

                    Log.d(TAG, "Route is visible: " + info);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                Log.d(TAG, "Cast Icon is visible: " + info.getName());
                                showFtu();
                            }
                        }
                    }, 1000);
                }
            }
        };

        setupActionBar(actionBar);
        mCastManager.reconnectSessionIfPossible(this, false);


        videopath = (EditText) findViewById(R.id.video_path);
        videolink = (Spinner) findViewById(R.id.video_link);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, VIDEO_URL);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        videolink.setAdapter(adapter);
        videolink.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                       long arg3) {
                item_selected = arg2;
                if(VIDEO_URL[item_selected] != null) {
                    mPath = VIDEO_URL[item_selected];
                }
                videopath.setText(mPath.toCharArray(),0,mPath.length());
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        videolink.setVisibility(View.VISIBLE);
    }

    private void setupActionBar(ActionBar actionBar) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //getSupportActionBar().setIcon(R.drawable.actionbar_logo_castvideos);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.video_browser, menu);

        mediaRouteMenuItem = mCastManager.
                addMediaRouterButton(menu, R.id.media_route_menu_item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(VideoBrowser.this, CastPreference.class);
                startActivity(i);
                break;
        }
        return true;
    }

    private void showFtu() {
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCastManager.isConnected()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                changeVolume(CastApplication.VOLUME_INCREMENT);
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                changeVolume(-CastApplication.VOLUME_INCREMENT);
            } else {
                // we don't want to consume non-volume key events
                return super.onKeyDown(keyCode, event);
            }
            if (mCastManager.getPlaybackStatus() == MediaStatus.PLAYER_STATE_PLAYING) {
                return super.onKeyDown(keyCode, event);
            } else {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeVolume(double volumeIncrement) {
        if (mCastManager == null) {
            return;
        }
        try {
            mCastManager.incrementVolume(volumeIncrement);
        } catch (Exception e) {
            Log.e(TAG, "onVolumeChange() Failed to change volume", e);
            com.philips.castdemo.videobrowser.utils.Utils.handleException(this, e);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = CastApplication.getCastManager(this);
        if (null != mCastManager) {
            mCastManager.addVideoCastConsumer(mCastConsumer);
            mCastManager.incrementUiCounter();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is called");
        if (null != mCastManager) {
            mMini.removeOnMiniControllerChangedListener(mCastManager);
            mCastManager.removeMiniController(mMini);
            mCastManager.clearContext(this);
        }
        super.onDestroy();
    }
    public void OnPlayClick(View appview)
    {

        switch(appview.getId()) {
            case R.id.button_play:
                runOnUiThread(new Runnable() {
                    public void run() {
                        playVideo();
                    }
                });
                break;
        }
    }

    private void playVideo() {
        mPath = videopath.getText().toString();
        final Uri path = Uri.parse(mPath);
        Log.v(TAG, "path: " + path);
        if (mPath == null || mPath.length() == 0) {
            Toast.makeText(VideoBrowser.this, "File URL/path is empty",
                    Toast.LENGTH_LONG).show();
        } else {
            if((mPath != null) && (mPath.contains("://")))
            {
                /*HLS: application/x-mpegurl or application/vnd.apple.mpegurl
                  DASH: application/dash+xml
                  Smooth Streaming: application/vnd.ms-sstr+xml
                */
                if(mPath.contains(".m3u8"))
                {
                    //mediaType = "application/x-mpegurl";
                    mediaType = "application/vnd.apple.mpegurl";
                }
                else if(mPath.contains(".mp4"))
                {
                    mediaType = "video/mp4";
                }
                stramType = MediaInfo.STREAM_TYPE_BUFFERED;
                //stramType = MediaInfo.STREAM_TYPE_LIVE;
            }
            else if((mPath != null) && (mPath.contains("/sdcard")))
            {
                stramType = MediaInfo.STREAM_TYPE_BUFFERED;
            }
            mMediaInfo = VideoProvider.buildMediaInfo("Philips Cast Demo","This Demo Application depended Google CCL library", "", mPath, "", "http://photos.tuchong.com/31916/f/6748393.jpg", stramType ,mediaType);
            Intent intent = new Intent(this, LocalPlayerActivity.class);
            intent.putExtra("media", Utils.fromMediaInfo(mMediaInfo));
            intent.putExtra("shouldStart", false);
            intent.putExtra("streamType", stramType);
            this.startActivity(intent);

        }
    }
}
