package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
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
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.FavoritesDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.EndpointGroups;
import gr.anomologita.anomologita.extras.Keys.GetGroupProfileComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Utils;
import gr.anomologita.anomologita.fragments.MainFragment;
import gr.anomologita.anomologita.fragments.NavFragment;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.GroupProfile;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;
import me.grantland.widget.AutofitHelper;

public class MainActivity extends ActionBarActivity implements MaterialTabListener, GetGroupProfileComplete, LoginMode {

    private final Handler handler = new Handler();
    private ViewPager viewPager;
    private LinearLayout mGroupProfileContainer, name;
    private TextView favoritesButton;
    private ImageView groupImage;
    private MaterialTabHost tabHost;
    private TextView groupNameTV, groupSubs, title, editGroup;
    private FloatingActionButton actionButton;
    private ViewPagerAdapter adapter;
    private FavoritesDBHandler db;
    private DrawerLayout drawerLayout;
    private NavFragment fragmentNav;
    private GroupProfile groupProfile = null;
    private int abPosition;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new FetchCountTask().execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mGroupProfileContainer = (LinearLayout) findViewById(R.id.groupProfileContainer);
        name = (LinearLayout) findViewById(R.id.titleLayout);

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
                if (Anomologita.refresh) {
                    if (position == 0)
                        Anomologita.fragmentNew.refresh();
                    else
                        Anomologita.fragmentTop.refresh();
                    Anomologita.refresh = false;
                }
                if (Anomologita.getCurrentGroupID() != null) {
                    mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    HidingGroupProfileListener.mGroupProfileOffset = 0;
                    name.setAlpha(0);
                }
            }
        });
        for (int i = 0; i < adapter.getCount(); i++) {
            tabHost.addTab(tabHost.newTab().setText(adapter.getPageTitle(i)).setTabListener(this));
        }
        ImageView actionButtonIcon = new ImageView(this);
        actionButtonIcon.setImageResource(R.drawable.ic_action_abplus);
        actionButton = new FloatingActionButton.Builder(this).setContentView(actionButtonIcon).setBackgroundDrawable(R.drawable.ic_ab_background).build();
        FloatingActionButton.LayoutParams params = (FloatingActionButton.LayoutParams) actionButton.getLayoutParams();
        abPosition = -screenWidth() / 2 + actionButton.getLayoutParams().width / 2 + ((FloatingActionButton.LayoutParams) actionButton.getLayoutParams()).rightMargin;
        actionButton.setX(abPosition);
        actionButton.setLayoutParams(params);
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

        favoritesButton = (TextView) findViewById(R.id.favoritesButton);
        db = new FavoritesDBHandler(this);
        groupImage = (ImageView) findViewById(R.id.icon);
        editGroup = (TextView) findViewById(R.id.edit);
        groupNameTV = (TextView) findViewById(R.id.groupNameProfile);
        AutofitHelper.create(groupNameTV);
        groupSubs = (TextView) findViewById(R.id.subs);
        title = (TextView) findViewById(R.id.title);

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesClick();
            }
        });

        editGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGroup();
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
        this.menu = menu;

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
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            Anomologita.emptyChatBadges();
        } else if (id == R.id.notification_settings) {
            Intent i = new Intent(getApplicationContext(), NotificationActivity.class);
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            Anomologita.emptyNotificationBadges();
        } else if (id == R.id.me_settings) {
            Intent i = new Intent(getApplicationContext(), MeActivity.class);
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else if (id == R.id.search) {
            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }
        return super.onOptionsItemSelected(item);
    }

    public Menu getMenu() {
        return menu;
    }

    public void setGroup() {
        if (Anomologita.isConnected()) {
            editGroup.setVisibility(View.INVISIBLE);
            adapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            tabHost.setSelectedNavigationItem(0);
            drawerLayout.closeDrawers();
            Glide.clear(getWindow().getDecorView().findViewById(android.R.id.content));
            Glide.with(this).load("http://anomologita.gr/img/" + Anomologita.getCurrentGroupID() + ".png")
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(groupImage);
            groupNameTV.setText(Anomologita.getCurrentGroupName());
            AttemptLogin getGroup = new AttemptLogin();
            getGroup.getGroup(Anomologita.getCurrentGroupID(), this);
            getGroup.execute();
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(drawerLayout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetGroupComplete(GroupProfile groupProfile) {
        this.groupProfile = groupProfile;
        if (groupProfile != null) {
            if (groupProfile.getGroupName() != null) {
                if (!groupProfile.getGroupName().equals(Anomologita.getCurrentGroupName()))
                    Anomologita.setCurrentGroupName(groupProfile.getGroupName());
                db.updateFavorite(groupProfile);
                fragmentNav.updateDrawer();

                if (Anomologita.userID.equals(String.valueOf(groupProfile.getUser_id()).trim()))
                    editGroup.setVisibility(View.VISIBLE);

                groupSubs.setText(String.valueOf(groupProfile.getSubscribers()));
                groupNameTV.setText(groupProfile.getGroupName());
                title.setText(groupProfile.getGroupName());
                if (!db.exists(groupProfile.getGroup_name())) {
                    db.updateFavorite(groupProfile);
                    favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribe_background));
                    favoritesButton.setText("+ Πρόσθεσε στα Αγαπημένα");
                    favoritesButton.setTextColor(getResources().getColor(R.color.accentColor));
                } else {
                    favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribed_background));
                    favoritesButton.setText("Αγαπημένο");
                    favoritesButton.setTextColor(getResources().getColor(R.color.primaryColorLight));
                }
            } else {
                if (db.exists(Anomologita.getCurrentGroupName()))
                    db.deleteFavorite(db.getFavorite(Anomologita.getCurrentGroupName()).getId());
                Anomologita.setCurrentGroupName(null);
                Anomologita.setCurrentGroupID(null);
                fragmentNav.updateDrawer();
                mGroupProfileContainer.animate().translationY(-Anomologita.convert(120 - 30)).setInterpolator(new AccelerateInterpolator(2)).start();
                name.setAlpha(1);
                title.setText("Ανομολόγητα");
            }
        }
    }

    private int screenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    void favoritesClick() {
        if (Anomologita.isConnected() && Anomologita.getCurrentGroupID() != null) {
            if (!db.exists(Anomologita.getCurrentGroupName())) {
                favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribed_background));
                favoritesButton.setText("Αγαπημένο");
                favoritesButton.setTextColor(getResources().getColor(R.color.white));
                db.createFavorite(groupProfile);
                Toast.makeText(getApplicationContext(), "Το " + Anomologita.getCurrentGroupName() + " έχει προστεθεί στα αγαπημένα", Toast.LENGTH_SHORT).show();
                AttemptLogin setSubs = new AttemptLogin();
                setSubs.setSubs("1", Anomologita.getCurrentGroupName());
                setSubs.execute();
                int subs = Integer.parseInt((String) groupSubs.getText());
                subs++;
                groupSubs.setText(String.valueOf(subs));
                fragmentNav.updateDrawer();
                if (!groupProfile.getUser_id().equals(Anomologita.userID)) {
                    String text;
                    if (subs == 1)
                        text = "Το γκρούπ " + groupProfile.getGroupName() + " πλέον αρέσει σε " + subs + " άτομο";
                    else
                        text = "Το γκρούπ " + groupProfile.getGroupName() + " πλέον αρέσει σε " + subs + " άτομα";
                    AttemptLogin sendNotification = new AttemptLogin();
                    sendNotification.sendNotification(text, "subscribe", String.valueOf(groupProfile.getGroup_id()), groupProfile.getRegID());
                    sendNotification.execute();
                }
            } else {
                favoritesButton.setBackground(getResources().getDrawable(R.drawable.subscribe_background));
                favoritesButton.setText("+ Πρόσθεσε στα Αγαπημένα");
                favoritesButton.setTextColor(getResources().getColor(R.color.accentColor));
                db.deleteFavorite(db.getFavorite(Anomologita.getCurrentGroupName()).getId());
                Toast.makeText(getApplicationContext(), "Το " + Anomologita.getCurrentGroupName() + " έχει διαγραφεί από τα αγαπημένα", Toast.LENGTH_SHORT).show();
                fragmentNav.updateDrawer();
                AttemptLogin setSubs = new AttemptLogin();
                setSubs.setSubs("-1", Anomologita.getCurrentGroupName());
                setSubs.execute();
                int subs = Integer.parseInt((String) groupSubs.getText());
                subs -= 1;
                groupSubs.setText(String.valueOf(subs));
            }
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(drawerLayout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
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

    public void onDrawerSlide(float drawerOffset) {
        actionButton.setTranslationX((drawerOffset * screenWidth()) + abPosition);
    }

    private void editGroup() {
        if (Anomologita.isConnected()) {
            Intent i = new Intent(this, EditGroupActivity.class);
            i.putExtra("hashtag", groupProfile.getHashtag_name());
            i.putExtra("name", groupProfile.getGroupName());
            i.putExtra("id", String.valueOf(groupProfile.getGroup_id()));
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(drawerLayout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΥΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setGroup();
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        } else if (requestCode == 2) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    fragmentNav.updateDrawer();
                    setGroup();
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private class FetchCountTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            invalidateOptionsMenu();
            return 0;
        }
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        final String tabs[];

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
                    Anomologita.fragmentNew = (MainFragment) fragment;
                    return fragment;
                case EndpointGroups.TOP:
                    fragment = MainFragment.newInstance("yes");
                    Anomologita.fragmentTop = (MainFragment) fragment;
                    return fragment;
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