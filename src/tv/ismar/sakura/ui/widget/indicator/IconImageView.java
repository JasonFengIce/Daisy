package tv.ismar.sakura.ui.widget.indicator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

/**
 * Created by huaijie on 2015/4/8.
 */
public class IconImageView extends ImageView {
    private boolean selected = false;


    public IconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleY(0.5f);
        setScaleX(0.5f);
    }


    public void setSelect(boolean selected) {

        if (selected) {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.5f, 1, 1.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(200);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            startAnimation(animationSet);

        } else if (this.selected) {
            AnimationSet animationSet = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(1.5f, 1f, 1.5f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(200);
            animationSet.addAnimation(scaleAnimation);
            animationSet.setFillAfter(true);
            startAnimation(animationSet);
        }
        this.selected = selected;
        invalidate();
    }


}
