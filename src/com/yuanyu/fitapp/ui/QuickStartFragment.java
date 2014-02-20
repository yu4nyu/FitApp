package com.yuanyu.fitapp.ui;

import java.util.ArrayList;

import com.yuanyu.fitapp.ExerciseActivity;
import com.yuanyu.fitapp.R;
import com.yuanyu.fitapp.model.Exercise.ExerciseType;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class QuickStartFragment extends Fragment implements OnClickListener, AnimationListener {

	ImageView mPlayImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_quick_start, container, false);

		mPlayImage = (ImageView) v.findViewById(R.id.fragment_quick_start_image);
		mPlayImage.setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.fragment_quick_start_image) {
			Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_fade);
			animation.setAnimationListener(this);
			animation.setFillAfter(true);
			v.startAnimation(animation);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		quickStart();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	private void quickStart() {
		// TODO, use random parameters
		Intent intent = new Intent(getActivity(), ExerciseActivity.class);
		intent.putExtra(ExerciseActivity.INTENT_KEY_NAME, getString(R.string.item_quick_start));
		intent.putExtra(ExerciseActivity.INTENT_KEY_TYPE , ExerciseType.TABATA);
		intent.putExtra(ExerciseActivity.INTENT_KEY_ACTIVITY_TIME, 20);
		intent.putExtra(ExerciseActivity.INTENT_KEY_REST_TIME, 10);
		ArrayList<String> gifList = new ArrayList<String>();
		gifList.add("situp");
		gifList.add("situp");
		intent.putStringArrayListExtra(ExerciseActivity.INTENT_KEY_GIF_LIST, gifList);
		startActivity(intent);
	}
}
