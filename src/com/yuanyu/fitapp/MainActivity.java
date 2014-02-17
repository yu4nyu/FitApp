package com.yuanyu.fitapp;

import com.yuanyu.fitapp.ui.QuickStartFragment;
import com.yuanyu.fitapp.utils.FragmentSwitcher;

import android.os.Bundle;
import android.app.ActionBar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

public class MainActivity extends FragmentActivity implements FragmentSwitcher.OnFragmentSwitchedListener {

	private static final String TAG = "MainActivity";
	
	private final FragmentSwitcher.Item[] ITEMS = {
			
            new FragmentSwitcher.Item(R.id.item_quick_start, TAG + "-QuickStartFragment") {
                @Override
                public Fragment createFragment() {
                    return new QuickStartFragment();
                }
            }.setTag(R.string.item_quick_start),
    };
	
	private FragmentSwitcher mFragmentSwitcher;
	private DrawerLayout mDrawerLayout;
    private View mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		View contentView = findViewById(android.R.id.content);
        mFragmentSwitcher = new FragmentSwitcher(getSupportFragmentManager(), contentView,
                R.id.main_activity_panel_main_view, R.string.item_quick_start, TAG + "-FragmentSwitcher", ITEMS);
        mFragmentSwitcher.setOnFragmentSwitchedListener(this);
        //updateTitleFromFragment(); // TODO
        //mFragmentSwitcher.setOnClickListener(this); // TODO
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = findViewById(R.id.main_activity_panel_sidemenu);

        if (mDrawerLayout!=null) {

            // set a custom shadow that overlays the main content when the drawer opens
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

                /** Called when a drawer has settled in a completely closed state. */
                @Override
                public void onDrawerClosed(View view) {
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    //                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /** Called when a drawer has settled in a completely open state. */
                @Override
                public void onDrawerOpened(View drawerView) {
                    getActionBar().setDisplayHomeAsUpEnabled(false);
                    //                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            // Set up the action bar in slidingMenu mode
            final ActionBar actionBar = getActionBar();
            //actionBar.setHomeButtonEnabled(true); // TODO confirm use this or not
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}
	
	/*public void onClickQuickStart(View view) {
		// TODO, use random parameters
		Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
		intent.putExtra(ExerciseActivity.INTENT_KEY_NAME, getString(R.string.item_quick_start));
		intent.putExtra(ExerciseActivity.INTENT_KEY_TYPE , ExerciseType.TABATA);
		intent.putExtra(ExerciseActivity.INTENT_KEY_ACTIVITY_TIME, 20);
		intent.putExtra(ExerciseActivity.INTENT_KEY_REST_TIME, 10);
		ArrayList<String> gifList = new ArrayList<String>();
		gifList.add("situp");
		gifList.add("situp");
		intent.putStringArrayListExtra(ExerciseActivity.INTENT_KEY_GIF_LIST, gifList);
		startActivity(intent);
	}*/
	
	/*public void onClickTabata(View view) {
		Intent intent = new Intent(MainActivity.this, TabataChoiceActivity.class);
		startActivity(intent);
	}*/

	/*public void onClickHiit(View view) {
	
	}*/

	/*public void onClickCrossFit(View view) {
	
	}*/

	@Override
	public void onFragmentSwitched(FragmentSwitcher source, int viewId) {
		// TODO Auto-generated method stub
		
	}
}
