package com.yuanyu.fitapp.model;

import java.util.ArrayList;
import java.util.List;

public class Exercise {
	
	public static enum ExerciseType{
		TABATA,
		HIIT,
		CROSSFIT,
	}
	
	private String mName;
	private ExerciseType mType;
	private List<String> mGifList; // List contains all the activity of the exercise. the same name is allowed.
	
	private int mActivityTime;
	private int mRestTime;
	
	public Exercise(String name, ExerciseType type, int activityTime, int restTime) {
		mName = name;
		mType = type;
		mActivityTime = activityTime;
		mRestTime = restTime;
		mGifList = new ArrayList<String>();
	}
	
	public String getName() {
		return mName;
	}

	public ExerciseType getType() {
		return mType;
	}
	
	public int getActivityTime() {
		return mActivityTime;
	}
	
	public int getRestTime() {
		return mRestTime;
	}
	
	public void addGifToList(String gifName) {
		mGifList.add(gifName);
	}
	
	public void addAllGifsToList(List<String> list) {
		mGifList.addAll(list);
	}
	
	public int getGifNumber() {
		return mGifList.size();
	}
	
	public List<String> getGifList() {
		return mGifList;
	}
}
