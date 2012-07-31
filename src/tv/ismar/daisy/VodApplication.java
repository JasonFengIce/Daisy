package tv.ismar.daisy;

import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.ContentModel;
import android.app.Application;

public class VodApplication extends Application {

	public static final String content_model_api = "/static/meta/content_model.json";
	public ContentModel[] mContentModel;
	
	@Override
	public void onCreate() {
		super.onCreate();
		getNewContentModel();
	}
	
	
	public void getNewContentModel(){
		
		new Thread(mGetNewContentModelTask).start();
	}
	
	private Runnable mGetNewContentModelTask = new Runnable() {
		
		@Override
		public void run() {
			SimpleRestClient restClient = new SimpleRestClient();
			mContentModel = restClient.getContentModelLIst(content_model_api).zh_CN;
		}
	};

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTrimMemory(int level) {
		// TODO Auto-generated method stub
		super.onTrimMemory(level);
	}

}
