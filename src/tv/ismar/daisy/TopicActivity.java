package tv.ismar.daisy;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.views.AlertDialogFragment;
import tv.ismar.daisy.views.AsyncImageView;
import tv.ismar.daisy.views.LoadingDialog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by zhangjiqiang on 15-6-3.
 */
public class TopicActivity extends BaseActivity implements View.OnFocusChangeListener,View.OnClickListener {

    private LoadingDialog mLoadingDialog;
    private RelativeLayout layout;
    private Bitmap bitmap;
    private BitmapFactory.Options mOptions;
    private SimpleRestClient mSimpleRestClient;
    private AsyncImageView[] mImgs;
    private ArrayList<Quadrangle> mQuadlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic);
        mLoadingDialog = new LoadingDialog(this, getResources().getString(R.string.loading));
        mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
        mSimpleRestClient = new SimpleRestClient();
        mQuadlist = new ArrayList<Quadrangle>();
        initView();
        initTemplate();
    }
    private void initView(){
        setInDensity();
        layout = (RelativeLayout)findViewById(R.id.topic_layout);
    }
    private void initHover(){
        layout.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_HOVER_ENTER:
                        break;
                    case MotionEvent.ACTION_HOVER_MOVE:
                        POINT p = new POINT(motionEvent.getRawX(),motionEvent.getRawY());
                        int count = mImgs.length;
                        for(int i=0;i<count;i++){
                            boolean isIN = PtInAnyRect(p,i);
                            if(isIN){
                                mImgs[i].setUrl(mImgs[i].getUrl());
                            }
                            else{
                                mImgs[i].setImageResource(android.R.color.transparent);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        break;
                }
                return false;
            }
        });
    }
    private void initTemplate(){
       String url = getIntent().getStringExtra("url");
//       String url = "http://cord.tvxio.com/v2_0/A21/dto/api/topic/8/";

        mSimpleRestClient.doTopicRequest(url, "get", "", new SimpleRestClient.HttpPostRequestInterface(){

            @Override
            public void onPrepare() {
                mLoadingDialog.show();
            }

            @Override
            public void onSuccess(String info) {
                JSONObject obj = null;
                try {
                    obj = new JSONObject(info);
                    if(obj.has("bg_image")){
                        String bgurl = obj.getString("bg_image");
                        new InitTemplateBgTask().execute(bgurl);
                    }
                    if(obj.has("albums")){
                        JSONArray array = obj.getJSONArray("albums");
                        int count = array.length();
                        if(count>0){
                            mImgs = new AsyncImageView[count];
                            for(int i=0;i<count;i++){
                                mImgs[i] = new AsyncImageView(TopicActivity.this);
                                mImgs[i].setOnFocusChangeListener(TopicActivity.this);
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                                JSONObject jsonObj = array.getJSONObject(i);
                                JSONObject directObj = jsonObj.getJSONObject("direction");
                                mImgs[i].setImageUrl(jsonObj.getString("image"));
                                //mImgs[i].setUrl(jsonObj.getString("image"));
                                mImgs[i].setImageResource(android.R.color.transparent);
                                mImgs[i].setFocusable(true);
                                mImgs[i].setTopicEnable(true);
                                mImgs[i].setTag(""+i);
                                mImgs[i].setId(jsonObj.getInt("order"));

                                JSONObject attributesJson = jsonObj.getJSONObject("attributes");
                                mImgs[i].setItemUrl(attributesJson.getString("url"));
                                mImgs[i].setIsComplex(attributesJson.getBoolean("is_complex"));
                                mImgs[i].setOnClickListener(TopicActivity.this);


                                int down = directObj.getInt("down");
                                int right = directObj.getInt("right");
                                int up = directObj.getInt("up");
                                int left = directObj.getInt("left");
                                if(down>0){
                                    mImgs[i].setNextFocusDownId(down);
                                }
                                if(right>0){
                                    mImgs[i].setNextFocusRightId(right);
                                }
                                if(up>0){
                                   mImgs[i].setNextFocusUpId(up);
                                }
                                if(left>0){
                                    mImgs[i].setNextFocusLeftId(left);
                                }
                                int position_x = jsonObj.getInt("position_x");
                                int position_y = jsonObj.getInt("position_y");

                                JSONArray polygonArray = jsonObj.getJSONArray("polygon");


                                JSONObject polygonLT = polygonArray.getJSONObject(0);
                                JSONObject polygonRT = polygonArray.getJSONObject(1);
                                JSONObject polygonRB = polygonArray.getJSONObject(2);
                                JSONObject polygonLB = polygonArray.getJSONObject(3);
                                POINT PLT,PRT,PRB,PLB;
                                PLT = new POINT(polygonLT.getInt("x"),polygonLT.getInt("y"));
                                PRT = new POINT(polygonRT.getInt("x"),polygonRT.getInt("y"));
                                PRB = new POINT(polygonRB.getInt("x"),polygonRB.getInt("y"));
                                PLB = new POINT(polygonLB.getInt("x"),polygonLB.getInt("y"));
                                Quadrangle quad;
                                quad = new Quadrangle(PLT,PRT,PRB,PLB,i);

                                mQuadlist.add(i,quad);
                                Log.i("Quadrangle","add view i="+i);
                                params.leftMargin = position_x;
                                params.topMargin = position_y;
                                layout.addView(mImgs[i],params);
                            }
                            initHover();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailed(String error) {
                showDialog();
            }
        });

    }
    public void showDialog() {
        AlertDialogFragment newFragment = AlertDialogFragment.newInstance(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG);
        newFragment.setPositiveListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                initTemplate();
                dialog.dismiss();
            }
        });
        newFragment.setNegativeListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopicActivity.this.finish();
                dialog.dismiss();
            }
        });
        FragmentManager manager = getFragmentManager();
        if(manager!=null) {
            newFragment.show(manager, "dialog");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasfocus) {
        if (hasfocus) {
            ((AsyncImageView) view).setUrl(((AsyncImageView) view).getUrl());
        } else {
            ((AsyncImageView) view).setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public void onClick(View view) {
        boolean iscomplex = ((AsyncImageView)view).getIscomplex();
        String url = ((AsyncImageView)view).getItemUrl();

        if(iscomplex){
            Intent intent = new Intent();
            intent.setAction("tv.ismar.daisy.Item");
            intent.putExtra("url", url);
            intent.putExtra(EventProperty.SECTION, "topic");
            startActivity(intent);
        }
        else{
            InitPlayerTool tool = new InitPlayerTool(TopicActivity.this);
            tool.setonAsyncTaskListener(new InitPlayerTool.onAsyncTaskHandler() {

                @Override
                public void onPreExecute(Intent intent) {
                    // TODO Auto-generated method stub
                    intent.putExtra(EventProperty.SECTION, "topic");
                    mLoadingDialog.show();
                }

                @Override
                public void onPostExecute() {
                    // TODO Auto-generated method stub
                    mLoadingDialog.dismiss();
                }
            });
            tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
        }
    }

    class InitTemplateBgTask extends AsyncTask<Object, Void, Void>{

        @Override
        protected Void doInBackground(Object... params) {
            bitmap = getNetWorkBitmap((String) params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            //layout.setBackground(drawable);
            layout.setBackgroundDrawable(drawable);
            mImgs[0].requestFocus();
            mLoadingDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }
    public void setInDensity() {
        if (mOptions == null) {
            mOptions = new BitmapFactory.Options();
            mOptions.inDither = true;
            mOptions.inScaled = true;
            mOptions.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
        }

        mOptions.inDensity = mOptions.inTargetDensity;
    }
    public  Bitmap getNetWorkBitmap(String urlString) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(urlString);
            // 使用HttpURLConnection打开连接
            HttpURLConnection urlConn = (HttpURLConnection) imgUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.connect();
            // 将得到的数据转化成InputStream
            InputStream is = urlConn.getInputStream();
            // 将InputStream转换成Bitmap
            bitmap = BitmapFactory.decodeStream(is,null,mOptions);
            is.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            System.out.println("[getNetWorkBitmap->]MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[getNetWorkBitmap->]IOException");
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onDestroy() {
        DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
        super.onDestroy();
    }
    private DialogInterface.OnCancelListener mLoadingCancelListener = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            finish();
            dialog.dismiss();
        }
    };

    public class POINT{
        public POINT(double x,double y){
            this.x = x;
            this.y = y;
        }
        public double x;
        public double y;
    }

    public class Quadrangle{
        public Quadrangle(POINT PLT,POINT PRT,POINT PRB,POINT PLB,int index){
            pLT = PLT;
            pRT = PRT;
            pRB = PRB;
            pLB = PLB;
            this.index = index;
        }
        public  POINT pLT, pLB, pRB, pRT;
        public int index;
    }

    // 判断点是否在四边形内部
// 参数：
//      POINT pCur 指定的当前点
//      POINT pLT, POINT pLB, POINT pRB, POINT pRT, 四边形的四个点
    boolean PtInAnyRect(POINT pCur,int index)
    {
        //任意四边形有4个顶点
        Quadrangle quad = mQuadlist.get(index);
        int nCount = 4;
        POINT RectPoints[] = { quad.pLT, quad.pLB, quad.pRB, quad.pRT };
        int nCross = 0;
        for (int i = 0; i < nCount; i++)
        {
            //依次取相邻的两个点
            POINT pStart = RectPoints[i];
            POINT pEnd = RectPoints[(i + 1) % nCount];

            //相邻的两个点是平行于x轴的，肯定不相交，忽略
            if ( pStart.y == pEnd.y )
                continue;

            //交点在pStart,pEnd的延长线上，pCur肯定不会与pStart.pEnd相交，忽略
            if ( pCur.y < Math.min(pStart.y, pEnd.y) || pCur.y > Math.max(pStart.y, pEnd.y) )
                continue;

            //求当前点和x轴的平行线与pStart,pEnd直线的交点的x坐标
            double x = (double)(pCur.y - pStart.y) * (double)(pEnd.x - pStart.x) / (double)(pEnd.y - pStart.y) + pStart.x;

            //若x坐标大于当前点的坐标，则有交点
            if ( x > pCur.x )
                nCross++;
        }

        // 单边交点为偶数，点在多边形之外
        return (nCross % 2 == 1);
    }
}
