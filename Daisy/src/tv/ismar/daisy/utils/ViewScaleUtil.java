package tv.ismar.daisy.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.view.View;

import tv.ismar.daisy.R;


/**
 * Created by huaijie on 1/31/16.
 */
public class ViewScaleUtil {
    public static void zoomout_1_15(View view) {

        Animator animator = AnimatorInflater.loadAnimator(view.getContext(), R.animator.zoom_out_1_15);
        animator.setTarget(view);
        animator.start();
    }

    public static void zoomin_1_15(View view) {

        Animator animator = AnimatorInflater.loadAnimator(view.getContext(), R.animator.zoom_in_1_15);
        animator.setTarget(view);
        animator.start();
    }

    public static void zoomin_1_3(View view) {

        Animator animator = AnimatorInflater.loadAnimator(view.getContext(), R.animator.zoom_in_1_3);
        animator.setTarget(view);
        animator.start();
    }

    public static void zoomout_1_3(View view) {

        Animator animator = AnimatorInflater.loadAnimator(view.getContext(), R.animator.zoom_out_1_3);
        animator.setTarget(view);
        animator.start();
    }
}
