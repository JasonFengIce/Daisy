package tv.ismar.daisy.ui.fragment;

import tv.ismar.daisy.R;
import tv.ismar.daisy.views.LabelImageView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by huaijie on 5/18/15.
 */
public class EntertainmentFragment extends Fragment {

	private ImageView vaiety_post;
	private ImageView vaiety_thumb1;
	private ImageView vaiety_thumb2;
	private ImageView vaiety_thumb3;
	private LabelImageView vaiety_card1_image;
	private TextView vaiety_card1_subtitle;
	private LabelImageView vaiety_channel1_image;
	private TextView vaiety_channel1_subtitle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View mView = inflater.inflate(R.layout.fragment_entertainment, null);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		vaiety_post = (ImageView) view.findViewById(R.id.vaiety_thumb1);
		vaiety_thumb1 = (ImageView) view.findViewById(R.id.vaiety_thumb1);
		vaiety_thumb2 = (ImageView) view.findViewById(R.id.vaiety_thumb2);
		vaiety_thumb3 = (ImageView) view.findViewById(R.id.vaiety_thumb3);
		vaiety_thumb1.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
				} else {
					v.setPadding(0, 22, 0, 0);
				}
			}
		});
		vaiety_thumb2.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
				} else {
					v.setPadding(0, 22, 0, 0);
				}
			}
		});
		vaiety_thumb3.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setPadding(0, 0, 0, 0);
				} else {
					v.setPadding(0, 22, 0, 0);
				}
			}
		});
		vaiety_card1_image = (LabelImageView) view
				.findViewById(R.id.vaiety_card1_image);
		vaiety_card1_subtitle = (TextView) view
				.findViewById(R.id.vaiety_card1_subtitle);
		vaiety_channel1_image = (LabelImageView) view
				.findViewById(R.id.vaiety_channel1_image);
		vaiety_channel1_subtitle = (TextView) view
				.findViewById(R.id.vaiety_channel1_subtitle);
		vaiety_card1_subtitle.setText("康熙来了");
		vaiety_card1_image.setFocustitle("小s力邀神秘嘉宾到场");
		vaiety_card1_image
				.setUrl("http://res.tvxio.com/media/upload/20140922/yudnandasndaszhehuxioasdos001_adlet.jpg");

		vaiety_channel1_subtitle.setText("星光大道");
		vaiety_channel1_image.setFocustitle("星光大道 大v");
		vaiety_channel1_image
				.setUrl("http://res.tvxio.com/media/upload/20140922/yudnandasndaszhehuxioasdos001_adlet.jpg");
	}
}
