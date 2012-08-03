package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.List;

import tv.ismar.daisy.adapter.ImageCacheAdapter;
import tv.ismar.daisy.core.ConnectionHelper;
import tv.ismar.daisy.core.SearchMovieService;
import tv.ismar.daisy.core.SearchPromptDialog;
import tv.ismar.daisy.core.SortMovieUtils;
import tv.ismar.daisy.models.MovieBean;
import tv.ismar.daisy.views.LoadingDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
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

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.search_main)
public class SearchActivity extends Activity implements OnFocusChangeListener {
	// 缓存适配器
	ImageCacheAdapter imageAdapter;
	// 返回的搜索结果list
	MovieBean movie = new MovieBean();
	List<MovieBean> movieList = null;
	// 搜索提示Adapter
	ArrayAdapter<String> autoCompleAdapter;
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
	// 搜索推荐项
	private final int SEARCH_AUTOCOMPLE = 5;
	// 搜索推荐词2秒一更新，有效减少与服务平凡交互
	long startTime;
	// 服务层单例
	SearchMovieService searchService = SearchMovieService.getInstance();
	// 分组后的List
	List<List<MovieBean>> groupList = new ArrayList<List<MovieBean>>();
	// 自定义的Dialog
	private LoadingDialog loadDialog;

	@Override
	protected void onResume() {
		super.onResume();
		if (!ConnectionHelper.isNetWorkAvailable(this)) {
			new SearchPromptDialog(SearchActivity.this, R.style.MyDialog).show();
			// 初始化一个自定义的Dialog
			return;
		}
		someBackgroundWork(null, ADD_VIEW);
	}

	// =====================ViewById======================
	// 搜索按钮
	@ViewById(R.id.ibtn_search)
	ImageButton ibtnSearch;
	// 搜索结果数
	@ViewById(R.id.tv_search_count)
	TextView tvSearchCount;
	// 动态生成热门搜索Button所在的linearLayout
	@ViewById(R.id.liner_search_result)
	LinearLayout linearSearch;
	// 动态生成热门搜索Button所在的linearLayout
	@ViewById(R.id.linear_hot_words)
	LinearLayout linearAdd;
	// 搜索个数拼接
	@ViewById(R.id.tv_search_front)
	TextView tvSearchFront;
	// 搜索个数拼接
	@ViewById(R.id.tv_search_after)
	TextView tvSearchAfter;
	// 自动提示
	@ViewById(R.id.act_autocomplete_country)
	AutoCompleteTextView autoCompleteTextView;// 找到相应的控件
	// GridView
	@ViewById(R.id.gridview)
	GridView gridView;

	// =====================ViewById======================

	// =====================@AfterViews======================
	@AfterViews
	void initAfterViews() {
		listHotWords = null;
		movieList = new ArrayList<MovieBean>();
		loadDialog = new LoadingDialog(this);
		startTime = System.currentTimeMillis();
		// autoCompleteTextView.onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent);
		autoCompleteTextView.setOnFocusChangeListener(this);
		autoCompleteTextView.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		autoCompleteTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				long endTime = System.currentTimeMillis();
				if (endTime - startTime < 2000) {
					return;
				} else {
					startTime = endTime;
				}
				// 添加自动提示词组
				if (null != listHelp) {
					someBackgroundWork(autoCompleteTextView.getText().toString(), UPDATE_SUGGEST);
					doInUiThread(null, null, UPDATE_SUGGEST);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		autoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				loadDialogShow();
				someBackgroundWork(autoCompleteTextView.getText().toString(), SEARCH_AUTOCOMPLE);
			}
		});

		// autoCompleteTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// autoCompleteTextView.showDropDown();
		// }
		// });

	}

	// =====================@AfterViews======================
	// =====================@Click======================
	@Click
	void ibtn_search() {
		if (!ConnectionHelper.isNetWorkAvailable(this)) {
			new SearchPromptDialog(SearchActivity.this, R.style.MyDialog).show();
			// 初始化一个自定义的Dialog
			return;
		}
		if (TextUtils.isEmpty(autoCompleteTextView.getText().toString().trim())) {
			return;
		}
		loadDialogShow();
		someBackgroundWork(autoCompleteTextView.getText().toString(), UPDATE_ADAPTER);
	}

	@Click
	void act_autocomplete_country() {
		InputMethodManager m = (InputMethodManager) autoCompleteTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		autoCompleteTextView.showDropDown();
	}

	// =====================@Click======================
	// =====================@OnItem======================
	// =====================@OnItem======================
	// =====================@Background======================
	@Background
	void someBackgroundWork(String btnText, int anotherParam) {
		switch (anotherParam) {
		case ADD_VIEW:
			if (null == listHotWords) {
				listHotWords = searchService.getHotWords();
				doInUiThread(null, listHotWords, ADD_VIEW);
			}
			break;
		case SEARCH_WORDS:
			movieList = searchService.getSearchResult(btnText);
			doInUiThread(movieList, null, SEARCH_WORDS);
			break;
		case UPDATE_ADAPTER:
			movieList = searchService.getSearchResult(btnText);
			doInUiThread(movieList, null, UPDATE_ADAPTER);
			break;
		case UPDATE_SUGGEST:
			listHelp = searchService.getSearchHelper(autoCompleteTextView.getText().toString());
			doInUiThread(null, listHelp, UPDATE_SUGGEST);
			break;
		case SEARCH_AUTOCOMPLE:
			movieList = searchService.getSearchResult(btnText);
			doInUiThread(movieList, null, UPDATE_ADAPTER);
			break;
		default:
			break;
		}
	}

	// =====================@Background======================
	// =====================@UiThread======================
	@UiThread
	void doInUiThread(List<MovieBean> movieList, List<String> listHotWords, int anotherParam) {
		switch (anotherParam) {
		case ADD_VIEW:
			if (null != listHotWords) {
				for (int j = 0; j < listHotWords.size(); j++) {
					final Button btnHotWords = new Button(SearchActivity.this);
					btnHotWords.setId(j);
					btnHotWords.setTextSize(30);
					btnHotWords.setPadding(15, 0, 15, 0);
					btnHotWords.setBackgroundResource(R.drawable.hotwords_selector);
					btnHotWords.setTextColor(getResources().getColor(R.color.search_words));
					btnHotWords.setText(String.valueOf(listHotWords.get(j)));
					linearAdd.addView(btnHotWords);
					btnHotWords.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							loadDialogShow();
							someBackgroundWork(btnHotWords.getText().toString(), SEARCH_WORDS);
						}
					});
				}
			}
			break;
		case SEARCH_WORDS:
			loadDialogShow();
			linearSearch.setVisibility(View.VISIBLE);
			if (null == movieList) {
				setSearchResult(0);
				return;
			}
			setSearchResult(movieList.size());
			movieList = SortMovieUtils.sort(movieList);
			imageAdapter = new ImageCacheAdapter(SearchActivity.this, movieList, R.layout.search_grid_view_item);
			gridView.setAdapter(imageAdapter);
			break;
		case UPDATE_ADAPTER:
			loadDialogShow();
			linearSearch.setVisibility(View.VISIBLE);
			if (null == movieList)
				return;
			setSearchResult(movieList.size());
			movieList = SortMovieUtils.sort(movieList);
			imageAdapter = new ImageCacheAdapter(SearchActivity.this, movieList, R.layout.search_grid_view_item);
			gridView.setAdapter(imageAdapter);
			break;
		case UPDATE_SUGGEST:
			autoCompleAdapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.autocomplete_list_item, listHelp);// 配置Adaptor
			autoCompleteTextView.setAdapter(autoCompleAdapter);
			autoCompleteTextView.showDropDown();
			break;
		default:
			break;
		}
	}

	// =====================@UiThread======================
	// =====================@ItemClick======================
	/**
	 * 点击Gridview Item
	 */
	@ItemClick
	void gridview(int position) {
		movie = movieList.get(position);
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		if (movie.is_complex) {
			bundle.putInt("itemPK", movie.pk);
			intent.setAction("tv.ismar.daisy.ItemDetail");
			intent.putExtras(bundle);
		} else {
			bundle.putInt("itemPK", movie.pk);
			bundle.putInt("subItemPK", movie.item_pk);
			intent.setAction("tv.ismar.daisy.Play");
			intent.putExtras(bundle);
		}
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// =====================@ItemClick======================
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

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	//
	// finish();
	// }
	// return false;
	// }

	/**
	 * 显示自定义Dialog
	 */
	private void loadDialogShow() {
		if (loadDialog.isShowing()) {
			loadDialog.dismiss();
		} else {
			loadDialog.show();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			autoCompleteTextView.showDropDown();
		}
	}
}
