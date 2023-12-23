package com.example.simpledemo.shadows;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;

import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;

import java.util.HashMap;
import java.util.Map;

@Implements(value = Activity.class, looseSignatures = true)
public class ShadowActivity extends org.robolectric.shadows.ShadowActivity {

    /**
     * Map of Intent to activities which launched those intents for result.
     * (The activity called startActivityForResult for the given Intent.)
     */
    private static final Map<Intent, Activity> callingActivitiesForResult = new HashMap<>();
    private ActivityScenario<Activity> nextActivityScenario = null;

    public ShadowActivity() {
    }

    /**
     * Invoke startActivityForResult on the real instance. Then launch the next
     * activity.
     */
    @Implementation
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        Reflector.reflector(_Activity_.class, realActivity)
                .startActivityForResult(intent, requestCode, options);
        // Save the scenario for the next activity, to clean up its resources later.
        nextActivityScenario = ActivityScenario.launchActivityForResult(intent);
        // Keep track of ourselves (the calling activity) as the activity who launched
        // this intent. When the next activity finishes, it will call us with the result.
        callingActivitiesForResult.put(intent, realActivity);
    }

    // https://robolectric.org/javadoc/4.11/org/robolectric/util/reflector/Reflector.html
    @ForType(value = Activity.class, direct = true)
    interface _Activity_ {
        void startActivityForResult(Intent intent, int requestCode, Bundle options);
    }

    @Implementation
    @Override
    public void finish() {
        super.finish();
        // If this activity was launched as a result of startActivityForResult,
        // send the result to the calling activity.
        Activity callingActivity = callingActivitiesForResult.get(realActivity.getIntent());
        if (callingActivity != null) {
            ShadowActivity shadowCallingActivity = (ShadowActivity) Shadows.shadowOf(callingActivity);
            shadowCallingActivity.receiveResult(realActivity.getIntent(), getResultCode(), getResultIntent());
        }
        // If we launched another activity for result, cleanup its scenario
        // to free resources.
        if (nextActivityScenario != null) {
            nextActivityScenario.close();
        }
    }

    @Resetter
    public static void reset() {
        callingActivitiesForResult.clear();
    }
}
