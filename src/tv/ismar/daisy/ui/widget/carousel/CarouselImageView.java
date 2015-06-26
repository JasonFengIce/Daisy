package tv.ismar.daisy.ui.widget.carousel;

import android.content.Context;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * Created by huaijie on 6/26/15.
 */
public class CarouselImageView extends ImageView {

    private int position;
    private String videoImage;
    private String videoUrl;

    public String getVideoImage() {
        return videoImage;
    }

    public void setVideoImage(String videoImage) {
        this.videoImage = videoImage;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private boolean paly;


    public CarouselImageView(Context context) {
        super(context);
    }


    public boolean isPaly() {
        return paly;
    }

    public void setPaly(boolean paly) {
        if (paly) {
            setAlpha((float) 1);
        } else {
            setAlpha((float) 0.5);
        }
        this.paly = paly;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            setPaly(true);
        } else {
            setPaly(false);
        }
    }
}
