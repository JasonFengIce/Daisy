package tv.ismar.daisy.views;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ismar.daisy.R;
import tv.ismar.daisy.core.NetworkUtils;
import tv.ismar.daisy.core.SimpleRestClient;
import tv.ismar.daisy.models.Item;
import tv.ismar.daisy.models.ItemList;
import tv.ismar.daisy.models.SectionList;
import tv.ismar.daisy.views.ItemListScrollView.OnColumnChangeListener;
import tv.ismar.daisy.views.ItemListScrollView.OnItemClickedListener;
import tv.ismar.daisy.views.ItemListScrollView.OnSectionPrepareListener;
import tv.ismar.daisy.views.ScrollableSectionList.OnSectionSelectChangedListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChannelFragment extends Fragment {

	/*
	 * defines max number of cells can be show in a single screen.
	 */
	private static final int MAX_CELLS_IN_SCREEN = 15;
	
	private SimpleRestClient mRestClient = new SimpleRestClient();
	
	private SectionList mSectionList;
	
	private ArrayList<ItemList> mItemLists;
	
	private ItemListScrollView mItemListScrollView;
	
	private ScrollableSectionList mScrollableSectionList;
	
	private TextView mChannelLabel;
	
	private int mCurrentSectionPosition = 0;
	
	public String mTitle;
	
	public String mUrl;
	
	public String mChannel;
	
	private LoadingDialog mLoadingDialog;
	
	private boolean isInitTaskLoading;
	
	private InitTask mInitTask;
	
	private void initViews(View fragmentView) {
		mItemListScrollView = (ItemListScrollView) fragmentView.findViewById(R.id.itemlist_scroll_view);
		mItemListScrollView.setOnSectionPrepareListener(mOnSectionPrepareListener);
		mItemListScrollView.setOnColumnChangeListener(mOnColumnChangeListener);
		mItemListScrollView.setOnItemClickedListener(mOnItemClickedListener);
		mScrollableSectionList = (ScrollableSectionList) fragmentView.findViewById(R.id.section_tabs);
		mScrollableSectionList.setOnSectionSelectChangeListener(mOnSectionSelectChangedListener);
		
		mChannelLabel = (TextView) fragmentView.findViewById(R.id.channel_label);
		mChannelLabel.setText(mTitle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLoadingDialog = new LoadingDialog(getActivity(), getResources().getString(R.string.loading));
		mLoadingDialog.setOnCancelListener(mLoadingCancelListener);
		View fragmentView = inflater.inflate(R.layout.list_view, container, false);
		initViews(fragmentView);
		mInitTask = new InitTask();
		mInitTask.execute(mUrl, mChannel);
		return fragmentView;
	}
	
	private boolean isChannelUrl(String url) {
		String patternStr = ".+/api/tv/sections/[\\w\\d]+/";
		Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
		return matcher.matches();
	}
	
	
	
	class InitTask extends AsyncTask<String, Void, Integer> {
		
		String url;
		String channel;

		@Override
		protected void onPreExecute() {
			if(mLoadingDialog!=null && !mLoadingDialog.isShowing()) {
				mLoadingDialog.show();
			}
			isInitTaskLoading = true;
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			try {
				mItemLists = new ArrayList<ItemList>();
				url = params[0];
				channel = params[1];
				if(!isChannelUrl(url)){
					mSectionList = mRestClient.getSectionsByChannel(channel);
					for(int i=0; i<mSectionList.size(); i++) {
						if(NetworkUtils.urlEquals(url, mSectionList.get(i).url)) {
							mCurrentSectionPosition = i;
							break;
						}
					}
				}
				if(mSectionList==null){
					mSectionList = mRestClient.getSections(url);
				}
				int itemsCount = 0;
				for(int i=0; i<mSectionList.size();i++) {
					ItemList itemList = null;
					if(i==mCurrentSectionPosition || itemsCount<=MAX_CELLS_IN_SCREEN){
						itemList = mRestClient.getItemList(mSectionList.get(i).url);
						// if the itemList's items is not able to fill the full single screen. we need load the second itemList.
						if(itemList.objects!=null) {
							itemsCount += itemList.objects.size();
						}
					} else {
						itemList = new ItemList();
					}
					itemList.slug = mSectionList.get(i).slug;
					itemList.title = mSectionList.get(i).title;
					itemList.count = mSectionList.get(i).count;
					mItemLists.add(itemList);
				}
				return 0;
			} catch (Exception e) {
				e.printStackTrace();

				return -1;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(mLoadingDialog!=null && mLoadingDialog.isShowing()) {
				mLoadingDialog.dismiss();
			}
			isInitTaskLoading = false;
			if(result==-1) {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, (mInitTask = new InitTask()), new String[]{url, channel});
				return;
			}
			if(mSectionList!=null && mItemLists.get(mCurrentSectionPosition)!=null) {
				mScrollableSectionList.init(mSectionList, 1365);
				for(int i=0; i<mItemLists.size(); i++) {
					mItemListScrollView.addSection(mItemLists.get(i), i);
				}
				int totalColumnsOfSectionX = mItemListScrollView.getTotalColumnCount(mCurrentSectionPosition);
				mScrollableSectionList.setPercentage(mCurrentSectionPosition, (int)(1f/(float)totalColumnsOfSectionX*100f));
				mItemListScrollView.jumpToSection(mCurrentSectionPosition);
			} else {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, (mInitTask = new InitTask()), new String[]{url, channel});
			}
//			new GetItemListTask().execute();
			super.onPostExecute(result);
		}
		
	}
	
	class GetItemListTask extends AsyncTask<String, Void, Integer> {
		
		private String slug;
		private String url;
		
		@Override
		protected Integer doInBackground(String... params) {
			int position = -1;
			slug = params[0];
			url = params[1];
			
			for(int i=0; i<mItemLists.size();i++){
				if(slug.equals(mItemLists.get(i).slug)){
					ItemList itemList = mRestClient.getItemList(url);
					if(itemList==null) {
						continue;
					}
					itemList.slug = mSectionList.get(i).slug;
					itemList.title = mSectionList.get(i).title;
					mItemLists.set(i, itemList);
					position = i;
					break;
				}
			}
//			}
			return position;
		}

		@Override
		protected void onPostExecute(Integer position) {
			if(mItemListScrollView!=null && mItemLists!=null && position!=-1) {
				mItemListScrollView.updateSection(mItemLists.get(position), position);
			} else {
				showDialog(AlertDialogFragment.NETWORK_EXCEPTION_DIALOG, new GetItemListTask(), new String[]{slug, url});
			}
		}
		
	}
	
	private OnSectionPrepareListener mOnSectionPrepareListener = new OnSectionPrepareListener() {
		
		@Override
		public void onPrepareNeeded(int position) {
			if(mItemLists.get(position).objects==null){
				mItemListScrollView.addScrapViewsToSection(position, 0);
				new GetItemListTask().execute(mItemLists.get(position).slug, mSectionList.get(position).url);
			} else {
				mItemListScrollView.updateSection(mItemLists.get(position), position);
			}
		}
	};
	
	private OnSectionSelectChangedListener mOnSectionSelectChangedListener = new OnSectionSelectChangedListener() {
		
		@Override
		public void onSectionSelectChanged(int index) {
			if(mItemLists.get(index).objects!=null){
				mItemListScrollView.updateSection(mItemLists.get(index), index);
			} else {
				mItemListScrollView.addScrapViewsToSection(index, 0);
				new GetItemListTask().execute(mItemLists.get(index).slug, mSectionList.get(index).url);
			}
			mItemListScrollView.jumpToSection(index);
		}
	};
	
	private OnColumnChangeListener mOnColumnChangeListener = new OnColumnChangeListener() {
		
		@Override
		public void onColumnChanged(int position, int column, int totalColumn) {
			int percentage = (int)((float)(column+1)/(float)totalColumn*100f);
			mScrollableSectionList.setPercentage(position, percentage);
		}
	};
	
	private OnItemClickedListener mOnItemClickedListener = new OnItemClickedListener() {
		
		@Override
		public void onItemClicked(Item item) {
			Intent intent = new Intent();
			if(item.is_complex) {
				intent.setAction("tv.ismar.daisy.Item");
			} else {
				intent.setAction("tv.ismar.daisy.Play");
			}
			intent.putExtra("url", item.url);
			startActivity(intent);
		}
	};

	@Override
	public void onResume() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(false);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if(mItemListScrollView != null) {
			mItemListScrollView.setPause(true);
		}
		if(mLoadingDialog!=null&& mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
		super.onPause();
	}

	

	@Override
	public void onDestroyView() {
		if(mInitTask!=null && mInitTask.getStatus()!=AsyncTask.Status.FINISHED) {
			mInitTask.cancel(true);
		}
		mInitTask = null;
		mSectionList = null;
		mItemLists = null;
		if(mItemListScrollView!=null){
			mItemListScrollView.clean();
		}
		mItemListScrollView = null;
		mScrollableSectionList = null;
		super.onDestroyView();
	}

	public void showDialog(int dialogType, final AsyncTask task, final Object[] params ) {
		AlertDialogFragment newFragment = AlertDialogFragment.newInstance(dialogType);
		newFragment.setPositiveListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(!isInitTaskLoading) {
					task.execute(params);
				}
				dialog.dismiss();
			}
		});
		newFragment.setNegativeListener(new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getActivity().finish();
				dialog.dismiss();
			}
		});
		FragmentManager manager = getFragmentManager();
		if(manager!=null) {
			newFragment.show(manager, "dialog");
		}
	}
	
	private OnCancelListener mLoadingCancelListener = new OnCancelListener() {
		
		@Override
		public void onCancel(DialogInterface dialog) {
			getActivity().finish();
			dialog.dismiss();
		}
	};

	@Override
	public void onDetach() {
		mRestClient = null;
		super.onDetach();
	}
	
	
}
