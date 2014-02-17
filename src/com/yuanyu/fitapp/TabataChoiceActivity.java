package com.yuanyu.fitapp;

import java.util.ArrayList;

import com.yuanyu.fitapp.model.Exercise.ExerciseType;
import com.yuanyu.fitapp.model.GifLoader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class TabataChoiceActivity extends Activity {
	
	// TODO remove to Common
	public static final int ROUND_NUMBER = 8;
	public static final int ACTIVITY_TIME = 20;
	public static final int ACTIVITY_REST = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabata_choice);
	}

	public void onClickTabataRandom(View view) {
		Intent intent = new Intent(TabataChoiceActivity.this, ExerciseActivity.class);
		intent.putExtra(ExerciseActivity.INTENT_KEY_NAME, getString(R.string.tabata_ramdom_name));
		intent.putExtra(ExerciseActivity.INTENT_KEY_TYPE , ExerciseType.TABATA);
		intent.putExtra(ExerciseActivity.INTENT_KEY_ACTIVITY_TIME, ACTIVITY_TIME);
		intent.putExtra(ExerciseActivity.INTENT_KEY_REST_TIME, ACTIVITY_REST);
		ArrayList<String> gifList = (ArrayList<String>)GifLoader.INSTANCE.getRandomUnrepeatableList(this, ROUND_NUMBER);
		intent.putStringArrayListExtra(ExerciseActivity.INTENT_KEY_GIF_LIST, gifList);
		startActivity(intent);
	}
	
	public void onClickTabataSelection(View view) {
		Intent intent = new Intent(TabataChoiceActivity.this, ActivitySelectionActivity.class);
		startActivity(intent);
	}

	public void onClickTabataPlaylist(View view) {
	
	}
}
