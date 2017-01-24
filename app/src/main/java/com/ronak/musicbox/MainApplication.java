package com.ronak.musicbox;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.orm.SugarContext;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by ronak on 01/23/17
 */

public class MainApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(getApplicationContext());
//        TypefaceUtil.overrideFont(getApplicationContext(), "serif", "fonts/font-light.ttf");

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/dosis-regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
