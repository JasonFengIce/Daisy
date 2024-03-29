package tv.ismar.daisy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakuratya.horizontal.ui.HGridView;

import tv.ismar.daisy.adapter.SearchAdapter;
import tv.ismar.daisy.core.ConnectionHelper;
import tv.ismar.daisy.core.DaisyUtils;
import tv.ismar.daisy.core.EventProperty;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SearchMovieService;
import tv.ismar.daisy.core.SearchPromptDialog;
import tv.ismar.daisy.core.SortMovieUtils;
import tv.ismar.daisy.models.MovieBean;
import tv.ismar.daisy.player.InitPlayerTool;
import tv.ismar.daisy.player.InitPlayerTool.onAsyncTaskHandler;
import tv.ismar.daisy.ui.activity.TVGuideActivity;
import tv.ismar.daisy.ui.widget.dialog.MessageDialogFragment;
import tv.ismar.daisy.utils.BitmapDecoder;
import tv.ismar.daisy.views.LoadingDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SearchActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

    // 搜索
    Button ibtnSearch;
    // 缓存适配器android:minSdkVersion
    SearchAdapter imageAdapter;
    // CacheAdapter cacheAdapter;
    // ViewHolderAdapter holderAdapter;
    HGridView gridView;
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
    EditText autoCompleteTextView;
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
    private MessageDialogFragment customDialog;

    private Boolean isActivityExit = false;
    private Button arrow_right;
    private Button arrow_left;
    private ExecutorService threadservice;
    private Thread fetchhotlines;
    private Future hotlinefuture;
    private BitmapDecoder bitmapDecoder;
    private InitPlayerTool tool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_main);
        activityTag = "SearchActivity";
        final View background = findViewById(R.id.large_layout);
        customDialog = new MessageDialogFragment(SearchActivity.this, getString(R.string.fetch_net_data_error), null);
        customDialog.setButtonText(getString(R.string.setting_network), getString(R.string.i_know));
        bitmapDecoder = new BitmapDecoder();
        bitmapDecoder.decode(this, R.drawable.main_bg, new BitmapDecoder.Callback() {
            @Override
            public void onSuccess(BitmapDrawable bitmapDrawable) {
            	background.setBackgroundDrawable(bitmapDrawable);
            }
        });

        movieList = new ArrayList<MovieBean>();
        threadservice = Executors.newSingleThreadExecutor();
        initViews();
//		InputMethodManager m = (InputMethodManager) autoCompleteTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//		m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);	
        DaisyUtils.getVodApplication(this).addActivityToPool(this.toString(), this);
    }

    boolean neterrorshow = false;
    @Override
    protected void onResume() {
        super.onResume();
        if (!ConnectionHelper.isNetWorkAvailable(this)) {
            if (!customDialog.isShowing()) {
            	 try {
            		 customDialog.showAtLocation(gridView, Gravity.CENTER,
                             new MessageDialogFragment.ConfirmListener() {
                                 @Override
                                 public void confirmClick(View view) {
                                     Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                     SearchActivity.this.startActivity(intent);
                                 }
                             }, new MessageDialogFragment.CancelListener() {

                                 @Override
                                 public void cancelClick(View view) {
                                	 customDialog.dismiss();
                                     neterrorshow = false;
                                 }
                             });
                     neterrorshow = true;
                 } catch (android.view.WindowManager.BadTokenException e) {
                 }
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
        if (autoCompleteTextView != null) {
            InputMethodManager imm = (InputMethodManager) autoCompleteTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
        }
        // imageAdapter.setAsyncisPauseed(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
    	if(tool != null)
    		tool.removeAsycCallback();
        if(bitmapDecoder != null && bitmapDecoder.isAlive()){
        	bitmapDecoder.interrupt();
        }
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
        fetchhotlines = new Thread(new Runnable() {
            @Override
            public void run() {
//				synchronized (SearchActivity.this) {
                try {
                    Log.i("listHotWords", listHotWords + "");
                    if (null == listHotWords || 0 == listHotWords.size()) {
                        Log.i("listHotWords", listHotWords + "Ture");
                        listHotWords = searchService.getHotWords();
                        mHandler.sendEmptyMessage(ADD_VIEW);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//				}
            }
        }) {
        };
        hotlinefuture = threadservice.submit(fetchhotlines);
//		fetchhotlines.start();
    }

    //-partition-size 2048 -
    public void initViews() {
        View ff = (View) findViewById(R.id.serarc_gridview);
        gridView = (HGridView) ff.findViewById(R.id.serarc_gridview);
        //gridView.setNumColumns(6);
        //	gridView.setVerticalSpacing(5);
        gridView.setOnItemClickListener(SearchActivity.this);
        imageAdapter = new SearchAdapter(SearchActivity.this, R.layout.search_grid_view_item);
        ibtnSearch = (Button) findViewById(R.id.ibtn_search);
        ibtnSearch.setOnClickListener(this);
        tvSearchCount = (TextView) findViewById(R.id.tv_search_count);
        linearSearch = (LinearLayout) findViewById(R.id.liner_search_result);
        linearAdd = (LinearLayout) findViewById(R.id.linear_hot_words);
        tvSearchFront = (TextView) findViewById(R.id.tv_search_front);
        tvSearchAfter = (TextView) findViewById(R.id.tv_search_after);
        autoCompleteTextView = (EditText) findViewById(R.id.act_autocomplete_country);// 找到相应的控件

        autoCompleteTextView.setOnClickListener(this);
        loadDialog = new LoadingDialog(this);
        startTime = System.currentTimeMillis();
//		autoCompleteTextView.addTextChangedListener(new TextWatcher() {
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				Log.i("onTextChanged", "start" + start + "  before  " + before + "  count  " + count);
//				long endTime = System.currentTimeMillis();
//				if ((count == 1 && before == 0) || count == 0 && before == 1) {
//					if (endTime - startTime < 1000) {
//						return;
//					} else {
//						startTime = endTime;
//					}
//				} else {
//					return;
//				}
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						listHelp = searchService.getSearchHelper(autoCompleteTextView.getText().toString());
//						// 添加自动提示词组
//						if (null != listHelp) {
//							if (!isActivityExit) {
//								mHandler.sendEmptyMessage(UPDATE_SUGGEST);
//							}
//						}
//					}
//				}) {
//				}.start();
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//			}
//		});
        autoCompleteTextView
                .setOnEditorActionListener(new OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if ((actionId == EditorInfo.IME_ACTION_SEARCH)) {
                            if (!TextUtils.isEmpty(autoCompleteTextView
                                    .getEditableText().toString())) {
                                ibtnSearch.requestFocus();
//                                exeClick(v);
                            }
                        }
                        return false;
                    }
                });
        arrow_right = (Button) findViewById(R.id.arrow_right);
        arrow_left = (Button) findViewById(R.id.arrow_left);
        arrow_left.setOnClickListener(this);
        arrow_right.setOnClickListener(this);
        gridView.leftbtn = arrow_left;
        gridView.rightbtn = arrow_right;
    }

    private void exeClick(View v) {
        try {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (!ConnectionHelper.isNetWorkAvailable(this)) {
                if (!customDialog.isShowing()) {
                	 try {
                		 customDialog.showAtLocation(gridView, Gravity.CENTER,
                                 new MessageDialogFragment.ConfirmListener() {
                                     @Override
                                     public void confirmClick(View view) {
                                         Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                         SearchActivity.this.startActivity(intent);
                                     }
                                 }, new MessageDialogFragment.CancelListener() {

                                     @Override
                                     public void cancelClick(View view) {
                                    	 customDialog.dismiss();
                                         neterrorshow = false;
                                     }
                                 });
                         neterrorshow = true;
                     } catch (android.view.WindowManager.BadTokenException e) {
                     }
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
                    hashLog.put(EventProperty.Q, autoCompleteTextView.getText().toString());
                    NetworkUtils.SaveLogToLocal(NetworkUtils.VIDEO_SEARCH, hashLog);
                    hashLog.clear();
                    movieList = searchService.getSearchResult(autoCompleteTextView.getText().toString());
                    mHandler.sendEmptyMessage(UPDATE_ADAPTER);
                }
            }) {
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_search:
                try {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (!ConnectionHelper.isNetWorkAvailable(this)) {
                        if (!customDialog.isShowing()) {
                        	 try {
                        		 customDialog.showAtLocation(gridView, Gravity.CENTER,
                                         new MessageDialogFragment.ConfirmListener() {
                                             @Override
                                             public void confirmClick(View view) {
                                                 Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                                 SearchActivity.this.startActivity(intent);
                                             }
                                         }, new MessageDialogFragment.CancelListener() {

                                             @Override
                                             public void cancelClick(View view) {
                                            	 customDialog.dismiss();
                                                 neterrorshow = false;
                                             }
                                         });
                                 neterrorshow = true;
                             } catch (android.view.WindowManager.BadTokenException e) {
                             }
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
                            hashLog.put(EventProperty.Q, autoCompleteTextView.getText().toString());
                            NetworkUtils.SaveLogToLocal(NetworkUtils.VIDEO_SEARCH, hashLog);
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
                //m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                m.showSoftInput(autoCompleteTextView, InputMethodManager.SHOW_FORCED);
//			autoCompleteTextView.showDropDown();
                // autoCompleteTextView.onKeyDown(KeyEvent.KEYCODE_BACK, null);
                break;
            case R.id.arrow_left:
                gridView.pageScroll(View.FOCUS_LEFT);
                break;
            case R.id.arrow_right:
                gridView.pageScroll(View.FOCUS_RIGHT);
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
                    if (null == movieList || movieList.size() == 0) {
                        setSearchResult(0);
                        gridView.setVisibility(View.INVISIBLE);
                        arrow_left.setVisibility(View.INVISIBLE);
                        arrow_right.setVisibility(View.INVISIBLE);
                        return;
                    }
                    setSearchResult(movieList.size());
                    // cacheAdapter = new CacheAdapter(SearchActivity.this, movieList);
                    // gridView.setAdapter(cacheAdapter);
                    movieList = SortMovieUtils.sort(movieList);
                    setImageAdapter(movieList);
                    break;
                case UPDATE_SUGGEST:
                    autoAdapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.autocomplete_list_item, listHelp);// 配置Adaptor
//				autoCompleteTextView.setAdapter(autoAdapter);
//				autoCompleteTextView.showDropDown();
                    // autoAdapter.notifyDataSetChanged();
                    break;
                case ADD_VIEW:
                    if (null == listHotWords)
                        return;
                    for (int j = 0; j < listHotWords.size(); j++) {
                        if (j == 8) {
                            break;
                        }
                        final Button btnHotWords = new Button(SearchActivity.this);
                        // final Button btnHotWords = (Button) findViewById(R.id.btn_words);
                        int mPaddingLR = getResources().getDimensionPixelSize(R.dimen.search_btnHotWords_PLR);
                        float rate = DaisyUtils.getVodApplication(SearchActivity.this).getRate(SearchActivity.this);
                        int mTextSize = (int) (getResources().getDimensionPixelSize(R.dimen.search_btnHotWords_textsize) / rate);
                        btnHotWords.setPadding(mPaddingLR, 0, mPaddingLR, 0);
                        btnHotWords.setTextSize(mTextSize);


                        btnHotWords.setId(j);
                        // btnHotWords.setBackgroundColor(R.drawable.gridview_text_selector);
                        btnHotWords.setBackgroundResource(R.drawable.hotwords_selector);
                        btnHotWords.setTextColor(getResources().getColor(R.color.hotwords_text_color));
                        btnHotWords.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                        // btnHotWords.setTypeface(Typeface.DEFAULT_BOLD,Typeface.NORMAL);
                        btnHotWords.setText(String.valueOf(listHotWords.get(j)));
                        linearAdd.addView(btnHotWords);
                        btnHotWords.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                loadDialogShow();
                                // 这里继续写执行查询代码
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            hashLog.put(EventProperty.Q, btnHotWords.getText().toString());
                                            NetworkUtils.SaveLogToLocal(NetworkUtils.VIDEO_SEARCH, hashLog);
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
                    if (null == movieList || movieList.size() == 0) {
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


    };

    /**
     * This clear data and set adapter
     *
     * @param movieList
     */
    private void setImageAdapter(List<MovieBean> movieList) {
        imageAdapter.cancelAsync();
        imageAdapter = new SearchAdapter(SearchActivity.this, movieList, R.layout.search_grid_view_item);
        gridView.setAdapter(imageAdapter);
        if (arrow_left.isShown()) {
            arrow_left.setVisibility(View.INVISIBLE);
        }
        if (arrow_right.isShown()) {
            arrow_right.setVisibility(View.INVISIBLE);
        }
        if (movieList.size() > 15) {
            arrow_right.setVisibility(View.VISIBLE);
        }
        gridView.setVisibility(View.VISIBLE);
        imageAdapter.setList((ArrayList<MovieBean>) movieList);
        gridView.setFocusable(true);
    }

    ;

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
			if (hotlinefuture != null)
				hotlinefuture.cancel(true);
            finish();
        } else if ((keyCode == 774 || keyCode == 480) && autoCompleteTextView != null) {
            InputMethodManager imm = (InputMethodManager) autoCompleteTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
        }
        return false;
    }

    private void loadDialogShow() {
        try {
            if (loadDialog.isShowing()) {
                loadDialog.dismiss();
            } else {
                loadDialog.show();
            }
        } catch (android.view.WindowManager.BadTokenException e) {
        }
    }

    /**
     * 点击Gridview Item
     */
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long positions) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        hashLog.put(EventProperty.CONTENT_TYPE, movieList.get(position).content_model);
        hashLog.put(EventProperty.Q, autoCompleteTextView.getText().toString());
        hashLog.put(EventProperty.ITEM, movieList.get(position).item_pk);
        hashLog.put(EventProperty.TITLE, movieList.get(position).title);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkUtils.SaveLogToLocal(NetworkUtils.VIDEO_SEARCH_ARRIVE, hashLog);
                hashLog.clear();
            }
        }) {
        }.start();
        try {
            if (movieList.get(position).is_complex) {
                DaisyUtils.gotoSpecialPage(SearchActivity.this, movieList.get(position).content_model, movieList.get(position).url, "search");
            } else {
                tool = new InitPlayerTool(SearchActivity.this);
                tool.fromPage = "search";
                tool.setonAsyncTaskListener(new onAsyncTaskHandler() {

                    @Override
                    public void onPreExecute(Intent intent) {
                        // TODO Auto-generated method stub
                        loadDialogShow();
                    }

                    @Override
                    public void onPostExecute() {
                        // TODO Auto-generated method stub
                        loadDialogShow();
                    }
                });
                tool.initClipInfo(movieList.get(position).url, InitPlayerTool.FLAG_URL);
            }
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
