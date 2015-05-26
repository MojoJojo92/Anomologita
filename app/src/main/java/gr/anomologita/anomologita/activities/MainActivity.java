package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.FavotitesDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.EndpointGroups;
import gr.anomologita.anomologita.extras.Keys.GetGroupProfileComplete;
import gr.anomologita.anomologita.extras.Keys.ImageEditComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Utils;
import gr.anomologita.anomologita.fragments.MainFragment;
import gr.anomologita.anomologita.fragments.NavFragment;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Favorite;
import gr.anomologita.anomologita.objects.GroupProfile;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;
import me.grantland.widget.AutofitHelper;

public class MainActivity extends ActionBarActivity implements MaterialTabListener, GetGroupProfileComplete, ImageEditComplete, LoginMode {

    final Handler handler = new Handler();
    private ViewPager viewPager;
    private LinearLayout mGroupProfileContainer;
    private Button favoritesButton;
    private ImageView groupImage;
    private MaterialTabHost tabHost;
    private TextView groupNameTV, groupSubs;
    private FloatingActionButton actionButton;
    private ViewPagerAdapter adapter;
    private List<Favorite> Favorites = new ArrayList<>();
    private FavotitesDBHandler db;
    private DrawerLayout drawerLayout;
    private NavFragment fragmentNav;
    private GroupProfile groupProfile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Intent intent =  new Intent(this,Splash.class);
        //  startActivity(intent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new FetchCountTask().execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        // getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_action_a));
        toolbar.setNavigationIcon(R.drawable.ic_action_a);


        mGroupProfileContainer = (LinearLayout) findViewById(R.id.groupProfileContainer);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        fragmentNav = (NavFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        fragmentNav.setUp(R.id.fragment_navigation_drawer, drawerLayout, toolbar);

        tabHost = (MaterialTabHost) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabHost.setSelectedNavigationItem(position);
                mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                HidingGroupProfileListener.mGroupProfileOffset = 0;
            }
        });
        for (int i = 0; i < adapter.getCount(); i++) {
            tabHost.addTab(tabHost.newTab().setText(adapter.getPageTitle(i)).setTabListener(this));
        }
        ImageView actionButtonIcon = new ImageView(this);
        actionButtonIcon.setImageResource(R.drawable.ic_action_abplus);
        actionButton = new FloatingActionButton.Builder(this).setContentView(actionButtonIcon).setBackgroundDrawable(R.drawable.ic_ab_background).build();
        actionButton.setX(-R.dimen.action_button_offset);
        actionButton.setY(20);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CreatePostActivity.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });
        actionButton.setTag(EndpointGroups.ACTION_BUTTON_TAG);

        favoritesButton = (Button) findViewById(R.id.favoritesButton);
        db = new FavotitesDBHandler(this);
        groupImage = (ImageView) findViewById(R.id.groupIcon);
        groupNameTV = (TextView) findViewById(R.id.groupNameProfile);
        groupSubs = (TextView) findViewById(R.id.subs);

        if (db.getFavoriteCount() != 0) Favorites.addAll(db.getAllFavorites());

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesClick(v);
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new FetchCountTask().execute();
                handler.postDelayed(this, 5 * 1000);
            }
        }, 5 * 1000);
        setGroup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem notifications = menu.findItem(R.id.notification_settings);
        MenuItem messages = menu.findItem(R.id.messages_settings);
        LayerDrawable notificationsIcon = (LayerDrawable) notifications.getIcon();
        LayerDrawable messagesIcon = (LayerDrawable) messages.getIcon();

        if (Anomologita.getNotificationBadges() > 0)
            Utils.setBadgeCount(this, notificationsIcon, Anomologita.getNotificationBadges());
        if (Anomologita.getChatBadges() > 0)
            Utils.setBadgeCount(this, messagesIcon, Anomologita.getChatBadges());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.messages_settings) {
            Intent i = new Intent(getApplicationContext(), ConversationsActivity.class);
            i.putExtra("done", "false");
            startActivity(i);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            Anomologita.emptyChatBadges();
            finish();
        } else if (id == R.id.notification_settings) {
            Intent i = new Intent(getApplicationContext(), NotificationActivity.class);
            startActivity(i);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            Anomologita.emptyNotificationBadges();
            finish();
        } else if (id == R.id.me_settings) {
            Intent i = new Intent(getApplicationContext(), MeActivity.class);
            startActivity(i);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
        } else if (id == R.id.search){
            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
            startActivity(i);
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setGroup() {
        if (Anomologita.isConnected()) {
            adapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            tabHost.setSelectedNavigationItem(0);
            drawerLayout.closeDrawers();
            Glide.clear(getWindow().getDecorView().findViewById(android.R.id.content));
            Glide.with(this).load("http://anomologita.gr/img/" + Anomologita.getCurrentGroupID() + ".png")
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(groupImage);
            AutofitHelper.create(groupNameTV);
            groupNameTV.setText(Anomologita.getCurrentGroupName());
            new AttemptLogin(GET_GROUP, Anomologita.getCurrentGroupID(), this).execute();
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(drawerLayout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetGroupComplete(GroupProfile groupProfile) {
        this.groupProfile = groupProfile;
        ImageView editGroup = (ImageView) findViewById(R.id.editMeGroups);
        if (groupProfile != null) {
            if (groupProfile.getGroupName() != null) {
                if (!groupProfile.getGroupName().equals(Anomologita.getCurrentGroupName())) {
                    Anomologita.setCurrentGroupName(groupProfile.getGroupName());
                    db.updateFavorite(groupProfile);
                }
                if (Anomologita.userID.equals(String.valueOf(groupProfile.getUser_id())))
                    editGroup.setVisibility(View.VISIBLE);
                else
                    editGroup.setVisibility(View.INVISIBLE);

                groupSubs.setText(String.valueOf(groupProfile.getSubscribers()));
                if (!db.exists(groupProfile.getGroup_name())) {
                    favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribe_background));
                    favoritesButton.setText("+ Αγαπημένα");
                    favoritesButton.setTextColor(Color.parseColor("#008080"));
                } else {
                    favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribed_background));
                    favoritesButton.setText("Αγαπημένο");
                    favoritesButton.setTextColor(getResources().getColor(R.color.primaryColorLight));
                }
            } else {
                if (db.exists(String.valueOf(groupProfile.getGroup_id()))) {
                    db.deleteFavorite(groupProfile.getGroup_id());
                    Anomologita.setCurrentGroupName(null);
                    Anomologita.setCurrentGroupID(null);
                }
            }
        }
    }

    public void editGroup(View view) {
        if (Anomologita.isConnected()) {
            Intent i = new Intent(this, EditGroupActivity.class);
            i.putExtra("hashtag", groupProfile.getHashtag_name());
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(drawerLayout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΥΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
    }

    public void favoritesClick(View view) {
        if (Anomologita.isConnected()) {
            if (!db.exists(Anomologita.getCurrentGroupName())) {
                favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribed_background));
                favoritesButton.setText("Αγαπημένο");
                favoritesButton.setTextColor(getResources().getColor(R.color.white));
                db.createFavorite(groupProfile);
                Favorites.add(db.getFavorite(Anomologita.getCurrentGroupName()));
                Toast.makeText(getApplicationContext(), Anomologita.getCurrentGroupName() + " has been added to your Contacts!", Toast.LENGTH_SHORT).show();
                AttemptLogin attemptLogin = new AttemptLogin(LoginMode.SET_SUBSCRIBERS, "1", Anomologita.getCurrentGroupName());
                attemptLogin.execute();
                int subs = Integer.parseInt((String) groupSubs.getText());
                subs++;
                groupSubs.setText(String.valueOf(subs));
                fragmentNav.updateDrawer();
                if (groupProfile.getUser_id() != Integer.parseInt(Anomologita.userID)) {
                    String text = "το γκρούπ " + groupProfile.getGroupName() + " έχει " + subs + " ακόλουθους";
                    Log.e("stuff", groupProfile.getRegID());
                    new AttemptLogin(SEND_NOTIFICATION, text, "subscribe", String.valueOf(groupProfile.getGroup_id()), groupProfile.getRegID()).execute();
                }
            } else {
                favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribe_background));
                favoritesButton.setText("+ Αγαπημένα");
                favoritesButton.setTextColor(Color.parseColor("#008080"));
                Favorites.remove(db.getFavorite(Anomologita.getCurrentGroupName()));
                db.deleteFavorite(db.getFavorite(Anomologita.getCurrentGroupName()).getId());
                Toast.makeText(getApplicationContext(), Anomologita.getCurrentGroupName() + " has been unsubscribed", Toast.LENGTH_SHORT).show();
                fragmentNav.updateDrawer();
                AttemptLogin attemptLogin = new AttemptLogin(LoginMode.SET_SUBSCRIBERS, "-1", Anomologita.getCurrentGroupName());
                attemptLogin.execute();
                int subs = Integer.parseInt((String) groupSubs.getText());
                subs -= 1;
                groupSubs.setText(String.valueOf(subs));
            }
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(drawerLayout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΘΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCreateGroupClick(View view) {
        Intent i = new Intent(getApplicationContext(), CreateGroupActivity.class);
        startActivity(i);
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onTabSelected(MaterialTab materialTab) {
        viewPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {
    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {
    }

    @Override
    public void onImageEditComplete(Bitmap imageBitmap) {
        // int color = averageColor(imageBitmap);
        // LinearLayout linearLayout = (LinearLayout) findViewById(R.id.prof);
        //  linearLayout.setBackgroundColor(color);
    }

    public void onDrawerSlide(float drawerOffset) {
        actionButton.setTranslationX((drawerOffset * 720) - 400);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Anomologita.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Anomologita.activityPaused();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                data.getStringExtra("group");
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    class FetchCountTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            invalidateOptionsMenu();
            // example count. This is where you'd
            // query your data store for the actual count.
            return 0;
        }

        @Override
        public void onPostExecute(Integer count) {

            //  updateNotificationsBadge(count);
        }
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        String tabs[];

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.tabs);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case EndpointGroups.NEW:
                    fragment = MainFragment.newInstance("no");
                    break;
                case EndpointGroups.TOP:
                    fragment = MainFragment.newInstance("yes");
                    break;
            }
            return fragment;
        }

        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}