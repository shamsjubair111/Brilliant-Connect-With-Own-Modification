package sdk.chat.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.pmw.tinylog.Logger;

import java.util.List;

import materialsearchview.MaterialSearchView;
import sdk.chat.core.Tab;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.adapters.PagerAdapterTabs;
import sdk.chat.ui.fragments.BaseFragment;
import sdk.chat.ui.interfaces.SearchSupported;

public class MainAppBarActivity extends MainActivity {

    protected PagerAdapterTabs adapter;

    protected Toolbar toolbar;
    protected TabLayout tabLayout;
    protected ViewPager2 viewPager;
    protected RelativeLayout content;
    protected MaterialSearchView searchView;
    protected FrameLayout root;



    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_view_pager;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    protected boolean searchEnabled() {
        return currentTab().fragment instanceof SearchSupported;
    }

    @Override
    protected void search(String text) {
        Fragment fragment = currentTab().fragment;
        if (fragment instanceof SearchSupported) {
            ((SearchSupported) fragment).filter(text);
        }
    }

    @Override
    protected MaterialSearchView searchView() {
        return searchView;
    }

    protected void initViews() {
        super.initViews();

        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        content = findViewById(R.id.content);
        searchView = findViewById(R.id.searchView);
        root = findViewById(R.id.root);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            private static final long DEBOUNCE_DELAY = 250; // Adjust delay time as needed
            private Handler handler = new Handler();
            private Runnable searchRunnable;
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Here you get the query text when the user submits the search query

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Here you can perform actions as the text changes in the search view

                handler.removeCallbacks(searchRunnable);

                // Schedule a new searchRunnable after debounce delay
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Perform search after debounce delay
                        search(newText);
                    }
                };
                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);


                return false;
            }
        });

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        // Only creates the adapter if it wasn't initiated already
        if (adapter == null) {
            adapter = new PagerAdapterTabs(this);
        }



//        final List<Tab> tabs = adapter.getTabs();
//        for (Tab tab : tabs) {
//            tabLayout.addTab(tabLayout.newTab().setText(tab.title));
//        }

        viewPager.setAdapter(adapter);

//        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
//            tab.setText(tabs.get(position).title);
//        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabSelected(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setOffscreenPageLimit(3);

        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tabSelected(tab);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        doOnStart();
    }

    public void doOnStart() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(ChatSDKUI.icons().get(this, ChatSDKUI.icons().user, ChatSDKUI.icons().actionBarIconColor));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLocalNotificationsForTab();
    }

    public void tabSelected(TabLayout.Tab tab) {

        int index = tab.getPosition();

        viewPager.setCurrentItem(index);


        final List<Tab> tabs = adapter.getTabs();

//        Fragment currentFragment = adapter.getTabs().get(index).fragment;
//        if (getSupportActionBar() != null) {
//            if (currentFragment instanceof HasAppbar) {
//                getSupportActionBar().hide();
//            } else {
//                getSupportActionBar().show();
//            }
//        }

        updateLocalNotificationsForTab();

        // We mark the tab as visible. This lets us be more efficient with updates
        // because we only
        for (int i = 0; i < tabs.size(); i++) {
            Fragment fragment = tabs.get(i).fragment;
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) tabs.get(i).fragment).setTabVisibility(i == tab.getPosition());
            }
        }

        searchView.closeSearch();
    }

    public Tab currentTab() {
        return adapter.getTabs().get(viewPager.getCurrentItem());
    }

    public void updateLocalNotificationsForTab() {
        Tab tab = adapter.getTabs().get(tabLayout.getSelectedTabPosition());
        ChatSDK.ui().setLocalNotificationHandler(thread -> showLocalNotificationsForTab(tab.fragment, thread));
    }

    public void clearData() {
        for (Tab t : adapter.getTabs()) {
            if (t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).clearData();
            }
        }
    }

    public void reloadData() {
        for (Tab t : adapter.getTabs()) {
            if (t.fragment instanceof BaseFragment) {
                ((BaseFragment) t.fragment).safeReloadData();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                ChatSDK.ui().startProfileActivity(this, ChatSDK.currentUserID());
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
