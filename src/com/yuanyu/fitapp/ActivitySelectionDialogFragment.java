package com.yuanyu.fitapp;

import com.yuanyu.fitapp.model.GifLoader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ActivitySelectionDialogFragment extends DialogFragment {
	
	public final static String ACTIVITY_INDEX_KEY = "activity_index";
	
	public interface OnActivitySelectedListener {
		public void onActivitySelected(int position, int which);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final OnActivitySelectedListener listener = (OnActivitySelectedListener)getActivity();
		Bundle bundle = getArguments();
		final int index = bundle.getInt(ACTIVITY_INDEX_KEY);
		
		String[] gifs = GifLoader.INSTANCE.getGifArray(getActivity());
		builder.setTitle(R.string.pick_an_activity);
		builder.setItems(gifs, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onActivitySelected(index, which);
			}
		});

		return builder.create();
	}

}
