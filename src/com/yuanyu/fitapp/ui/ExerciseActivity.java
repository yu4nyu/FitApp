package com.yuanyu.fitapp.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.yuanyu.fitapp.R;
import com.yuanyu.fitapp.R.id;
import com.yuanyu.fitapp.R.layout;
import com.yuanyu.fitapp.R.string;
import com.yuanyu.fitapp.model.Common;
import com.yuanyu.fitapp.model.Exercise;
import com.yuanyu.fitapp.model.Exercise.ExerciseType;
import com.yuanyu.fitapp.utils.ProgressWheel;
import com.yuanyu.fitapp.utils.gifview.GifView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ExerciseActivity extends Activity {
	
	private static enum ExerciseStatus{
		ACTIVITY,
		REST,
	}
	
	// TODO remove to Common
	public static final String INTENT_KEY_NAME = "name";
	public static final String INTENT_KEY_TYPE = "type";
	public static final String INTENT_KEY_ACTIVITY_TIME = "activity_time";
	public static final String INTENT_KEY_REST_TIME = "rest_time";
	//public static final String INTENT_KEY_ROUND = "round";
	public static final String INTENT_KEY_GIF_LIST = "gif_list";
	
	private static final int DEFAULT_ACTIVITY_TIME = 20;
	private static final int DEFAULT_REST_TIME = 10;
	
	private static final int MSG_UPDATE_TIMER = 0;

	private ProgressWheel mProgressWheel;
	private LinearLayout mProgressBackground;
	private GifView mGifView;
	private TextView mTopLeftText;
	private TextView mTopCenterText;
	private TextView mTopRightText;
	
	private Exercise mExercise;
	
	private int mPercentageTime; // in millisecond
	private int mCurrentTotalTime = 0; // in millisecond
	private int mCurrentRound = 0;
	private int mCurrentProgress = 0;
	private ExerciseStatus mCurrentStatus = ExerciseStatus.ACTIVITY;
	
	private Timer mTimer; // TODO prevent the leak when rotating
	private TimerTask mTask;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_UPDATE_TIMER:
				if(mCurrentProgress == 360) {
					if(mCurrentStatus == ExerciseStatus.ACTIVITY) {
						mProgressWheel.resetCount();
						mCurrentProgress = 0;
						mCurrentStatus = ExerciseStatus.REST;
						mProgressBackground.setBackgroundColor(Color.BLUE);
						mTask.cancel();
						startTimer(mExercise.getRestTime());
					}
					else if(mCurrentStatus == ExerciseStatus.REST) {
						if(mCurrentRound < mExercise.getGifList().size() - 1) {
							mProgressWheel.resetCount();
							mCurrentProgress = 0;
							mCurrentStatus = ExerciseStatus.ACTIVITY;
							mProgressBackground.setBackgroundColor(Color.RED);
							mCurrentRound++;
							loadGif(mCurrentRound);
							mTask.cancel();
							startTimer(mExercise.getActivityTime());
						}
						else { // Reach the end
							finish();
						}
					}
				}
				else {
					mProgressWheel.incrementProgress();
					mCurrentProgress++;
				}
				mCurrentTotalTime += mPercentageTime;
				updateText();
				break;
			}
		}
	};
	
	// TODO, manage the process when rotating screen
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exercise);
		
		mProgressWheel = (ProgressWheel)findViewById(R.id.exercise_activity_progress_wheel);
		mProgressBackground = (LinearLayout)findViewById(R.id.exercise_activity_progress_background);
		mGifView = (GifView)findViewById(R.id.exercise_activity_gif_view);
		mTopLeftText = (TextView)findViewById(R.id.exercise_activity_top_left_text);
		mTopCenterText = (TextView)findViewById(R.id.exercise_activity_top_center_text);
		mTopRightText = (TextView)findViewById(R.id.exercise_activity_top_right_text);
		
		mProgressBackground.setBackgroundColor(Color.RED);
		
		readData();
		updateText();
		loadGif(0);
		startTimer(mExercise.getActivityTime());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTimer.cancel();
		mHandler.removeCallbacksAndMessages(null);
	}

	private void readData() {
		Intent intent = getIntent();
		String name = intent.getStringExtra(INTENT_KEY_NAME);
		ExerciseType type = (ExerciseType)intent.getSerializableExtra(INTENT_KEY_TYPE);
		int activityTime = intent.getIntExtra(INTENT_KEY_ACTIVITY_TIME, DEFAULT_ACTIVITY_TIME);
		int restTime = intent.getIntExtra(INTENT_KEY_REST_TIME, DEFAULT_REST_TIME);
		mExercise = new Exercise(name, type, activityTime, restTime);
		
		List<String> list = intent.getStringArrayListExtra(INTENT_KEY_GIF_LIST);
		mExercise.addAllGifsToList(list);
	}
	
	private void updateText() {
		// Top left text TODO
		String str;

		// Top center text
		str = mExercise.getName() + "\n";
		int currentMinute = (mCurrentTotalTime/1000)/60;
		if(currentMinute < 10) {
			str += "0" + currentMinute + ":";
		}
		else {
			str += currentMinute + ":";
		}
		int currentSecond = (mCurrentTotalTime/1000)%60;
		if(currentSecond < 10) {
			str += "0" + currentSecond + " / ";
		}
		else {
			str += currentSecond + " / ";
		}
		int totalTime = (mExercise.getActivityTime() + mExercise.getRestTime()) * mExercise.getGifList().size();
		int totalMinute = totalTime / 60;
		if(totalMinute < 10) {
			str += "0" + totalMinute + ":";
		}
		else {
			str += totalMinute + ":";
		}
		int totalSecond = totalTime % 60;
		if(totalSecond < 10) {
			str += "0" + totalSecond;
		}
		else {
			str += totalSecond;
		}
		mTopCenterText.setText(str);
		
		// Top right text
		str = "" + (mCurrentRound + 1) + "/" + mExercise.getGifList().size();
		mTopRightText.setText(str);
		
		// Progress wheel text
		currentMinute = (mCurrentProgress*mPercentageTime/1000)/60;
		if(currentMinute < 10) {
			str = "0" + currentMinute + ":";
		}
		else {
			str = currentMinute + ":";
		}
		currentSecond = (mCurrentProgress*mPercentageTime/1000)%60;
		if(currentSecond < 10) {
			str += "0" + currentSecond + "\n";
		}
		else {
			str += currentSecond + "\n";
		}
		totalTime = 0;
		if(mCurrentStatus == ExerciseStatus.ACTIVITY) {
			totalTime = mExercise.getActivityTime();
		}
		else if(mCurrentStatus == ExerciseStatus.REST) {
			totalTime = mExercise.getRestTime();
		}
		totalMinute = totalTime / 60;
		if(totalMinute < 10) {
			str += "0" + totalMinute + ":";
		}
		else {
			str += totalMinute + ":";
		}
		totalSecond = totalTime % 60;
		if(totalSecond < 10) {
			str += "0" + totalSecond;
		}
		else {
			str += totalSecond;
		}
		mProgressWheel.setText(str);
	}
	
	private void loadGif(int position) { // Load the file whose name is in activity list at provided position
		if(position > mExercise.getGifList().size()) {
			Toast.makeText(this, R.string.no_activity_selected, Toast.LENGTH_LONG).show();
		}
		
		String file = mExercise.getGifList().get(position);
		byte[] gifByteArray = null;
		try {
			String path = Common.GIF_DIRECTORY_NAME + "/" + file;
			InputStream inputStream = getAssets().open(path);
			int size = inputStream.available();
			gifByteArray = new byte[size];//inputStream.available()];
			inputStream.read(gifByteArray);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//String fileName = Common.GIF_DIRECTORY_NAME + "/" + file;
		mGifView.setGifImage(gifByteArray);
		//mGifView.setGifImage(fileName);
		mGifView.setLoopNumber(Integer.MAX_VALUE);
	}
	
	/**
	 * @param Ӧ��Timer��ʱ�������ʱ������֮���Timer�ᱻȡ��
	 */
	private void startTimer(int time) {
		long interval = time * 1000 / 360; // Interval for increment 1 degree of progress wheel
		mPercentageTime = (int)interval;
		
		mTimer = new Timer();
		mTask = getTimerTask(); // Recreate a timer task
		mTimer.schedule(mTask, interval, interval);
	}
	
	private TimerTask getTimerTask() {
		return new TimerTask(){
			@Override
			public void run() {
				mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
			}
		};
	}
}
