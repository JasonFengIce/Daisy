package tv.ismar.daisy.qiyimediaplayer;

import java.io.Serializable;

import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;
import com.qiyi.video.utils.LogUtils;

/**
 * Reference implementation of IPlaybackInfo
 */

public class SdkVideo implements IPlaybackInfo,Serializable {

	private static final long serialVersionUID = -7717185072913133522L;
	private static final String TAG = "SdkVideo";
    
    public void setmAlbumId(String mAlbumId) {
		this.mAlbumId = mAlbumId;
	}

	public void setmTvId(String mTvId) {
		this.mTvId = mTvId;
	}

	public void setmVid(String mVid) {
		this.mVid = mVid;
	}

	public void setmDefinition(Definition mDefinition) {
		this.mDefinition = mDefinition;
	}

	private String mAlbumId;
    private String mTvId;
    private String mVid;
    private Definition mDefinition;
    public SdkVideo(String albumId, String tvId, String vid, Definition definition) {
        mAlbumId = albumId;
        mTvId = tvId;
        mVid = vid;
        mDefinition = definition;
    }
    public SdkVideo(){
    	
    }
    public SdkVideo(String albumId, String tvId, String vid) {
        mAlbumId = albumId;
        mTvId = tvId;
        mVid = vid;
    }

    @Override
    public String getAlbumId() {
        if (LogUtils.mIsDebug) LogUtils.d(TAG, "getAlbumId() returns " + mAlbumId);
        return mAlbumId;
    }

    @Override
    public String getTvId() {
        if (LogUtils.mIsDebug) LogUtils.d(TAG, "getTvId() returns " + mTvId);
        return mTvId;
    }

    @Override
    public String getVid() {
        if (LogUtils.mIsDebug) LogUtils.d(TAG, "getVid() returns " + mVid);
        return mVid;
    }

    @Override
    public Definition getDefinition() {
        return mDefinition;
    }
}