package com.ronak.musicbox;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.ronak.musicbox.flags.Flags;
import com.ronak.musicbox.flags.Url_format;
import com.ronak.musicbox.fragment.AboutUs;
import com.ronak.musicbox.fragment.Favourite;
import com.ronak.musicbox.fragment.Home;
import com.ronak.musicbox.fragment.SearchFragment;
import com.ronak.musicbox.fragment.TopListFragment;
import com.ronak.musicbox.model.CurrentStation;
import com.ronak.musicbox.model.SearchStation;
import com.ronak.musicbox.model.Station;
import com.ronak.musicbox.model.StationAddedManually;
import com.ronak.musicbox.utilities.DownloadSongDetailAndPlayOnClick;
import com.ronak.musicbox.utilities.IcyURLStreamHandler;

public class MainActivity extends AppCompatActivity
        implements OnChangePlayerState, ViewPager.OnPageChangeListener {
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private static final String TAG = "JUKEBOX";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 14;
    private static int COUNT_EXIT = 0;
    PagerAdapter adapter;
    //   private static ExoPlayer player;
    private Context context;
    private MyService myServiceEngine;
    private Fragment fragment;
    private SharedPreferences prefrences;
    private ServiceConnection connectionService;
    private ViewPager viewPager;
    private ImageButton imageButtonPlayStop;
    private TextView textViewTitle;
    private TextView textViewText;
    private ImageView imageViewLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        prefrences = getSharedPreferences(Flags.SETTINGS, MODE_PRIVATE);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new ScreenSliderPagerFragment(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(this);

        imageButtonPlayStop = (ImageButton) findViewById(R.id.but_media_play);
        textViewTitle = (TextView) findViewById(R.id.strip_title);
        textViewText = (TextView) findViewById(R.id.strip_text);
        imageViewLogo = (ImageView) findViewById(R.id.strip_logo);


        this.askForRuntimePermissions();
        prepareDatabase();


        prepareServiceAndPlayer();
//	  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//	  fab.setOnClickListener(new View.OnClickListener() {
//		 @Override
//		 public void onClick(View view) {
////			prepare();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Intent intent = new Intent(context, MyService.class);
        bindService(intent, connectionService, BIND_AUTO_CREATE);


        this.onCreatePlayer();

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, null, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }

    private void prepareServiceAndPlayer() {
        connectionService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MyService.ServiceBinder servicBinder = (MyService.ServiceBinder) iBinder;
                myServiceEngine = servicBinder.getService();
                myServiceEngine.setOnChangePlayerStateListener(MainActivity.this);
                Log.e(TAG, "SERVICE CONNECTED");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.e(TAG, "SERVICE DISCONNECTED");
            }
        };

        context = this;
        try {
            java.net.URL.setURLStreamHandlerFactory(new java.net.URLStreamHandlerFactory() {
                public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
                    Log.d("LOG", "Asking for stream handler for protocol: '" + protocol + "'");
                    if ("icy".equals(protocol))
                        return new IcyURLStreamHandler();
                    return null;
                }
            });
        } catch (Throwable t) {
            Log.w("LOG", "Cannot set the ICY URLStreamHandler - maybe already set ? - " + t);
        }
    }

    private void askForRuntimePermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                Log.e(TAG, "Should show record Audio permission");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    private void prepareDatabase() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            /**
             * This block of code is for fixing the SugarORM bug. Related to SDK VERSION
             * tables not found
             */
            long station_id = Station.save(new Station());
            long current_station = CurrentStation.save(new CurrentStation());
            long search_station = SearchStation.save(new SearchStation());
            long station_added_manually_id = StationAddedManually.save(new StationAddedManually());

            try {
                Station station = Station.findById(Station.class, station_id);
                Station.delete(station);
                CurrentStation currentStation = CurrentStation.findById(CurrentStation.class, current_station);
                CurrentStation.delete(currentStation);
                SearchStation searchStation = SearchStation.findById(SearchStation.class, search_station);
                SearchStation.delete(searchStation);
                StationAddedManually stationAddedManually = StationAddedManually.findById(StationAddedManually.class, station_added_manually_id);
                StationAddedManually.delete(stationAddedManually);

            } catch (Exception ignored) {
                Log.e("Exception Raisd", "EX : " + ignored.getMessage());
            }
        } else {
            Station.findById(Station.class, (long) 1);
            CurrentStation.findById(CurrentStation.class, (long) 1);
            SearchStation.findById(SearchStation.class, (long) 1);
            StationAddedManually.findById(StationAddedManually.class, (long) 1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        //noinspection SimplifiableIfStatement
        FragmentTransaction trasaction = fragmentManager.beginTransaction();

        if (id == R.id.action_search) {

            viewPager.setCurrentItem(0);

            return true;
        } else if (id == R.id.action_favourite) {

            viewPager.setCurrentItem(2);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (COUNT_EXIT == 0) {
            COUNT_EXIT++;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getCurrentFocus(), "Press again to exit", Snackbar.LENGTH_SHORT)
                                        .setAction("Minimize", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i = new Intent(Intent.ACTION_MAIN);
                                                i.addCategory(Intent.CATEGORY_HOME);
                                                startActivity(i);
                                            }
                                        })
                                        .setActionTextColor(getResources().getColor(R.color.colorPremiere))
                                        .show();

                            }
                        });
                        Thread.sleep(1500);
                        COUNT_EXIT = 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            finish();
        }
    }

    public void playpause(View view) {
        ImageButton imageView = (ImageButton) view;

        if (myServiceEngine != null) {
            if (myServiceEngine.isPlaying()) {
                myServiceEngine.stop();
                imageView.setEnabled(false);
                imageView.setImageResource(android.R.drawable.ic_media_play);
            } else {
                /**
                 * If there is no song playing
                 */
                CurrentStation currentStation = CurrentStation.getCurrentStation();
                if (currentStation != null) {
                    DownloadSongDetailAndPlayOnClick downloadSongDetailAndPlayOnClick = new DownloadSongDetailAndPlayOnClick(currentStation.getStation(), this);
                    downloadSongDetailAndPlayOnClick.execute();
                }
            }
        } else {
            Log.e(TAG, "my serviceEngine is null on playpause.");
            prepareServiceAndPlayer();
        }
    }


    public void searchstation(View view) {
        EditText editText = (EditText) findViewById(R.id.search_text_station);

        //Hiding IME
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


        String urlToSearch = new Url_format().getStationByKeywords(Flags.DEV_ID,
                editText.getText().toString(), "0", "50", null, null);

        viewPager.setCurrentItem(2);


    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(connectionService);
        } catch (Exception ex) {
            Log.e("error", "connection error");
        }
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

        RotateAnimation rotateAnimation;

        rotateAnimation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        if (imageButtonPlayStop == null) return;
        switch (playbackState) {
            case ExoPlayer.STATE_READY:
                Log.e(TAG, "State Ready");
                imageButtonPlayStop.startAnimation(rotateAnimation);
                rotateAnimation.cancel();
                rotateAnimation.reset();
                imageButtonPlayStop.setEnabled(true);
                imageButtonPlayStop.setImageResource(R.drawable.ic_stop);

                fragment = new Home();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.container_fragment, fragment, Home.TITLE).commit();


                Home home = (Home) this.getSupportFragmentManager().findFragmentByTag(Home.TITLE);
                if (home != null) {
                    home.initAudio();
                }


                break;
            case ExoPlayer.STATE_BUFFERING:
                imageButtonPlayStop.setEnabled(false);
                imageButtonPlayStop.setImageResource(R.drawable.ic_buffering);
                imageButtonPlayStop.setAnimation(rotateAnimation);
                Snackbar.make((View) imageButtonPlayStop.getParent(), "Buffering...", Snackbar.LENGTH_SHORT).show();
//                rotateAnimation.start();
                imageButtonPlayStop.startAnimation(rotateAnimation);

                Log.e(TAG, "State Buffering");
                break;
            case ExoPlayer.STATE_ENDED:
                rotateAnimation.cancel();
                imageButtonPlayStop.setEnabled(true);
                imageButtonPlayStop.setImageResource(R.drawable.ic_play);
                Log.e(TAG, "State Ended");
                break;
            case ExoPlayer.STATE_IDLE:
                rotateAnimation.cancel();
                imageButtonPlayStop.setEnabled(true);
                imageButtonPlayStop.setImageResource(R.drawable.ic_play);
                Log.e(TAG, "State Idle");
                break;
            case ExoPlayer.STATE_PREPARING:
                refreshCurrentSong();
                Snackbar.make((View) imageButtonPlayStop.getParent(), "Preparing...", Snackbar.LENGTH_SHORT).show();
                break;
            default:
                Log.e(TAG, "Default Unknown state");
                break;
        }
//        Log.e(TAG, "PLayer state changed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.refreshCurrentSong();
    }

    private void refreshCurrentSong() {

        CurrentStation currentStation = CurrentStation.build();

        if (currentStation != null) {
            textViewTitle.setText(currentStation.getName());
            textViewText.setText(currentStation.getCtqueryString());
//            if (currentStation.getLogo() != null) {
            try {
                Glide.with(this)
                        .load(currentStation.getLogo())
                        .error(R.drawable.music)
                        .into(imageViewLogo);
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, ex.getMessage() + "");
            }
//            }

            imageButtonPlayStop.setImageResource(R.drawable.ic_preparing);
            Log.e(TAG, "State Preparing");
        }
    }

    private void onCreatePlayer() {

        Station currentStation = Station.getStationRandom();

        if (currentStation != null) {
            textViewTitle.setText(currentStation.getName());
            textViewText.setText(currentStation.getCtqueryString());
//            if (currentStation.getLogo() != null) {
            Glide.with(this)
                    .load(currentStation.getLogo())
                    .error(R.drawable.music)
                    .into(imageViewLogo);
//            }

            imageButtonPlayStop.setImageResource(R.drawable.ic_idle);
            Log.e(TAG, "State Preparing");
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, "Player Error : " + error.getMessage());
    }

    public void changeVisualization(View view) {
        ((VisualizerView) view).changeTypeEqualizer();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d("Page Scrolled", "position" + String.valueOf(position));
    }

    @Override
    public void onPageSelected(int position) {
        Log.d("Page Selected", "position" + String.valueOf(position));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d("Page State", "state " + String.valueOf(state) + String.valueOf(ViewPager.SCROLL_STATE_IDLE));
        if (viewPager.getCurrentItem() == 2 && state == ViewPager.SCROLL_STATE_IDLE) {
            Favourite page = (Favourite) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
            page.onUpdateUI();
        } else if (viewPager.getCurrentItem() == 1 && state == ViewPager.SCROLL_STATE_IDLE) {
            Home page = (Home) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
            page.onUpdateUI();
        }
    }

    public void openHome(View view) {
        if (viewPager != null) {
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    Exo.getPlayer().stop();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Exo.getPlayer().stop();
                    //A call is dialing, active or on hold
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private class ScreenSliderPagerFragment extends FragmentPagerAdapter {

        private int NUMBER_PAGES = 5;

        public ScreenSliderPagerFragment(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new SearchFragment();
                    break;
                case 1:
                    fragment = new Home();
                    break;
                case 2:
                    fragment = new Favourite();
                    break;
                case 3:
                    fragment = new TopListFragment();
                    break;
                case 4:
                    fragment = new AboutUs();
                    break;
                default:
                    fragment = new Home();
            }
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "SEARCH";
                case 1:
                    return "HOME";
//                case 2:
//                    return "CURRENT LIST";
                case 2:
                    return "FAVOURITE";
                case 3:
                    return "TOP CHANNELS";
                case 4:
                    return "ABOUT US";
                default:
                    return "HOME";

            }

        }

        @Override
        public int getCount() {
            return NUMBER_PAGES;
        }
    }
}