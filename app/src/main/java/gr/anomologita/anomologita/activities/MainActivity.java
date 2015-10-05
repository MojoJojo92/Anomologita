package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.FavoritesDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.EndpointGroups;
import gr.anomologita.anomologita.extras.Keys.GetGroupProfileComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.LocationValet;
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
    private TextView favoritesButtonOn;
    private TextView groupNameTV;
    private TextView groupSubs;
    private TextView title;
    private ImageView groupImage;
    private MaterialTabHost tabHost;
    private FloatingActionButton actionButton;
    private ViewPagerAdapter adapter;
    private FavoritesDBHandler db;
    private DrawerLayout drawerLayout;
    private NavFragment fragmentNav;
    private ImageView editGroup;
    private GroupProfile groupProfile = null;
    private int abPosition, mGroupProfileHeight;
    private LocationValet locationValet;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MMSDK.initialize(this);
        locationValet = new LocationValet(this, new LocationValet.ILocationValetListener() {

            public void onBetterLocationFound(Location userLocation) {
                MMRequest.setUserLocation(userLocation);
            }
        });
        locationValet.startAquire(true);
        setContentView(R.layout.activity_main);

        new FetchCountTask().execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mGroupProfileContainer = (LinearLayout) findViewById(R.id.groupProfileContainer);
        mGroupProfileHeight = (int) (getResources().getDimension(R.dimen.groupProfileHeight) - (getResources().getDimension(R.dimen.titleSize)));
        mGroupProfileContainer.setTranslationY(-mGroupProfileHeight);
        name = (LinearLayout) findViewById(R.id.titleLayout);

        favoritesButton = (TextView) findViewById(R.id.favoritesButton);
        favoritesButtonOn = (TextView) findViewById(R.id.favoritesButtonOn);
        editGroup = (ImageView) findViewById(R.id.shareGroup);
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
                    String theTitle = title.getText().toString();
                    if (!theTitle.equals(getResources().getString(R.string.noInternet)) && !theTitle.equals(getResources().getString(R.string.deleted)) && !theTitle.equals(getResources().getString(R.string.groupName))) {
                        mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                        HidingGroupProfileListener.mGroupProfileOffset = 0;
                        name.setAlpha(0);
                        if (Anomologita.like) {
                            favoritesButtonOn.setVisibility(View.GONE);
                            favoritesButton.setVisibility(View.VISIBLE);
                        } else {
                            favoritesButtonOn.setVisibility(View.VISIBLE);
                            favoritesButton.setVisibility(View.GONE);
                        }
                    }
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
                if (Anomologita.isConnected() && Anomologita.getCurrentGroupID() != null) {
                    Intent i = new Intent(getApplicationContext(), CreatePostActivity.class);
                    startActivityForResult(i, 1);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                } else {
                    Toast.makeText(MainActivity.this, "Πρέπει να επιλέξεις γκρουπ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        actionButton.setTag(EndpointGroups.ACTION_BUTTON_TAG);
        db = new FavoritesDBHandler(this);
        groupImage = (ImageView) findViewById(R.id.icon);
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareGroup();
            }
        });
        TextView share = (TextView) findViewById(R.id.edit);
        groupNameTV = (TextView) findViewById(R.id.groupNameProfile);
        AutofitHelper.create(groupNameTV);
        groupSubs = (TextView) findViewById(R.id.subs);
        title = (TextView) findViewById(R.id.title);
        AutofitHelper.create(title);

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoritesClick();
            }
        });

        favoritesButtonOn.setOnClickListener(new View.OnClickListener() {
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

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareGroup();
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
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e("ok", "onMenuOpened", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            settingsDialog();
        } else if (id == R.id.action_share) {
            shareApp();
        } else if (id == R.id.action_support) {
            supportDialog();
        } else if (id == R.id.action_terms) {
            Intent i = new Intent(getApplicationContext(), TermsActivity.class);
            startActivityForResult(i, 1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
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

    private void shareApp() {
        String message = "https://play.google.com/store/apps/details?id=gr.anomologita.anomologita&hl=en";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(share, "Πες το και στους φίλους σου!"));
    }

    private void shareGroup() {
        drawerLayout.invalidate();
        View view = drawerLayout;
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();
        Bitmap bitmap1 = overlay(bitmap);
        Uri imageUri = getImageUri(bitmap1);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Κοινοποίησε το γκρουπ"));
    }

    private static Bitmap overlay(Bitmap bmp1) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp1, 0, 0, null);
        return bmOverlay;
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "", "");
        return Uri.parse(path);
    }

    private void supportDialog() {
        new MaterialDialog.Builder(this)
                .title("Αναφορά")
                .content(R.string.complaints)
                .positiveText("OK")
                .positiveColorRes(R.color.accentColor)
                .negativeText("ΑΚΥΡΟ")
                .negativeColorRes(R.color.primaryColor)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        support();
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void support() {
        String[] TO = {"anomologita.m2@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Θέμα");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void settingsDialog() {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Ρυθμίσεις")
                .iconRes(R.drawable.ic_setting_light)
                .customView(R.layout.settings_layout, wrapInScrollView)
                .positiveText("OK")
                .positiveColorRes(R.color.accentColor)
                .show();
        Switch switchAll = (Switch) dialog.getView().findViewById(R.id.switch1);
        switchAll.setChecked(Anomologita.areNotificationsOn());
        switchAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    Anomologita.notificationsOn();
                else
                    Anomologita.notificationsOff();
            }
        });
        Switch switchSound = (Switch) dialog.getView().findViewById(R.id.switch2);
        switchSound.setChecked(Anomologita.isNotificationSoundOn());
        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    Anomologita.notificationSoundOn();
                else
                    Anomologita.notificationSoundOff();
            }
        });
    }

    public Menu getMenu() {
        return menu;
    }

    public void setGroup() {
        if (Anomologita.isConnected()) {
            if (Anomologita.getCurrentGroupID() != null) {
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
                if (db.exists(Anomologita.getCurrentGroupName()))
                    db.deleteFavorite(db.getFavorite(Anomologita.getCurrentGroupName()).getId());
                title.setText(R.string.deleted);
                Anomologita.setCurrentGroupName(null);
                Anomologita.setCurrentGroupID(null);
                Anomologita.setCurrentGroupUserID(null);
                fragmentNav.updateDrawer();
                mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                name.setAlpha(1);
            }
        } else {
            title.setText(R.string.noInternet);
            YoYo.with(Techniques.Tada).duration(700).playOn(title);
            Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
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
                    db.updateFavorite(groupProfile);
                    favoritesButtonOn.setVisibility(View.GONE);
                    favoritesButton.setVisibility(View.VISIBLE);
                } else {
                    favoritesButton.setVisibility(View.GONE);
                    favoritesButtonOn.setVisibility(View.VISIBLE);
                }

                mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                HidingGroupProfileListener.mGroupProfileOffset = 0;
                name.setAlpha(0);
            } else {
                title.setText("Ανομολόγητα");
                if (db.exists(Anomologita.getCurrentGroupName())) {
                    db.deleteFavorite(db.getFavorite(Anomologita.getCurrentGroupName()).getId());
                    title.setText(R.string.deleted);
                }
                Anomologita.setCurrentGroupName(null);
                Anomologita.setCurrentGroupID(null);
                Anomologita.setCurrentGroupUserID(null);
                fragmentNav.updateDrawer();
                mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                name.setAlpha(1);
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
                favoritesButton.setVisibility(View.GONE);
                favoritesButtonOn.setVisibility(View.VISIBLE);
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
                favoritesButtonOn.setVisibility(View.GONE);
                favoritesButton.setVisibility(View.VISIBLE);
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
            YoYo.with(Techniques.Tada).duration(700).playOn(title);
            Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
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
            startActivityForResult(i, 3);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(title);
            Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        Anomologita.activityResumed();
        super.onResume();
    }

    @Override
    protected void onPause() {
        locationValet.stopAquire();
        Anomologita.activityPaused();
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //    if (Anomologita.userID.equals(groupProfile.getUser_id()))
    //        Anomologita.setCurrentGroupUserID(groupProfile.getUser_id());
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
        } else if (requestCode == 3) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    fragmentNav.updateDrawer();
                    setGroup();
                    Anomologita.fragmentNew.refresh();
                    Anomologita.fragmentTop.refresh();
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