
package com.yuanyu.fitapp.utils;

import com.yuanyu.fitapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.View;

/**
 * Class that helps implementing a series of views that when clicked switch a
 * fragment
 */
public class FragmentSwitcher implements View.OnClickListener, FragmentManager.OnBackStackChangedListener {

    private static final String OVERLAY_FRAGMENT_TAG = "OVERLAY_FRAGMENT";
    private static final String OVEROVERLAY_FRAGMENT_TAG = "OVEROVERLAY_FRAGMENT";

    public interface OnFragmentSwitchedListener {
        /**
         * Called <b>after</b> the fragment state has changed
         * <p>
         * Calls to {@link FragmentSwitcher#getCurrentFragment()} will result
         * in the same id as provided.
         **/
        void onFragmentSwitched(FragmentSwitcher source, int viewId);
    }
    /**
     * An Item that links the R.id. of a View (e.g. a Button) to a Fragment.
     * Clicking that View will cause the Fragment to be shown.
     */
    public static abstract class Item {
        private final int mTriggerViewId;
        private final String mFragmentTag;
        private Object mTag;
        private boolean mAddToBackStack = false;
        private boolean mCustomAnimations = false;
        private boolean mDisplayTitle = true;

        public Item(int clickTriggerViewId, String fragmentTag) {
            mTriggerViewId = clickTriggerViewId;
            mFragmentTag = fragmentTag;
        }

        /**
         * The id of the view inside the contentView that when clicked should
         * show this fragment
         */
        public int getClickTriggerViewId() {
            return mTriggerViewId;
        }

        /**
         * Return a new Fragment here
         */
        public abstract Fragment createFragment();

        /**
         * @return a unique tag for this fragment
         */
        String getFragmentTag() {
            return mFragmentTag;
        }

        /**
         * Allows to add random data to this item. Useful in conjunction with
         * {@link FragmentSwitcher#getCurrentFragmentItem()}
         * @return self to allow chaining calls
         */
        public final Item setTag(Object tag) {
            mTag = tag;
            return this;
        }

        /**
         * Get the data previously attached with {@link #setTag(Object)}. Useful in conjunction with
         * {@link FragmentSwitcher#getCurrentFragmentItem()}
         */
        public final Object getTag() {
            return mTag;
        }

        /**
         * If set to true, the fragment transaction will be added to the back stack
         * {@link FragmentTransaction#addToBackStack(String)}
         * Default value is false
         * @param addToBackStack
         * @return self to allow chaining calls
         */
        public final Item setAddToBackStack(boolean addToBackStack) {
            mAddToBackStack = addToBackStack;
            return this;
        }

        /**
         * Get the data previously attached with {@link #setAddToBackStack(boolean)}.
         */
        public boolean getAddToBackStack() {
            return mAddToBackStack;
        }

        public final Item setCustomAnimations(boolean customAnimations) {
            mCustomAnimations = customAnimations;
            return this;
        }

        public boolean getCustomAnimations() {
            return mCustomAnimations;
        }

        public boolean hasDisplayTitle() {
            return mDisplayTitle;
        }

        public final Item  setDisplayTitle(boolean mDisplayTitle) {
            this.mDisplayTitle = mDisplayTitle;
            return this;
        }

    }

    private static final Logger LOG = Logger.getInstance("FragmentSwitcher");
    private static final boolean DBG = false;

    private final int mFragmentContainerId;
    private final FragmentManager mFragmentManager;
    private final View mContentView;
    private final SparseArray<Item> mFragmentItems =
            new SparseArray<Item>();
    private final RetainFragment<Integer> mRetainFragment;
    private View.OnClickListener mChildClickListener;
    private OnFragmentSwitchedListener mFragmentSwitchedListener;
    private int mFragmentParentId;
    private int mTemporaryFragmentItemIdx;

    /**
     * Instanciate from onCreate of an Activity. Fragments are identified by the
     * R.id. of their clickable views.
     * Calling this constructor will
     * <ul>
     * <li>set the OnClickListener for every View in the Items list
     * <li>restore the last active fragment or set the default one
     * </ul>
     * Fragments will be detached once they are swapped out so they can be
     * re-used during the Activity is
     * alive and in case they have {@link Fragment#setRetainInstance(boolean)}
     * set to true even when
     * the Activity is recreated.
     *
     * @param fm SupportFragmentManager used to add / attach / detach the
     *            fragments
     * @param contentView the view that contains all the clickable views
     * @param fragmentContainerViewId R.id. of the contentframe that holds the
     *            fragments
     * @param defaultFragment R.id. of the clickable view, must exist in the
     *            Item list
     * @param fragmentSwitcherTag Unique Fragment Tag so this FragmentSwitcher
     *            can store some data. This tag should not be used by any other
     *            fragment or FragmentSwitcher in a different Activity
     * @param fragmentItems an Array of Items that hold the necessary
     *            information to switch fragments
     */
    public FragmentSwitcher(FragmentManager fm, View contentView, int fragmentContainerViewId,
            int defaultFragment, String fragmentSwitcherTag, Item... fragmentItems) {
        mFragmentManager = fm;
        mFragmentManager.addOnBackStackChangedListener(this);
        mContentView = contentView;
        mFragmentContainerId = fragmentContainerViewId;
        for (Item item : fragmentItems) {
            int button = item.getClickTriggerViewId();
            mContentView.findViewById(button).setOnClickListener(this);
            mFragmentItems.put(button, item);
        }
        mTemporaryFragmentItemIdx = mFragmentItems.size();
        mRetainFragment = RetainFragment.findOrCreateRetainFragment(mFragmentManager, "current_fragment_id");
        if (mRetainFragment.getData() == null) {
            if (DBG) {
                LOG.d("No RetainFragment, setting default %d", defaultFragment);
            }
            storeCurrentFragmentId(defaultFragment);
            setFragment(getCurrentFragment());
        }

        // NOTE: no need to set the current fragment if we have a retain (i.e. on screen rotation for example),
        // everything is already setup in place correctly
    }

    private void storeCurrentFragmentId(int fragmentId) {
        mRetainFragment.setData(Integer.valueOf(fragmentId));
    }

    /**
     * @return the current R.id. belonging to the current fragment
     */
    public int getCurrentFragment() {
        return mRetainFragment.getData().intValue();
    }

    /**
     * @return the Item belonging to the current fragment
     */
    public Item getCurrentFragmentItem() {
        return mFragmentItems.get(getCurrentFragment());
    }

    /**
     * Get notified about every click on any of the clickable views.
     * Notification happens <b>before</b> Fragment state is changed.
     */
    public void setOnClickListener(View.OnClickListener listener) {
        mChildClickListener = listener;
    }

    /**
     * Get notified when Fragments were switched. Does not notify after every
     * click, only when the state actually switches.
     * Notification happens <b>after</b> Fragment state is changed.
     */
    public void setOnFragmentSwitchedListener(OnFragmentSwitchedListener listener) {
        mFragmentSwitchedListener = listener;
    }

    /**
     * Change the current fragment identified by the R.id of the View that is
     * associated with this Fragment
     */
    public void setFragment(int fragmentId) {
        setFragment(fragmentId, null);
    }

    /**
     * Change the current fragment identified by the R.id of the View that is
     * associated with this Fragment
     */
    public void setFragment(int fragmentId, Bundle args) {

        // First thing to do is to remove the overlay fragment that may be displayed on top of the current fragment
        // And call executePendingTransactions so that the rest of the method is working like if there was no overlay fragment!
        hideOverOverlayFragment();
        hideOverlayFragment();

        Item fragmentItem = mFragmentItems.get(fragmentId);
        if (fragmentItem == null) {
            throw new AssertionError("No Fragment associated with view id:" + fragmentId);
        }

        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate (null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (fragmentItem.getAddToBackStack()) {
            mFragmentParentId = getCurrentFragment();
        }
        storeCurrentFragmentId(fragmentId);
        String fragmentTag = fragmentItem.getFragmentTag();
        Fragment targetFragment = mFragmentManager.findFragmentByTag(fragmentTag);
        Fragment oldFragment = mFragmentManager.findFragmentById(mFragmentContainerId);
        if (targetFragment == null || targetFragment != oldFragment) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            if (fragmentItem.getCustomAnimations()) {
                transaction.setCustomAnimations(R.anim.full_screen_fragment_enter,
                                                R.anim.fast_fade_out,
                                                R.anim.fast_fade_in,
                                                R.anim.full_screen_fragment_exit);
            } else {
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            }
            if (fragmentItem.getAddToBackStack()) {
                transaction.addToBackStack(fragmentTag);
            }

            if (oldFragment != null) {
                if (DBG) {
                    LOG.d("Detaching old different fragment.");
                }
                transaction.detach(oldFragment);
            }
            if (targetFragment == null) {
                if (DBG) {
                    LOG.d("Creating and adding new fragment (%s).", fragmentTag);
                }
                targetFragment = fragmentItem.createFragment();
                // If there is no arg to put, we create an empty one, so that it can be updated later
                if (args==null) {
                    args = new Bundle();
                }
                targetFragment.setArguments(args);
                transaction.add(mFragmentContainerId, targetFragment, fragmentTag);
            } else {
                if (DBG) {
                    LOG.d("Re-Attaching fragment (%s).", fragmentTag);
                }
                transaction.attach(targetFragment);
                // CAUTION: we play a dangerous game here...:
                // Clear the previously set arguments. OK for now, but may not be possible in the futur if some of the args are expected to stay forever
                targetFragment.getArguments().clear();
                // Update the arguments
                if (args!=null) {
                    targetFragment.getArguments().putAll(args);
                }
            }

            transaction.commit();

            mFragmentManager.executePendingTransactions();

            if (mFragmentSwitchedListener != null) {
                mFragmentSwitchedListener.onFragmentSwitched(this, fragmentId);
            }
        } else if (DBG) {
            LOG.d("Fragment state needs no change");
        }
    }

    public void setTemporaryFragment(int fragmentId, Item fragmentItem) {
        int button = fragmentItem.getClickTriggerViewId();
        if (mTemporaryFragmentItemIdx < mFragmentItems.size()) {
            mFragmentItems.removeAt(mTemporaryFragmentItemIdx);
        }
        mFragmentItems.put(button, fragmentItem);
        setFragment(fragmentId);
    }

    /**
     * To display a fragment without removing the underlaying one
     * @param fragment
     */
    public void showOverlayFragment(Fragment fragment) {
        // remove the previous one if it exists
        hideOverlayFragment();
        // The overlay fragment does not touch mFragmentItems etc.
        mFragmentManager.beginTransaction().add(mFragmentContainerId, fragment, OVERLAY_FRAGMENT_TAG).commit();
    }

    public void hideOverlayFragment() {
        Fragment overlayFragment = mFragmentManager.findFragmentByTag(OVERLAY_FRAGMENT_TAG);
        if (overlayFragment != null) {
            mFragmentManager.beginTransaction().remove(overlayFragment).commit();
            mFragmentManager.executePendingTransactions();
        }
    }

    public boolean isOverlayFragmentDisplayed() {
        Fragment overlayFragment = mFragmentManager.findFragmentByTag(OVERLAY_FRAGMENT_TAG);
        return (overlayFragment != null);
    }

    public void showOverOverlayFragment(Fragment fragment) {
        // The overlay fragment does not touch mFragmentItems etc.
        mFragmentManager.beginTransaction().add(mFragmentContainerId, fragment, OVEROVERLAY_FRAGMENT_TAG).commit();
    }

    public void hideOverOverlayFragment() {
        Fragment overoverlayFragment = mFragmentManager.findFragmentByTag(OVEROVERLAY_FRAGMENT_TAG);
        if (overoverlayFragment != null) {
            mFragmentManager.beginTransaction().remove(overoverlayFragment).commit();
            mFragmentManager.executePendingTransactions();
        }
    }

    public boolean isOverOverlayFragmentDisplayed() {
        Fragment overoverlayFragment = mFragmentManager.findFragmentByTag(OVEROVERLAY_FRAGMENT_TAG);
        return (overoverlayFragment != null);
    }

    @Override
    /** Don't call, use {@link #setFragment(int)} */
    public void onClick(View v) {
        if (mChildClickListener != null) {
            mChildClickListener.onClick(v);
        }
        setFragment(v.getId());
    }

    @Override
    public void onBackStackChanged() {
        if (mFragmentManager.getBackStackEntryCount() == 0) {
            storeCurrentFragmentId(mFragmentParentId);
            if (mFragmentSwitchedListener != null) {
                mFragmentSwitchedListener.onFragmentSwitched(this, getCurrentFragment());
            }
        }
    }

}
