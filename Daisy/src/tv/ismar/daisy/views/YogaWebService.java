package tv.ismar.daisy.views;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.gson.Gson;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.service.InitService;
import tv.ismar.daisy.models.Expense;
import tv.ismar.daisy.models.History;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemCollection;
import tv.ismar.daisy.player.InitPlayerTool;

import java.util.*;

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
    private List<String> checkedChannels;
    private boolean firstIn;
    private SharedPreferences sharedPreferences;

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
        sharedPreferences = getSharedPreferences("DUAL_HOME",MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("first_in",true).commit();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        Log.i("yoga", "onstart");
        if (SimpleRestClient.device_token==null||"".equals(SimpleRestClient.device_token)){
            Log.i("yoga","激活服务");
            Intent init=new Intent();
            init.setClass(mContext, InitService.class);
            startService(init);
        }
        server.get("/", new HttpServerRequestCallback() {

            private List<String> checkedChannels;

            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                String type = request.getQuery().getString("type");
                Log.i("request", request + "heh" + type);
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (type.equals("channel")) {
                    String portraitflag = request.getQuery().getString("portraitflag");
                    String url = request.getQuery().getString("url");
                    String title = request.getQuery().getString("title");
                    String channel = request.getQuery().getString("channel");
                    checkedChannels = getCheckedChannels();
                    if(sharedPreferences.getBoolean("first_in",true)) {
                        if (checkedChannels.size() < 3) {
                            setPreferenceChannel(channel);
                        }else{
                            sharedPreferences.edit().putBoolean("first_in", false).commit();
                        }
                    }
                    Log.i("yoga", url + "-----" + title + "----" + channel);
                    intent.setAction("tv.ismar.daisy.Channel");
                    intent.putExtra("channel", channel);
                    intent.putExtra("url", url);
                    intent.putExtra("title", title);
                    intent.putExtra("portraitflag", portraitflag);
                    Log.i("yoga", "send Intent!");
                    startActivity(intent);
                    response.send("callback(\"" + type + "\")");
                    Log.i("yoga", "跳转channel" + channel);
                } else if (type.equals("detail")) {
                    String url = request.getQuery().getString("url");
                    String contentMode = request.getQuery().getString("content_model");
                    String expense = request.getQuery().getString("expense");
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
                        Log.i("yoga", "跳转详情" + contentMode);
                        response.send("callback(\"" + type + "\")");
                    } else if (expense.equals("false")) {
                        InitPlayerTool tool = new InitPlayerTool(mContext);
                        tool.fromPage = "homepage";
                        tool.initClipInfo(url, InitPlayerTool.FLAG_URL);
                        response.send("callback(\"" + type + "\")");
                    }
                } else if (type.equals("morehistories")) {

                    intent.putExtra("channel", "histories");
                    intent.setAction("tv.ismar.daisy.Channel");
                    startActivity(intent);
                    response.send("callback(\"" + type + "\")");
                } else if (type.equals("history")) {
                    Log.i("yoga", "history" + "___" + type + "--" + SimpleRestClient.access_token);
                    if ("".equals(SimpleRestClient.access_token)) {
                        mHistoryItemList = new ItemCollection(1, 0, "1", "1");
                        mGetHistoryTask = new GetHistoryTask(response);
                        mGetHistoryTask.execute();
                    } else {
                        Log.i("yoga", "islogin" + SimpleRestClient.access_token);
                        getHistoryByNet(response);
                    }
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("yoga","service  is destroy");
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

    public ArrayList<String> getCheckedChannels() {
        ArrayList<String> list = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        HashMap<String, Integer> map = new HashMap<>();
        map.put("chinese_film_favor", Settings.System.getInt(resolver, "chinese_film_favor", 0));
        map.put("overseas_film_favor", Settings.System.getInt(resolver, "overseas_film_favor", 0));
        map.put("variety_entertainment_favor", Settings.System.getInt(resolver, "variety_entertainment_favor", 0));
        map.put("music_favor", Settings.System.getInt(resolver, "music_favor", 0));
        map.put("game_favor", Settings.System.getInt(resolver, "game_favor", 0));
        map.put("sport_favor", Settings.System.getInt(resolver, "sport_favor", 0));
        map.put("live_documentary_favor", Settings.System.getInt(resolver, "live_documentary_favor", 0));
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            if (entry.getValue() == 1) {
                list.add((String) entry.getKey());
            }
        }
        return list;
    }

        public void setPreferenceChannel(String preferenceChannel) {
            String channel="";
            switch(preferenceChannel){
                case "chinesemovie":
                    channel="chinese_film_favor";
                    break;
                case "overseas":
                    channel="overseas_film_favor";
                    break;
                case "variety":
                    channel="variety_entertainment_favor";
                    break;
                case "music":
                    channel="music_favor";
                    break;
                case "game":
                    channel="game_favor";
                    break;
                case "sport":
                    channel="sport_favor";
                    break;
                case "documentary":
                    channel="live_documentary_favor";
                    break;

            }
            if(!"".equals(channel)){

                Settings.System.putInt(getContentResolver(),channel,1);
            }

        }
}
//if("chinesemovie".equals(preferenceChannel)){
//        channel="chinese_film_favor";
//        }else if("overseas".equals(preferenceChannel)){
//        channel="overseas_film_favor";
//        }else if("variety".equals(preferenceChannel)){
//        channel="variety_entertainment_favor";
//        }else if("music".equals(preferenceChannel)){
//        channel="music_favor";
//        }else if("game".equals(preferenceChannel)){
//        channel="game_favor";
//        }else if("sport".equals(preferenceChannel)){
//        channel="sport_favor";
//        }else if("documentary".equals(preferenceChannel)){
//        channel="live_documentary_favor";
//        }