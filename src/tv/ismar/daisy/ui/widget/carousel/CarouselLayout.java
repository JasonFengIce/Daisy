package tv.ismar.daisy.ui.widget.carousel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import tv.ismar.daisy.data.HomePagerEntity;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by huaijie on 6/26/15.
 */
public class CarouselLayout extends LinearLayout {
    LinkedList<CarouselImageView> linkedList;
    private int manualPostion;
    private int autoPosition;



    public CarouselLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void starLoop(ArrayList<HomePagerEntity.Carousel> carousels) {
        int count = getChildCount();
        linkedList = new LinkedList<CarouselImageView>();

        for (int i = 0; i < count; i++) {
            CarouselImageView carouselImageView = (CarouselImageView) getChildAt(i);
            carouselImageView.setPosition(i);
            carouselImageView.setVideoImage(carousels.get(i).getVideo_image());
            carouselImageView.setVideoUrl(carousels.get(i).getVideo_url());
            linkedList.add(carouselImageView);
        }


    }


//    public void setCurrentPosition(int position) {
//        CarouselImageView carouselImageView =  linkedList.removeFirst();
//        linkedList.addLast(carouselImageView);
//        if (carouselImageView.getPosition() != position){
//            setCurrentPosition(position);
//        }else {
//            carouselImageView.setPaly(true);
//
//        }
//
//
//    }



    public interface onItemChangedListener {
        void onItemChanged(CarouselImageView view, int position);
    }


}
