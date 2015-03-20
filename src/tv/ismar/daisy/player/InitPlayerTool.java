package tv.ismar.daisy.player;

import com.google.gson.JsonSyntaxException;
import com.ismartv.api.t.AccessProxy;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.core.VodUserAgent;
import tv.ismar.daisy.exception.ItemOfflineException;
import tv.ismar.daisy.exception.NetworkException;
import tv.ismar.daisy.models.Clip;
import tv.ismar.daisy.models.Item;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class InitPlayerTool {
    Context mContext;
    Intent intent;
    private SimpleRestClient simpleRestClient;
    public final static String FLAG_URL = "url";
    public final static String FLAG_ITEM = "item";
	public InitPlayerTool(Context context){
		this.mContext = context;
		intent = new Intent();
		simpleRestClient = new SimpleRestClient();
	}
	
	public void initClipInfo(Object item,String flag) {
		simpleRestClient = new SimpleRestClient();
		new ItemByUrlTask().execute(item,flag);
	}

	// 初始化播放地址url
	private class ItemByUrlTask extends AsyncTask<Object, Void, String> {

		@Override
		protected void onPostExecute(String result) {		
			if(result.equals("iqiyi")){
				intent.setAction("tv.ismar.daisy.qiyiPlay");
				String info = AccessProxy.getvVideoClipInfo();
				intent.putExtra("iqiyi", info);		
			}
			else{
				String ismartv = AccessProxy.getvVideoClipInfo();
				intent.setAction("tv.ismar.daisy.Play");
				intent.putExtra("ismartv", ismartv);
			}
			if(!"".equals(result))
				mContext.startActivity(intent);
			if(mListener!=null)
				mListener.onPostExecute();	
		}
		@Override
		protected String doInBackground(Object... params) {

			String sn = VodUserAgent.getMACAddress();
			AccessProxy.init(VodUserAgent.deviceType,
					VodUserAgent.deviceVersion, sn);
            String flag = (String) params[1];
            Item item = null;;
            if(flag.equals("url")){
            	try {
					item = simpleRestClient.getItem((String) params[0]);
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
            String info = "";
            if(item!=null){
            	Clip clip = item.clip;
            	if(item.clip != null){
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
	
	public void setonAsyncTaskListener(onAsyncTaskHandler l){
		this.mListener = l;
	}
}