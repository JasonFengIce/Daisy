package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.adapter.ImageCacheAdapter;
import tv.ismar.daisy.core.ConnectionHelper;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SearchMovieService;
import tv.ismar.daisy.core.SearchPromptDialog;
import tv.ismar.daisy.core.SortMovieUtils;
import tv.ismar.daisy.models.MovieBean;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnClickListener, OnItemClickListener {

	// 搜索
	ImageButton ibtnSearch;
	// 缓存适配器android:minSdkVersion
	ImageCacheAdapter imageAdapter;
	// CacheAdapter cacheAdapter;
	// ViewHolderAdapter holderAdapter;
	GridView gridView;
	// 搜索结果数
	TextView tvSearchCount;
	// 搜索结果linear
	LinearLayout linearSearch;
	// 返回的搜索结果list
	MovieBean movie = new MovieBean();
	List<MovieBean> movieList = null;
	// 搜索个数拼接
	TextView tvSearchFront;
	TextView tvSearchAfter;
	// 自动提示
	AutoCompleteTextView autoCompleteTextView;
	// 搜索提示Adapter
	ArrayAdapter<String> autoAdapter;
	// 动态生成热门搜索Button所在的linearLayout
	LinearLayout linearAdd;
	// 推荐词List
	List<String> listHelp = new ArrayList<String>();
	// 热门搜索List
	List<String> listHotWords = new ArrayList<String>();
	// 搜索返回json
	String searchResult = null;
	// 更新adapter数据
	private final int UPDATE_ADAPTER = 1;
	// 搜索提示消息
	private final int UPDATE_SUGGEST = 2;
	// 动态生成Button消息
	private final int ADD_VIEW = 3;
	// 获取热门搜索词
	private final int SEARCH_WORDS = 4;
	// 搜索推荐词2秒一更新，有效减少与服务平凡交互
	long startTime;
	// 服务层单例
	SearchMovieService searchService = SearchMovieService.getInstance();
	// 分组后的List
	List<List<MovieBean>> groupList = new ArrayList<List<MovieBean>>();
	//日志上报
	HashMap<String, Object> hashLog = new HashMap<String, Object>();
	
	// 自定义的Dialog
	private LoadingDialog loadDialog;
	private SearchPromptDialog customDialog;
	
	private Boolean isActivityExit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_main);
		movieList = new ArrayList<MovieBean>();
		initViews();
		DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!ConnectionHelper.isNetWorkAvailable(this)) {
			if (!customDialog.isShowing()) {
				customDialog.show();
			}
			// 初始化一个自定义的Dialog
			return;
		} else {
			showHotWords();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		isActivityExit = true;
		// imageAdapter.setAsyncisPauseed(true);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		mHandler.removeMessages(UPDATE_ADAPTER);
		mHandler.removeMessages(UPDATE_SUGGEST);
		mHandler.removeMessages(ADD_VIEW);
		mHandler.removeMessages(SEARCH_WORDS);
		DaisyUtils.getVodApplication(this).removeActivtyFromPool(this.toString());
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		imageAdapter.clearCache();
	}

	private void showHotWords() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (SearchActivity.this) {
					try {
						Log.i("listHotWords", listHotWords+"" );
						if (null == listHotWords || 0==listHotWords.size()) {
							Log.i("listHotWords", listHotWords+"Ture" );
							listHotWords = searchService.getHotWords();
							mHandler.sendEmptyMessage(ADD_VIEW);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}) {
		}.start();
	}

	public void initViews() {
		gridView = (GridView) findViewById(R.id.serarc_gridview);
		gridView.setNumColumns(6);
		gridView.setVerticalSpacing(5);
		gridView.setOnItemClickListener(SearchActivity.this);
		imageAdapter = new ImageCacheAdapter(SearchActivity.this, R.layout.search_grid_view_item);
		ibtnSearch = (ImageButton) findViewById(R.id.ibtn_search);
		ibtnSearch.setOnClickListener(this);
		tvSearchCount = (TextView) findViewById(R.id.tv_search_count);
		linearSearch = (LinearLayout) findViewById(R.id.liner_search_result);
		linearAdd = (LinearLayout) findViewById(R.id.linear_hot_words);
		tvSearchFront = (TextView) findViewById(R.id.tv_search_front);
		tvSearchAfter = (TextView) findViewById(R.id.tv_search_after);
		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.act_autocomplete_country);// 找到相应的控件
		autoCompleteTextView.setOnClickListener(this);
		loadDialog = new LoadingDialog(this);
		customDialog = new SearchPromptDialog(SearchActivity.this, R.style.MyDialog);
		startTime = System.currentTimeMillis();
		autoCompleteTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Log.i("onTextChanged", "start" + start + "  before  " + before + "  count  " + count);
				long endTime = System.currentTimeMillis();
				if ((count == 1 && before == 0) || count == 0 && before == 1) {
					if (endTime - startTime < 1000) {
						return;
					} else {
						startTime = endTime;
					}
				} else {
					return;
				}
				new Thread(new Runnable() {
					@Override
					public void run() {
						listHelp = searchService.getSearchHelper(autoCompleteTextView.getText().toString());
						// 添加自动提示词组
						if (null != listHelp) {
							if (!isActivityExit) {
								mHandler.sendEmptyMessage(UPDATE_SUGGEST);
							}
						}
					}
				}) {
				}.start();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_search:
			try {
				if (!ConnectionHelper.isNetWorkAvailable(this)) {
					if (!customDialog.isShowing()) {
						customDialog.show();
					}
					// 初始化一个自定义的Dialog
					return;
				}
				if (TextUtils.isEmpty(autoCompleteTextView.getText().toString().trim())) {
					return;
				} else if (checkInput(autoCompleteTextView.getText().toString())) {
					Toast.makeText(SearchActivity.this, getString(R.string.search_error_text), Toast.LENGTH_LONG).show();
					return;
				}
				loadDialogShow();
				new Thread(new Runnable() {
					@Override
					public void run() {
						hashLog.put("q",autoCompleteTextView.getText().toString());
						NetworkUtils.LogSender(NetworkUtils.VIDEO_SEARCH, hashLog);
						hashLog.clear();
						movieList = searchService.getSearchResult(autoCompleteTextView.getText().toString());
						mHandler.sendEmptyMessage(UPDATE_ADAPTER);
					}
				}) {
				}.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case R.id.act_autocomplete_country:
			// Toast.makeText(this, "is ok", Toast.LENGTH_LONG).show();
			InputMethodManager m = (InputMethodManager) autoCompleteTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			autoCompleteTextView.showDropDown();
			// autoCompleteTextView.onKeyDown(KeyEvent.KEYCODE_BACK, null);
			break;
		default:
			break;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_ADAPTER:
				loadDialogShow();
				linearSearch.setVisibility(View.VISIBLE);
				if (null == movieList)
					return;
				setSearchResult(movieList.size());
				// cacheAdapter = new CacheAdapter(SearchActivity.this, movieList);
				// gridView.setAdapter(cacheAdapter);
				movieList = SortMovieUtils.sort(movieList);
				setImageAdapter(movieList);
				break;
			case UPDATE_SUGGEST:
				autoAdapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.autocomplete_list_item, listHelp);// 配置Adaptor
				autoCompleteTextView.setAdapter(autoAdapter);
				autoCompleteTextView.showDropDown();
				// autoAdapter.notifyDataSetChanged();
				break;
			case ADD_VIEW:
				if (null == listHotWords)
					return;
				for (int j = 0; j < listHotWords.size(); j++) {
					final Button btnHotWords = new Button(SearchActivity.this);
					// final Button btnHotWords = (Button) findViewById(R.id.btn_words);
					int H = DaisyUtils.getVodApplication(SearchActivity.this).getheightPixels(SearchActivity.this);
					if(H==720){
						btnHotWords.setPadding(10, 0, 10, 0);
						btnHotWords.setTextSize(20);
					}
					else{
						btnHotWords.setPadding(15, 0, 15, 0);
						btnHotWords.setTextSize(30);
					}
					btnHotWords.setId(j);
					// btnHotWords.setBackgroundColor(R.drawable.gridview_text_selector);
					btnHotWords.setBackgroundResource(R.drawable.hotwords_selector);
					btnHotWords.setTextColor(getResources().getColor(R.color.search_words));
					// btnHotWords.setTypeface(Typeface.DEFAULT_BOLD,Typeface.NORMAL);
					btnHotWords.setText(String.valueOf(listHotWords.get(j)));
					linearAdd.addView(btnHotWords);
					btnHotWords.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							loadDialogShow();
							// 这里继续写执行查询代码
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										hashLog.put("q",btnHotWords.getText().toString());
										NetworkUtils.LogSender(NetworkUtils.VIDEO_SEARCH, hashLog);
										hashLog.clear();
										movieList = searchService.getSearchResult(btnHotWords.getText().toString());
										mHandler.obtainMessage(SEARCH_WORDS, btnHotWords.getText().toString()).sendToTarget();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}) {
							}.start();
						}
					});
				}
				break;
			case SEARCH_WORDS:
				loadDialogShow();
				autoCompleteTextView.setText((String) msg.obj);
				linearSearch.setVisibility(View.VISIBLE);
				if (null == movieList) {
					setSearchResult(0);
					return;
				}
				setSearchResult(movieList.size());
				movieList = SortMovieUtils.sort(movieList);
				setImageAdapter(movieList);
				break;
			default:
				break;
			}
		}

		/**
		 * This clear data and set adapter
		 * 
		 * @param movieList
		 */
		private void setImageAdapter(List<MovieBean> movieList) {
			imageAdapter.cancelAsync();
			imageAdapter = new ImageCacheAdapter(SearchActivity.this, movieList, R.layout.search_grid_view_item);
			gridView.setAdapter(imageAdapter);
		};
	};

	/**
	 * 设置搜索个数
	 * 
	 * @param resultCount
	 */
	public void setSearchResult(int resultCount) {
		if (0 == resultCount) {
			tvSearchFront.setText(getString(R.string.search_not_result));
			tvSearchCount.setText("");
			tvSearchAfter.setText("");
		} else {
			tvSearchFront.setText(getString(R.string.search_for));
			tvSearchCount.setText(String.valueOf(movieList.size()).toString());
			tvSearchAfter.setText(getString(R.string.search_result));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == 4) {
			finish();
		}
		return false;
	}

	private void loadDialogShow() {
		if (loadDialog.isShowing()) {
			loadDialog.dismiss();
		} else {
			loadDialog.show();
		}
	}

	/**
	 * 点击Gridview Item
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long positions) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		hashLog.put("content_type", movieList.get(position).content_model);
		hashLog.put("q", autoCompleteTextView.getText().toString());
		hashLog.put("item", movieList.get(position).item_pk);
		hashLog.put("title", movieList.get(position).title);
		new Thread(new Runnable() {
			@Override
			public void run() {
				NetworkUtils.LogSender(NetworkUtils.VIDEO_SEARCH_ARRIVE, hashLog);	
				hashLog.clear();
			}
		}){}.start();
		if (movieList.get(position).is_complex) {
			bundle.putString("url", movieList.get(position).url);
			intent.setAction("tv.ismar.daisy.Item");
			intent.putExtras(bundle);
		} else {
			intent.setAction("tv.ismar.daisy.Play");
			intent.putExtra("url", movieList.get(position).url);
		}
		try {
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean checkInput(String username) {
		String check = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？ ]+";
		Pattern regex = Pattern.compile(check.trim());
		Matcher matcher = regex.matcher(username);
		return matcher.matches();
	}
}
