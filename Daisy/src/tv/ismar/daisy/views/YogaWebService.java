package tv.ismar.daisy.views;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sakuratya.horizontal.adapter.HGridAdapterImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.player.InitPlayerTool;

/**
 * Created by liucan on 2016/6/15.
 */
public class YogaWebService extends Service {
    Context mContext=this;
    AsyncHttpServer server;
    GetHistoryTask mGetHistoryTask;
    private Item[] mHistoriesByNet;
    private SimpleRestClient mRestClient;
    private boolean isInGetHistoryTask;
    private ItemCollection mHistoryItemList;
    private ArrayList<ItemCollection> mItemCollections;
    ArrayList<Item> items=new ArrayList<>();
    ArrayList<History> mHistories;
    String result;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
       server = new AsyncHttpServer();
        server.listen(5000);
        mRestClient = new SimpleRestClient();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("yoga","onstart");
        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                String type = request.getQuery().getString("type");
                Log.i("request", request + "heh" + type);
                if (type.equals("channel")) {
                    String portraitflag = request.getQuery().getString("portraitflag");
                    String url = request.getQuery().getString("url");
                    String title = request.getQuery().getString("title");
                    String channel = request.getQuery().getString("channel");
                    Log.i("yoga", url + "-----" + title + "----" + channel);
                    Intent intent = new Intent();
                    intent.setAction("tv.ismar.daisy.Channel");
                    intent.putExtra("channel", channel);
                    intent.putExtra("url", url);
                    intent.putExtra("title", title);
                    intent.putExtra("portraitflag", portraitflag);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (type.equals("detail")) {
                    String url = request.getQuery().getString("url");
                    String contentMode = request.getQuery().getString("content_model");
                    String expense = request.getQuery().getString("expense");
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (expense.equals("true")) {
                        if (("variety".equals(contentMode) || "entertainment".equals(contentMode))) {
                            intent.setAction("tv.ismar.daisy.EntertainmentItem");
                            intent.putExtra("title", "娱乐综艺");
                        } else if ("movie".equals(contentMode)) {
                            intent.setAction("tv.ismar.daisy.PFileItem");
                            intent.putExtra("title", "电影");
                        } else if ("package".equals(contentMode)) {
                            intent.setAction("tv.ismar.daisy.packageitem");
                            intent.putExtra("title", "礼包详情");
                        } else {
                            intent.setClassName("tv.ismar.daisy",
                                    "tv.ismar.daisy.ItemDetailActivity");
                        }
                        intent.putExtra("url", url);
                        intent.putExtra("fromPage", "homepage");
                        startActivity(intent);
                    } else if (expense.equals("false")) {
                        InitPlayerTool tool = new InitPlayerTool(mContext);
                        tool.fromPage = "homepage";
                        tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
                    }
                } else if (type.equals("morehistories")) {
                    Intent intent = new Intent();
                    intent.putExtra("channel", "histories");
                    intent.setAction("tv.ismar.daisy.Channel");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (type.equals("history")) {
                    Log.i("yoga","history"+"___"+type+"--"+SimpleRestClient.access_token);
                    if("".equals(SimpleRestClient.access_token)){
                        mHistoryItemList=new ItemCollection(1,0,"1","1");
                        mGetHistoryTask = new GetHistoryTask(response);
                        mGetHistoryTask.execute();
                    }else{
                        Log.i("yoga","islogin"+SimpleRestClient.access_token);
                        getHistoryByNet(response);
                    }
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    class GetHistoryTask extends AsyncTask<Void, Void, Void> {
        private AsyncHttpServerResponse response;
        public GetHistoryTask(AsyncHttpServerResponse resp){
            response=resp;
        }
        @Override
        protected void onPreExecute() {
            isInGetHistoryTask = true;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mHistories = DaisyUtils.getHistoryManager(mContext).getAllHistories("no");
                Log.i("yoga",mHistories.size()+"");
                if(mHistories.size()>0) {
                    Collections.sort(mHistories);
                    for(int i=0;i<mHistories.size();++i) {
                        History history = mHistories.get(i);
                        Item item = getItem(history);
                        items.add(item);
                        mHistoryItemList.objects.put(mHistoryItemList.count++, item);
                    }
                    mHistoryItemList.num_pages = (int) Math.ceil((float)mHistoryItemList.count / (float) ItemCollection.NUM_PER_PAGE);
                    if(mHistoryItemList.count>0){
                        Arrays.fill(mHistoryItemList.hasFilledValidItem, true);
                    }
                    Log.i("yoga","mHistoryItemList"+mHistoryItemList.count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mHistoryItemList!=null&&mHistoryItemList.count>0) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("access_token", SimpleRestClient.access_token);
                    obj.put("device_token", SimpleRestClient.device_token);
                    JSONArray array = new JSONArray();
                    Gson gson=new Gson();
                    for(int i=0;i<mHistories.size();i++){
                        String js=gson.toJson(mHistoryItemList.objects.get(i));
                        array.put(js);
                    }
                    obj.put("historyList", array.toString());
                    response.send("callback("+obj+")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {

            }

            isInGetHistoryTask = false;
        }

    }

    private Item getItem(History history) {
        Item item = new Item();
        item.adlet_url = history.adlet_url;
        item.is_complex = history.is_complex;
        item.url = history.url;
        item.content_model = history.content_model;
        item.quality = history.quality;
        item.title = history.title;
//		if(history.price==0){
//			item.expense = null;
//		}
//		else{
        item.expense = new Expense();
        if(history.price!=0)
            item.expense.price = history.price;
        if(history.cpid!=0)
            item.expense.cpid=history.cpid;
        if(history.cpname!=null)
            item.expense.cpname=history.cpname;
        if(history.cptitle!=null)
            item.expense.cptitle=history.cptitle;
        if(history.paytype!=-1)
            item.expense.pay_type=history.paytype;
//		}
        return item;
    }
    private void getHistoryByNet(final AsyncHttpServerResponse resp){
        Log.i("begin", System.currentTimeMillis()+"");
        mRestClient.doSendRequest("/api/histories/", "get", "", new SimpleRestClient.HttpPostRequestInterface() {
            @Override
            public void onPrepare() {

            }
            @Override
            public void onSuccess(String info) {
                // TODO Auto-generated method stub
                Log.i("info", info);
                JSONObject obj=new JSONObject();
                try {
                    obj.put("access_token", SimpleRestClient.access_token);
                    obj.put("device_token", SimpleRestClient.device_token);
                    obj.put("historyList", info);
                    resp.send("callback("+obj+")");
                    Log.i("end", System.currentTimeMillis()+"");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailed(String error) {
                Log.i("yoga",error);
            }
        });
    }

}
