package tv.ismar.daisy.ui.listener;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import tv.ismar.daisy.player.InitPlayerTool;

import java.util.HashMap;

/**
 * Created by huaijie on 8/25/15.
 */
public class ItemDetailClickListener implements View.OnClickListener {
    public static final String MODEL = "model";
    public static final String URL = "url";
    public static final String TITLE = "title";

    private Context mContext;

    public ItemDetailClickListener(Context context) {
        mContext = context;
    }


    @Override
    public void onClick(View view) {
        HashMap<String, String> hashMap = (HashMap<String, String>) view.getTag();
        String model = hashMap.get("model");
        String url = hashMap.get("url");
        String title = hashMap.get("title");

        Intent intent = new Intent();
        if ("item".equals(model)) {
            intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.ItemDetailActivity");
            intent.putExtra("url", url);
            mContext.startActivity(intent);
        } else if ("topic".equals(model)) {
            intent.putExtra("url", url);
            intent.setClassName("tv.ismar.daisy",
                    "tv.ismar.daisy.TopicActivity");
            mContext.startActivity(intent);
        } else if ("section".equals(model)) {
            intent.putExtra("title", title);
            intent.putExtra("itemlistUrl", url);
            intent.putExtra("lableString", title);
            intent.setClassName("tv.ismar.daisy", "tv.ismar.daisy.PackageListDetailActivity");
            mContext.startActivity(intent);
        } else if ("package".equals(model)) {
            intent.setAction("tv.ismar.daisy.packageitem");
            intent.putExtra("url", url);
            mContext.startActivity(intent);
        } else if ("clip".equals(model)) {
            InitPlayerTool tool = new InitPlayerTool(mContext);
            tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
        }
    }
}
