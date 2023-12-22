package com.example.simpledemo.shadows

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import org.robolectric.Shadows
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.Resetter
import org.robolectric.util.reflector.ForType
import org.robolectric.util.reflector.Reflector

@Implements(value = Activity::class, looseSignatures = true)
class ShadowActivity : org.robolectric.shadows.ShadowActivity() {
    var nextActivity: Activity? = null

    @Implementation
    fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        Reflector.reflector(_Activity_::class.java, realActivity)
            .startActivityForResult(intent, requestCode, options)
        val nextActivityScenario = ActivityScenario.launch<Activity>(intent)
        realActivity?.let {
            callingActivities[intent] = it
        }
        nextActivityScenario.onActivity {
            this.nextActivity = it
        }
    }


    @ForType(Activity::class, direct = true)
    // https://robolectric.org/javadoc/4.11/org/robolectric/util/reflector/Reflector.html
    interface _Activity_ {
        fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?)
    }

    @Implementation
    override fun finish() {
        super.finish()
        callingActivities[realActivity?.intent]?.let {
            val shadowCallingActivity = Shadows.shadowOf(it)
            shadowCallingActivity.receiveResult(realActivity?.intent, resultCode, resultIntent)
        }
    }

    companion object {
        private val callingActivities = mutableMapOf<Intent, Activity>()

        @JvmStatic
        @Resetter
        public fun reset() {
            callingActivities.clear()
        }
    }
}