package tv.ismar.daisy.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.ismartv.api.t.AccessProxy;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.utils.Util;

public class InitPlayerTool {
    Context mContext;
    Intent intent;
    private SimpleRestClient simpleRestClient;
    public final static String FLAG_URL = "url";
    public final static String FLAG_ITEM = "item";
    private boolean mIsPreviewVideo = false;
    private int price = 0;
    public String slug;
    public String channel;
    public boolean isSubitemPreview = false;
    public String fromPage="";
    private ItemByUrlTask urltask;
    private Item seraItem;
	public InitPlayerTool(Context context){
		this.mContext = context;
		intent = new Intent();
		simpleRestClient = new SimpleRestClient();
	}

	public void initClipInfo(Object item,String flag) {
		simpleRestClient = new SimpleRestClient();
		urltask = new ItemByUrlTask();
		urltask.execute(item,flag);
	}
	public void initClipInfo(Object item,String flag,boolean isPreviewVideo,Item seraiItem) {
		simpleRestClient = new SimpleRestClient();
		this.mIsPreviewVideo = isPreviewVideo;
		urltask = new ItemByUrlTask();
		urltask.execute(item,flag);
		this.seraItem = seraiItem;
	}
    public void initClipInfo(Object item,String flag,int price ) {
        simpleRestClient = new SimpleRestClient();
        this.price = price;
		urltask = new ItemByUrlTask();
		urltask.execute(item,flag);
    }
	// 初始化播放地址url
	private class ItemByUrlTask extends AsyncTask<Object, Void, String> {

		@Override
		protected void onPostExecute(String result) {
            if(Util.checkNetState(mContext) < 0){
                Toast.makeText(mContext, "网络无连接！", Toast.LENGTH_SHORT).show();
                return;
            }
			if(result.equals("iqiyi")){
				intent.setAction("tv.ismar.daisy.qiyiPlay");
				String info = AccessProxy.getvVideoClipInfo();
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("iqiyi", info);
			}
			else{
				String ismartv = AccessProxy.getvVideoClipInfo();
				intent.setAction("tv.ismar.daisy.Play");
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("ismartv", ismartv);
			}
			if(!"".equals(result)){
				if("lockscreen".equals(fromPage)){
					((Activity)mContext).startActivityForResult(intent, 1010);
				}else if(!mIsPreviewVideo||"dualhome".equals(fromPage)) {
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}else{
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			       ((Activity)mContext).startActivityForResult(intent, 20);}
			      ((Activity)mContext).startActivityForResult(intent, 20);
				}
			if(mListener!=null)
				mListener.onPostExecute();	
		}
		@Override
		protected String doInBackground(Object... params) {

			String sn = VodUserAgent.getMACAddress();
            AccessProxy.init(VodUserAgent.getModelName(),
                    ""+SimpleRestClient.appVersion, SimpleRestClient.sn_token);
            String flag = (String) params[1];
            Item item = null;
            if(flag.equals("url")){
            	try {
					item = simpleRestClient.getItem((String) params[0]);
					if(item.expense != null && item.preview != null){
						mIsPreviewVideo = true;
						item.isPreview = true;
					}

				} catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ItemOfflineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NetworkException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else{
            	item = (Item) params[0];
            }
            if(seraItem != null){
            	intent.putExtra("seraItem", seraItem);
            }
            String info = "";
            if(item!=null){
            	Clip clip;
            	if(!mIsPreviewVideo)
            	    clip = item.clip;
            	else{
                    if(isSubitemPreview){
                        clip = item.clip;
                    }
                    else{
						if(item.preview != null)
                        clip = item.preview;
						else
						clip = item.clip;
                    }
            		item.isPreview = true;
            	}
            	if(item.clip != null&&clip!=null){
                    item.channel = channel;
                    item.slug = slug;
                    if(fromPage!=null&&!fromPage.equals(""))
                        item.fromPage = fromPage;
                	intent.putExtra("item", item);
    				info = AccessProxy.getVideoInfo(SimpleRestClient.root_url
    						+ "/api/clip/" + clip.pk + "/",
    						VodUserAgent.getAccessToken(sn));
            	}
            }
			return info;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			//mLoadingDialog.show();
			if(mListener!=null)
			   mListener.onPreExecute(intent);
		}
	}
	public interface onAsyncTaskHandler{
		public void onPostExecute();
		public void onPreExecute(Intent intent);
	}
	private onAsyncTaskHandler mListener;
	
	public void removeAsycCallback(){
		if(urltask != null && urltask.getStatus()!=AsyncTask.Status.FINISHED && !urltask.isCancelled())
		urltask.cancel(true);
		urltask = null;
	}

	public void setonAsyncTaskListener(onAsyncTaskHandler l){
		this.mListener = l;
	}
}
