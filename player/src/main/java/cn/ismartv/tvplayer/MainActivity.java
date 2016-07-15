package cn.ismartv.tvplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer.util.Util;

import java.io.UnsupportedEncodingException;

import cn.ismartv.activator.core.rsa.Coder;
import cn.ismartv.activator.core.rsa.SkyAESTool2;
import cn.ismartv.tvplayer.mvvm.model.ClipInfoEntity;
import cn.ismartv.tvplayer.mvvm.model.SkyService;
import cn.ismartv.tvplayer.mvvm.view.activity.PlayerActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String pk = intent.getStringExtra("pk");
        String baseUrl = intent.getStringExtra("base_url");
        String deviceToken = intent.getStringExtra("device_token");
        String sign = intent.getStringExtra("sign");


        invokeApiClip(baseUrl, pk, deviceToken, sign);

//        Intent mpdIntent = new Intent(this, PlayerActivity.class)
//                .setData(Uri.parse(sample.uri))
//                .putExtra(PlayerActivity.CONTENT_ID_EXTRA, sample.contentId)
//                .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, sample.type)
//                .putExtra(PlayerActivity.PROVIDER_EXTRA, sample.provider);
//        startActivity(mpdIntent);
    }


    private void invokeApiClip(String baseUrl, String pk, final String deviceToken, String sign) {
        SkyService.Factory.create(baseUrl).apiClip(pk, deviceToken, sign, "1")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ClipInfoEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ClipInfoEntity clipInfoEntity) {
                        handleClipInfo(clipInfoEntity, deviceToken);

                    }
                });
    }

    private void handleClipInfo(ClipInfoEntity clipInfoEntity, String deviceToken) {
        if (!TextUtils.isEmpty(clipInfoEntity.getIqiyi_4_0())) {
//            Intent intent = new Intent();
//            intent.setClass(this, SdkTestActivity.class);
//            intent.putExtra("clip_info", clipInfoEntity.getIqiyi_4_0());
//            startActivity(intent);
            Toast.makeText(this, "This is qiyi source!!!", Toast.LENGTH_SHORT).show();
        } else {
            String url = aesDecrypt(clipInfoEntity.getBestUrl(), deviceToken);
            Log.i(TAG, "url: " + url);
            Intent intent = new Intent(this, PlayerActivity.class)
                    .setData(Uri.parse(url))
                    .putExtra(PlayerActivity.CONTENT_ID_EXTRA, "test")
                    .putExtra(PlayerActivity.CONTENT_TYPE_EXTRA, Util.TYPE_HLS);
            startActivity(intent);


        }
    }

    private String aesDecrypt(String content, String key) {
        String result = "";
        byte[] base64;
        try {
            base64 = Coder.UrlSafeBase64_decode(content);
            result = SkyAESTool2.decrypt(key.substring(0, 16), base64);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
