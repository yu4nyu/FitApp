package com.yuanyu.fitapp;

import java.util.ArrayList;
import java.util.List;

import com.yuanyu.fitapp.model.GifLoader;
import com.yuanyu.fitapp.model.Exercise.ExerciseType;
import com.yuanyu.fitapp.ui.ExerciseActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ActivitySelectionActivity extends Activity implements OnItemClickListener,
	ActivitySelectionDialogFragment.OnActivitySelectedListener {
	
	private ListView mListView;
	private ActivitySelectionAdapter mAdapter;
	private List<String> mActivityNameList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_selection);
		
		mListView = (ListView)findViewById(R.id.activity_selection_list);
		
		for(int i = 0; i < 8; i++) {
			mActivityNameList.add("");
		}
		
		// Must add footer before set adapter
		LayoutInflater inflater = LayoutInflater.from(this);
		final View footer = inflater.inflate(R.layout.activity_selection_list_footer, null, false);
		mListView.addFooterView(footer);
		Button okButton = (Button) findViewById(R.id.activity_selection_list_footer_button);
		okButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(detectIfAllSelected()){
					startTabataExercise();
				}
			}
		});
		
		mAdapter = new ActivitySelectionAdapter(this, mActivityNameList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private boolean detectIfAllSelected() {
		boolean allSelected = true;
		//int firstVisible = mListView.getFirstVisiblePosition();
		//int lastVisible = mListView.getLastVisiblePosition();
		for(int i = 0; i < mActivityNameList.size(); i++){
			if(mActivityNameList.get(i).equals("")) {
				allSelected = false;
				/*if(i >= firstVisible && i <= lastVisible) {
					mListView.getChildAt(i).setB
				}*/ // TODO highlight the empty items
			}
		}
		
		return allSelected;
	}
	
	private void startTabataExercise() {
		Intent intent = new Intent(this, ExerciseActivity.class);
		intent.putExtra(ExerciseActivity.INTENT_KEY_NAME, getString(R.string.tabata_ramdom_name));
		intent.putExtra(ExerciseActivity.INTENT_KEY_TYPE , ExerciseType.TABATA);
		intent.putExtra(ExerciseActivity.INTENT_KEY_ACTIVITY_TIME, TabataChoiceActivity.ACTIVITY_TIME);
		intent.putExtra(ExerciseActivity.INTENT_KEY_REST_TIME, TabataChoiceActivity.ACTIVITY_REST);
		ArrayList<String> gifList = (ArrayList<String>)mActivityNameList;
		intent.putStringArrayListExtra(ExerciseActivity.INTENT_KEY_GIF_LIST, gifList);
		startActivity(intent);
	}
	
	private static class ActivitySelectionAdapter extends BaseAdapter {
		
		private List<String> mData;
		private LayoutInflater mInflater;

		public ActivitySelectionAdapter(Context context, List<String> data) {
			mData = data;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public String getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.activity_selection_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.index = (TextView) convertView.findViewById(R.id.activity_selection_list_item_index);
				viewHolder.name = (TextView) convertView.findViewById(R.id.activity_selection_list_item_name);
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			viewHolder.index.setText((position + 1) + ". ");
			viewHolder.name.setText(mData.get(position));
			
			return convertView;
		}
		
		static class ViewHolder{
			TextView index;
			TextView name;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		ActivitySelectionDialogFragment dialogFragment = new ActivitySelectionDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(ActivitySelectionDialogFragment.ACTIVITY_INDEX_KEY, position);
		dialogFragment.setArguments(bundle);
		dialogFragment.show(this.getFragmentManager(), "activity_selection");
	}

	@Override
	public void onActivitySelected(int position, int which) {
		String[] gifs = GifLoader.INSTANCE.getGifArray(this);
		mActivityNameList.set(position, gifs[which]);
		mAdapter.notifyDataSetChanged();
	}
}
