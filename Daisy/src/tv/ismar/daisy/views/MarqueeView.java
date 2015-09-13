package tv.ismar.daisy.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import tv.ismar.daisy.R;
import tv.ismar.daisy.core.DaisyUtils;

public class MarqueeView extends LinearLayout {

	private static final String TAG = MarqueeView.class.getSimpleName();
	private static final int TEXTVIEW_VIRTUAL_WIDTH = 5000;

	private Context context;
	private TextView mTextField;
	private ScrollView mScrollView;

	private Paint mPaint;
	private Animation mMoveText = null;

	private float widthOfMarqueeView;
	private float heightOfMarqueeView;
	private String interval = "     ";
	private String stringOfItem = "";
	private String stringOfTextView = "";
	private String stringOfOrigin = "";
	private float widthOfItem = 0;
	private float widthOfString = 0;
	private float startXOfAnimation = 0;
	private float endXOfAnimation = 0;
	private Runnable mAnimationStartRunnable;
	private int mSpeed = 20;
	private Interpolator mInterpolator = new LinearInterpolator();
    private int textSize;
    private ColorStateList textColor;
	public MarqueeView(Context context) {
		super(context);
		init(context);
	}

	public MarqueeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		  TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MarqueeView);

	        if (a == null) {
	            return;
	        }
        
	        mSpeed = a.getInteger(R.styleable.MarqueeView_speed, 20);
	        //textColor = a.getInteger(R.styleable.MarqueeView_textcolor, -1);
	        float rate = DaisyUtils.getVodApplication(context).getRate(context);
	        textSize = (int) (a.getDimensionPixelSize(R.styleable.MarqueeView_textsize,textSize)/rate) ;
	        textColor = a.getColorStateList(R.styleable.MarqueeView_textcolor);	  
		    init(context);
		    initViews();
		    setTextFitSize();
		    a.recycle();
	}
    public String getText(){
    	return stringOfOrigin;
    }
	public MarqueeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
	}

	private Runnable startRunnable = new Runnable() {

		@Override
		public void run() {
			
			setText(stringOfOrigin);
			startMarquee();
		}
	};

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {

			widthOfMarqueeView = getWidth();
			heightOfMarqueeView = getHeight();
			float mTextWidth = mTextField.getPaint().measureText(stringOfOrigin);
			if(widthOfMarqueeView>=mTextWidth){
				mTextField.setText(stringOfOrigin);				
			}
			else{
				postDelayed(startRunnable, 0);
			}

		}
	}

	public void setText(String string) {
		stringOfOrigin = string;
		stringOfItem = string + interval;
		initViews();
		clearMarquee();
		dealChange();
		
		if(getWidth()>0){
			widthOfMarqueeView = getWidth();
			heightOfMarqueeView = getHeight();
			float mTextWidth = mTextField.getPaint().measureText(stringOfOrigin);
			if(widthOfMarqueeView>=mTextWidth){
				mTextField.setText(stringOfOrigin);				
			}
			else{
				startMarquee();
			}
		}
	}
	
	public void initViews() {
		clearMarquee();
		removeAllViews();
		mScrollView = new ScrollView(context);
		mTextField = new TextView(context);
		mPaint = mTextField.getPaint();

		mTextField.setSingleLine(true);
//		if(textColor==null)
//		   mTextField.setTextColor(Color.parseColor("#ffffffff"));
		mTextField.setTextColor(textColor != null ? textColor : ColorStateList.valueOf(0xFF000000));
		//mTextField.setTextColor(textColor);
//		mPaint.setFakeBoldText(true);
		mPaint.setAntiAlias(true);

		LayoutParams sv1lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		sv1lp.gravity = Gravity.CENTER;
		LayoutParams tv1lp = new LayoutParams(TEXTVIEW_VIRTUAL_WIDTH,
				LayoutParams.MATCH_PARENT);
		tv1lp.gravity = Gravity.CENTER;
		mTextField.setLayoutParams(sv1lp);
		mScrollView.addView(mTextField, tv1lp);
		addView(mScrollView, sv1lp);
	}

	public void clearMarquee() {
		stopMarquee();
	}

	public void stopMarquee() {
		if (mAnimationStartRunnable == null)
			return;
		removeCallbacks(mAnimationStartRunnable);
		mTextField.clearAnimation();
		invalidate();
	}

	public void startMarquee() {
		mAnimationStartRunnable = new Runnable() {
			public void run() {
				mTextField.startAnimation(mMoveText);
			}
		};
		postDelayed(mAnimationStartRunnable, 0);
		invalidate();
	}
	public void dealLayoutChange() {
		stringOfTextView = stringOfItem + stringOfItem;
		widthOfItem = mPaint.measureText(stringOfItem);
		widthOfString = mPaint.measureText(stringOfTextView);
		while (widthOfString <= 2 * widthOfMarqueeView) {
			stringOfTextView += stringOfItem;
			widthOfString = mPaint.measureText(stringOfTextView);
		}
		widthOfString = mPaint.measureText(stringOfTextView);
		expandTextView();
		mTextField.setText(stringOfTextView);
	}

	public void dealChange() {
		setTextFitSize();		
		dealLayoutChange();
		prepareAnimation();
	
	}
	private void expandTextView() {

		mTextField.layout(getLeft(), getTop(),
				(int) (getLeft() + widthOfString + 5), getTop() + getHeight());
	}

	public void setTextFitSize() {
		
		//mTextField.setTextSize(getFitTextSize(mPaint, getHeight()));
		mTextField.setTextSize(textSize);
	}

	public int getFitTextSize(Paint paint, int height) {

		// System.out.println("height: " + height);
		int minSize = 10;
		int maxSize = 200;
		int step = 1;

		// int heightOfText = height * 2 / 3;
		int heightOfText = height;
		while (minSize < maxSize) {

			paint.setTextSize(minSize);
			FontMetrics fm = paint.getFontMetrics();

			// //System.out.println("Math.ceil(fm.descent - fm.top): "
			// + Math.ceil(fm.descent - fm.top));
			if (Math.ceil(fm.descent - fm.top) >= heightOfText) {
				break;
			}
			minSize += step;
		}
		System.out.println("--- fit size: " + minSize + " ---");
		return minSize;
	}
	private void prepareAnimation() {

		startXOfAnimation = -(widthOfString - widthOfMarqueeView) % widthOfItem;
		endXOfAnimation = -widthOfString + widthOfMarqueeView;

		final int duration = ((int) Math.abs(startXOfAnimation
				- endXOfAnimation) * mSpeed);

		mMoveText = new TranslateAnimation(0, -widthOfItem,
				0, 0);
		mMoveText.setDuration(duration);
		mMoveText.setInterpolator(mInterpolator);
		mMoveText.setFillAfter(true);

		mMoveText.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				startMarquee();
			}

			public void onAnimationRepeat(Animation animation) {
			}
		});
	}

}
