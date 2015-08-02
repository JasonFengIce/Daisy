package tv.ismar.daisy.views;

import java.io.IOException;
import java.util.Map;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.SystemFileUtil;
import tv.ismar.player.SmartPlayer;
import tv.ismar.player.SmartPlayer.OnCompletionListenerUrl;
import tv.ismar.player.SmartPlayer.OnPreparedListenerUrl;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class IsmatvVideoView extends SurfaceView implements MediaPlayerControl {
	private String TAG = "VideoView";
	// settable by the client
	private String dataSource;
	private String[] paths;
	private Uri mUri;
	private Map<String, String> mHeaders;
	private int mDuration;

	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
//	private MediaPlayer mMediaPlayer = null;
	private SmartPlayer player = null;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private MediaController mMediaController;
	private SmartPlayer.OnCompletionListener mOnCompletionListener;
	private SmartPlayer.OnPreparedListener mOnPreparedListener;
	private SmartPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
	private SmartPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private SmartPlayer.OnPreparedListenerUrl mOnPreparedListenerUrl;
    private SmartPlayer.OnCompletionListenerUrl mOnCompletionListenerUrl;
	private int mCurrentBufferPercentage;
	private SmartPlayer.OnErrorListener mOnErrorListener;
	private SmartPlayer.OnInfoListener mOnInfoListener;
	private SmartPlayer.OnTsInfoListener mOnTsInfoListener;
	private int mSeekWhenPrepared; // recording the seek position while
									// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;
	private Context mContext;

	public IsmatvVideoView(Context context) {
		super(context);
		mContext = context;
		initVideoView();
	}

	public IsmatvVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
		initVideoView();
	}

	public IsmatvVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initVideoView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Log.i("@@@@", "onMeasure");
		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		if (mVideoWidth > 0 && mVideoHeight > 0) {
			if (mVideoWidth * height > width * mVideoHeight) {
				// Log.i("@@@", "image too tall, correcting");
				height = width * mVideoHeight / mVideoWidth;
			} else if (mVideoWidth * height < width * mVideoHeight) {
				// Log.i("@@@", "image too wide, correcting");
				width = height * mVideoWidth / mVideoHeight;
			} else {
				// Log.i("@@@", "aspect ratio is correct: " +
				// width+"/"+height+"="+
				// mVideoWidth+"/"+mVideoHeight);
			}
		}
		// Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
		setMeasuredDimension(width, height);
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(IsmatvVideoView.class.getName());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(IsmatvVideoView.class.getName());
	}

	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = desiredSize;
			break;

		case MeasureSpec.AT_MOST:
			/*
			 * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
			result = Math.min(desiredSize, specSize);
			break;

		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}
public void setVideoPaths(String[] paths){
	this.paths = paths;
    mUri = Uri.parse(paths[paths.length-1]);
    mHeaders = null;
    mSeekWhenPrepared = 0;
	openVideo();
	requestLayout();
	invalidate();
}
	public void setVideoPath(String path) {
		dataSource = path;
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	/**
	 * @hide
	 */
	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	public void playIndex(int index) {
		if (player != null) {
			try {
				player.playUrl(index);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void stopPlayback() {
		if (player != null) {
			player.stop();
			player.release();
			player = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}
	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			return;
		}
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);
		release(false);
		try {
			player = new SmartPlayer();
			player.setSn(SimpleRestClient.sn_token);
			player.setScreenOnWhilePlaying(true);
		//	player.setOnPreparedListener(mPreparedListener);
            if (SystemFileUtil.isCanWriteSD())
			   player.setSDCardisAvailable(true);
            else
                player.setSDCardisAvailable(false);
			player.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			player.setOnSeekCompleteListener(mOnSeekCompleteListener);
			//player.setOnCompletionListener(mOnCompletionListener);
			player.setOnErrorListener(mOnErrorListener);
			player.setOnInfoListener(mOnInfoListener);
			player.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
			player.setOnTsInfoListener(mOnTsInfoListener);
			mCurrentBufferPercentage = 0;
	        player.setDataSource(paths);
			player.setDisplay(mSurfaceHolder);
			player.setOnPreparedListenerUrl(new OnPreparedListenerUrl() {

				@Override
				public void onPrepared(SmartPlayer mp, String url) {
					// TODO Auto-generated method stub
                    mCurrentState = STATE_PREPARED;
                    player = mp;
                    // Get the capabilities of the player for this stream
                    // Metadata data = mp.getMetadata(false,
                    // true);
                    Metadata data = new Metadata();

                    if (data != null) {
                        mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
                                || data.getBoolean(Metadata.PAUSE_AVAILABLE);
                        mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
                                || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
                        mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
                                || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
                    } else {
                        mCanPause = mCanSeekBack = mCanSeekForward = true;
                    }

                    if (mOnPreparedListenerUrl != null) {
                        mOnPreparedListenerUrl.onPrepared(player,url);
                    }
                    if (mMediaController != null) {
                        mMediaController.setEnabled(true);
                    }
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();

                    int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
                    // changed after seekTo()
                    // call
                    if (seekToPosition != 0) {
                        seekTo(seekToPosition);
                    }
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        // Log.i("@@@@", "video size: " + mVideoWidth +"/"+
                        // mVideoHeight);
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        if (mSurfaceWidth == mVideoWidth
                                && mSurfaceHeight == mVideoHeight) {
                            // We didn't actually change the size (it was already at the
                            // size
                            // we need), so we won't get a "surface changed" callback,
                            // so
                            // start the video here instead of in the callback.
                            if (mTargetState == STATE_PLAYING) {
                                start();
                                if (mMediaController != null) {
                                    mMediaController.show();
                                }
                            } else if (!isPlaying()
                                    && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                                if (mMediaController != null) {
                                    // Show the media controls when we're paused into a
                                    // video and make 'em stick.
                                    mMediaController.show(0);
                                }
                            }
                        }
                    } else {
                        // We don't know the video size yet, but should start anyway.
                        // The video size might be reported to us later.
                        if (mTargetState == STATE_PLAYING) {
                            start();
                        }
                    }
				}
			});

			player.setOnCompletionListenerUrl(new OnCompletionListenerUrl() {
				
				@Override
				public void onCompletion(SmartPlayer sp, String url) {
					// TODO Auto-generated method stub
                    player = sp;
					//int index = findVideoUrlIndex(url);
                    if(mOnCompletionListenerUrl!=null){
                        mOnCompletionListenerUrl.onCompletion(sp,url);
                    }
                    if(paths.length == 1)
                    	return;
					int currentIndex = sp.getCurrentPlayUrl();
					
					if (currentIndex >= 0 && currentIndex<paths.length) {        //如果当前播放的为第一个影片的话，则准备播放第二个影片。
						try {
							currentIndex++;
							sp.playUrl(currentIndex);                               //准备播放第二个影片，传入参数为1，第二个影片在数组中的下标。
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setScreenOnWhilePlaying(true);
			player.prepareAsync();
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		}catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(player,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (Exception ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(player,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} 
	}
private int findVideoUrlIndex(String url){
	int index = -1;
	int size = paths.length;
	for(int i=0;i<size;i++){
		if(url.equals(paths[i])){
			index = i;
			break;
		}
	}
	return index;
}
	public void setMediaController(MediaController controller) {
		if (mMediaController != null) {
			mMediaController.hide();
		}
		mMediaController = controller;
		attachMediaController();
	}

	private void attachMediaController() {
		if (player != null && mMediaController != null) {
			mMediaController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ? (View) this
					.getParent() : this;
			mMediaController.setAnchorView(anchorView);
			mMediaController.setEnabled(isInPlaybackState());
		}
	}
	SmartPlayer.OnPreparedListener mPreparedListener = new SmartPlayer.OnPreparedListener() {
		public void onPrepared(SmartPlayer mp) {
			mCurrentState = STATE_PREPARED;

			// Get the capabilities of the player for this stream
			// Metadata data = mp.getMetadata(false,
			// true);
			Metadata data = new Metadata();

			if (data != null) {
				mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
						|| data.getBoolean(Metadata.PAUSE_AVAILABLE);
				mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
						|| data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
				mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
						|| data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
			} else {
				mCanPause = mCanSeekBack = mCanSeekForward = true;
			}

			if (mOnPreparedListener != null) {
				mOnPreparedListener.onPrepared(player);
			}
			if (mMediaController != null) {
				mMediaController.setEnabled(true);
			}
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();

			int seekToPosition = mSeekWhenPrepared; // mSeekWhenPrepared may be
													// changed after seekTo()
													// call
			if (seekToPosition != 0) {
				seekTo(seekToPosition);
			}
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				// Log.i("@@@@", "video size: " + mVideoWidth +"/"+
				// mVideoHeight);
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				if (mSurfaceWidth == mVideoWidth
						&& mSurfaceHeight == mVideoHeight) {
					// We didn't actually change the size (it was already at the
					// size
					// we need), so we won't get a "surface changed" callback,
					// so
					// start the video here instead of in the callback.
					if (mTargetState == STATE_PLAYING) {
						start();
						if (mMediaController != null) {
							mMediaController.show();
						}
					} else if (!isPlaying()
							&& (seekToPosition != 0 || getCurrentPosition() > 0)) {
						if (mMediaController != null) {
							// Show the media controls when we're paused into a
							// video and make 'em stick.
							mMediaController.show(0);
						}
					}
				}
			} else {
				// We don't know the video size yet, but should start anyway.
				// The video size might be reported to us later.
				if (mTargetState == STATE_PLAYING) {
					start();
				}
			}
		}
	};

	private SmartPlayer.OnErrorListener mErrorListener = new SmartPlayer.OnErrorListener() {
		public boolean onError(SmartPlayer mp, int framework_err, int impl_err) {
			Log.d(TAG, "Error: " + framework_err + "," + impl_err);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			if (mMediaController != null) {
				mMediaController.hide();
			}

			/* If an error handler has been supplied, use it and finish. */
			if (mOnErrorListener != null) {
				if (mOnErrorListener.onError(player, framework_err,
						impl_err)) {
					return true;
				}
			}

			/*
			 * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
			if (getWindowToken() != null) {
				int messageId;

				if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
					messageId = R.string.VideoView_error_text_invalid_progressive_playback;
				} else {
					messageId = R.string.VideoView_error_text_unknown;
				}

				new AlertDialog.Builder(mContext)
						.setMessage(messageId)
						.setPositiveButton(R.string.VideoView_error_button,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/*
										 * If we get here, there is no onError
										 * listener, so at least inform them
										 * that the video is over.
										 */
										if (mOnCompletionListener != null) {
											mOnCompletionListener
													.onCompletion(player);
										}
									}
								}).setCancelable(false).show();
			}
			return true;
		}
	};
	
	SmartPlayer.OnVideoSizeChangedListener mSizeChangedListener = new SmartPlayer.OnVideoSizeChangedListener() {
		public void onVideoSizeChanged(SmartPlayer mp, int width, int height) {
			mVideoWidth = mp.getVideoWidth();
			mVideoHeight = mp.getVideoHeight();
			if (mVideoWidth != 0 && mVideoHeight != 0) {
				getHolder().setFixedSize(mVideoWidth, mVideoHeight);
				requestLayout();
			}
		}
	};

	/**
	 * Register a callback to be invoked when the media file is loaded and ready
	 * to go.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnPreparedListener(SmartPlayer.OnPreparedListener l) {
		mOnPreparedListener = l;
	}

	/**
	 * Register a callback to be invoked when the end of a media file has been
	 * reached during playback.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnCompletionListener(SmartPlayer.OnCompletionListener l) {
		mOnCompletionListener = l;
	}

	/**
	 * Register a callback to be invoked when an error occurs during playback or
	 * setup. If no listener is specified, or if the listener returned false,
	 * VideoView will inform the user of any errors.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	public void setOnErrorListener(SmartPlayer.OnErrorListener l) {
		mOnErrorListener = l;
	}

	/**
	 * Register a callback to be invoked when an informational event occurs
	 * during playback or setup.
	 * 
	 * @param l
	 *            The callback that will be run
	 */
	
	public void setOnSeekCompleteListener(SmartPlayer.OnSeekCompleteListener l) {
		mOnSeekCompleteListener = l;
	}

	public void setOnBufferingUpdateListener(SmartPlayer.OnBufferingUpdateListener l){
		mOnBufferingUpdateListener =l;
	}

	public void setOnTsInfoListener(SmartPlayer.OnTsInfoListener l){
		mOnTsInfoListener = l;
	}

	public void setOnInfoListener(SmartPlayer.OnInfoListener l) {
		mOnInfoListener = l;
	}

    public void setOnPreparedListenerUrl(OnPreparedListenerUrl l){
        this.mOnPreparedListenerUrl = l;
    }
    public void setOnCompletionListenerUrl(OnCompletionListenerUrl l){
        this.mOnCompletionListenerUrl = l;
    }
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (player != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mMediaController != null)
				mMediaController.hide();
			release(true);
		}
	};

	/*
	 * release the media player in any state
	 */
	private void release(boolean cleartargetstate) {
		if (player != null) {
			player.reset();
			player.release();
			player = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent ev) {
		if (isInPlaybackState() && mMediaController != null) {
			toggleMediaControlsVisiblity();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
				&& keyCode != KeyEvent.KEYCODE_VOLUME_UP
				&& keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
				&& keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
				&& keyCode != KeyEvent.KEYCODE_MENU
				&& keyCode != KeyEvent.KEYCODE_CALL
				&& keyCode != KeyEvent.KEYCODE_ENDCALL;
		if (isInPlaybackState() && isKeyCodeSupported
				&& mMediaController != null) {
			if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
				if (player.isPlaying()) {
					pause();
					mMediaController.show();
				} else {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
				if (!player.isPlaying()) {
					start();
					mMediaController.hide();
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
					|| keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
				if (player.isPlaying()) {
					pause();
					mMediaController.show();
				}
				return true;
			} else {
				toggleMediaControlsVisiblity();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	private void toggleMediaControlsVisiblity() {
		if (mMediaController.isShowing()) {
			mMediaController.hide();
		} else {
			mMediaController.show();
		}
	}

	public void start() {
		if (isInPlaybackState()) {
			player.start();
			mCurrentState = STATE_PLAYING;
		}
		mTargetState = STATE_PLAYING;
	}

	public void pause() {
		if (isInPlaybackState()) {
			if (player.isPlaying()) {
				player.pause();
				mCurrentState = STATE_PAUSED;
			}
		}
		mTargetState = STATE_PAUSED;
	}

	public void suspend() {
		release(false);
	}

	public void resume() {
		openVideo();
	}

	// cache duration as mDuration for faster access
	public int getDuration() {
		if (isInPlaybackState()) {
			if (mDuration > 0) {
				return mDuration;
			}
			mDuration = player.getDuration();
			return mDuration;
		}
		mDuration = -1;
		return mDuration;
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return player.getCurrentPosition();
		}
		return 0;
	}

	public void seekTo(final int msec) {
		if (isInPlaybackState()) {
			new Thread(){
				@Override
				public void run() {
					super.run();
					player.seekTo(msec);
				}
			}.start();
			mSeekWhenPrepared = 0;
		} else {
			mSeekWhenPrepared = msec;
		}
	}

	public boolean isPlaying() {
		return isInPlaybackState() && player.isPlaying();
	}

	public int getBufferPercentage() {
		if (player != null) {
			return mCurrentBufferPercentage;
		}
		return 0;
	}

	private boolean isInPlaybackState() {
		return (player != null && mCurrentState != STATE_ERROR
				&& mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
	}

	public boolean canPause() {
		return mCanPause;
	}

	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	public boolean canSeekForward() {
		return mCanSeekForward;
	}

	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}
}