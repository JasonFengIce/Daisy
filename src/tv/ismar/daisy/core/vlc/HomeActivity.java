package tv.ismar.daisy.core.vlc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCUtil;
import tv.ismar.daisy.VodApplication;

public class HomeActivity extends Activity implements IVLCVout.Callback, LibVLC.HardwareAccelerationError,
        PlaybackService.Client.Callback, PlaybackService.Callback {
    private static final String TAG = "HomeActivity";

    public final static String EXTRA_POSITION = "extra_position";
    public final static String EXTRA_DURATION = "extra_duration";
    public final static String ACTION_RESULT = "org.videolan.vlc.player.result";

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;


    private final PlaybackServiceActivity.Helper mHelper = new PlaybackServiceActivity.Helper(this, this);

    private PlaybackService mService;
    private Uri mUri;

    private SharedPreferences mSettings;
    private AudioManager mAudioManager;
    private int mAudioMax;
    private boolean mEnableCloneMode;

    private SurfaceView mSurface;


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = Uri.parse("http://jq.v.ismartv.tv/topvideo/464359AA0657CDEE699D507D5FC8D77A.mp4");

        boolean flag = VLCUtil.hasCompatibleCPU(this);
        Log.d(TAG, "flag: " + flag);



        /* Services and miscellaneous */
        mAudioManager = (AudioManager) VodApplication.getAppContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

//        setContentView(R.layout.main);
//
//        mSurface = (SurfaceView) findViewById(R.id.player_surface);


        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mHelper.onStart();


    }


    private static LibVLC LibVLC() {
        return VLCInstance.get();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startPlayback() {

        LibVLC().setOnHardwareAccelerationError(this);
        final IVLCVout vlcVout = mService.getVLCVout();

        vlcVout.setVideoView(mSurface);
        vlcVout.addCallback(this);
        vlcVout.attachViews();


        final MediaWrapper mw = new MediaWrapper(mUri);
        mw.removeFlags(MediaWrapper.MEDIA_FORCE_AUDIO);
        mw.addFlags(MediaWrapper.MEDIA_VIDEO);
        mService.addCallback(this);
        mService.load(mw);
        mService.play();

    }


    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mSarNum = sarNum;
        mSarDen = sarDen;
        changeSurfaceLayout();
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void update() {

    }

    @Override
    public void updateProgress() {

    }

    @Override
    public void onMediaEvent(Media.Event event) {

    }

    @Override
    public void onMediaPlayerEvent(MediaPlayer.Event event) {

    }

    @Override
    public void onMediaIndexChange(MediaWrapperList mediaWrapperList, int position) {

    }

    @Override
    public void eventHardwareAccelerationError() {

    }

    @Override
    public void onConnected(PlaybackService service) {
        mService = service;
        mService.addCallback(this);
        startPlayback();
    }

    @Override
    public void onDisconnected() {
        mService = null;

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeSurfaceLayout() {
        int sw;
        int sh;

        // get screen size
        sw = getWindow().getDecorView().getWidth();
        sh = getWindow().getDecorView().getHeight();

        final IVLCVout vlcVout = mService.getVLCVout();
        vlcVout.setWindowSize(sw, sh);

        double dw = sw, dh = sh;
        boolean isPortrait;

        // getWindow().getDecorView() doesn't always take orientation into account, we have to correct the values
        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mSarDen == mSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mSarNum / mSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        SurfaceView surface;

        surface = mSurface;

        // set display size
        ViewGroup.LayoutParams lp = surface.getLayoutParams();
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surface.setLayoutParams(lp);
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);

        surface.invalidate();
    }
}
