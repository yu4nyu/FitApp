package com.yuanyu.fitapp.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;

public enum GifLoader {

	INSTANCE;
	
	private List<String> mGifList = null;

	public String[] getGifArray(Context context) {
		loadGifsIfNull(context);
		
		String[] gifs = null;
		gifs = new String[mGifList.size()];
		mGifList.toArray(gifs);
		return gifs;
	}
	
	public List<String> getGifList(Context context) {
		loadGifsIfNull(context);
		return mGifList;
	}
	
	public String[] getRandomRepeatableArray(Context context, int number) {
		loadGifsIfNull(context);
		
		String[] gifs = new String[number];
		Random random = new Random();
		int result;
		for(int i = 0; i < number; i++) {
			result = random.nextInt(mGifList.size());
			gifs[i] = mGifList.get(result);
		}
		
		return gifs;
	}
	
	public List<String> getRandomRepeatableList(Context context, int number) {
		loadGifsIfNull(context);
		
		List<String> gifs = new ArrayList<String>();
		Random random = new Random();
		int result;
		for(int i = 0; i < number; i++) {
			result = random.nextInt(mGifList.size());
			gifs.add(mGifList.get(result));
		}
		
		return gifs;
	}
	
	public String[] getRandomUnrepeatableArray(Context context, int number) {
		loadGifsIfNull(context);
		
		if(number > mGifList.size()) {
			return new String[0];
		}
		
		String[] gifs = new String[number];
		List<String> list = new ArrayList<String>(mGifList);
		Random random = new Random();
		int result;
		for(int i = 0; i < number; i++) {
			result = random.nextInt(list.size());
			gifs[i] = list.get(result);
			list.remove(result);
		}
		
		return gifs;
	}
	
	public List<String> getRandomUnrepeatableList(Context context, int number) {
		loadGifsIfNull(context);
		
		List<String> gifs = new ArrayList<String>();
		if(number > mGifList.size()) {
			return gifs;
		}
		
		List<String> list = new ArrayList<String>(mGifList);
		Random random = new Random();
		int result;
		for(int i = 0; i < number; i++) {
			result = random.nextInt(list.size());
			gifs.add(list.get(result));
			list.remove(result);
		}
		
		return gifs;
	}
	
	private void loadGifsIfNull(Context context) {
		if(mGifList == null) {
			mGifList = new ArrayList<String>();
			String[] gifs = null;
			AssetManager assetManager = context.getAssets();
			try {
				gifs = assetManager.list(Common.GIF_DIRECTORY_NAME);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(gifs != null) {
				for(int i = 0; i < gifs.length; i++) {
					mGifList.add(gifs[i]);
				}
			}
		}
	}
}
