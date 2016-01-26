package tv.ismar.daisy.qiyimediaplayer;

import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;

/**
 * Reference implementation of IPlaybackInfo
 */

public class SdkVideo implements IPlaybackInfo {

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
        return mAlbumId;
    }

    @Override
    public String getTvId() {
        return mTvId;
    }

    @Override
    public String getVid() {
        return mVid;
    }

    @Override
    public Definition getDefinition() {
        return mDefinition;
    }

	@Override
	public int getVideoSource() {
		// TODO Auto-generated method stub
		return 0;
	}
}