package tv.ismar.daisy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by zhangjiqiang on 15-7-15.
 */
public class MonthSectionButton extends Button {
    private int mPosition;

    public void setPosition(int position){
        this.mPosition = position;
    }
    public int getPosition(){
        return mPosition;
    }
    public MonthSectionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MonthSectionButton(Context context) {
        super(context);
    }

    public MonthSectionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
