
package com.yuanyu.fitapp.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class RetainFragment<RetainedData> extends Fragment {

    public RetainFragment() {
    }

    public static <RetainedData> RetainFragment<RetainedData> findOrCreateRetainFragment(
            FragmentManager fm, String fragmentTag) {
        return findOrCreateRetainFragment(fm, fragmentTag, null);
    }

    @SuppressWarnings("unchecked")
    public static <RetainedData> RetainFragment<RetainedData> findOrCreateRetainFragment(
            FragmentManager fm, String fragmentTag, RetainedData defaultData) {

        RetainFragment<RetainedData> fragment =
                (RetainFragment<RetainedData>) fm.findFragmentByTag(fragmentTag);

        if (fragment == null) {
            fragment = new RetainFragment<RetainedData>();
            fragment.setData(defaultData);
            fm.beginTransaction().add(fragment, fragmentTag).commit();
        }

        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private RetainedData mData;

    public void setData(RetainedData data) {
        mData = data;
    }

    public RetainedData getData() {
        return mData;
    }
}
