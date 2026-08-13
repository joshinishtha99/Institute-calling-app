package com.institute.calling.data.phone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Places a real phone call and reports its real start/end times by listening to
 * the device call state.
 *
 * How it works: we start ACTION_CALL (needs CALL_PHONE), then listen to call
 * state (needs READ_PHONE_STATE). The call going OFF_HOOK marks the start; the
 * return to IDLE marks the end. Callbacks fire on the main thread.
 *
 * DEVICE CAVEAT: after a call ends, most phones return to the app that launched
 * the call (this app), so the disposition screen appears. A few OEMs behave
 * differently; if so, reopening the app from recents still shows the disposition
 * because the state is held here.
 */
@Singleton
class CallController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val telephony =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    /** Called when the call becomes active (OFF_HOOK). */
    var onCallStarted: ((startMillis: Long) -> Unit)? = null

    /** Called when the call ends (returns to IDLE after having been active). */
    var onCallEnded: ((startMillis: Long, endMillis: Long) -> Unit)? = null

    private var sawOffHook = false
    private var startMillis = 0L

    @RequiresApi(Build.VERSION_CODES.S)
    private var telephonyCallback: TelephonyCallback? = null
    private var legacyListener: PhoneStateListener? = null

    /** Places the call. Permissions (CALL_PHONE, READ_PHONE_STATE) must already be granted. */
    fun placeCall(rawNumber: String) {
        sawOffHook = false
        startMillis = 0L
        val cleaned = rawNumber.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleaned"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        register()
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            // CALL_PHONE not granted; end immediately with a zero-length call.
            unregister()
            val now = System.currentTimeMillis()
            onCallEnded?.invoke(now, now)
        }
    }

    private fun handleState(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (!sawOffHook) {
                    sawOffHook = true
                    startMillis = System.currentTimeMillis()
                    onCallStarted?.invoke(startMillis)
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                if (sawOffHook) {
                    onCallEnded?.invoke(startMillis, System.currentTimeMillis())
                    unregister()
                    sawOffHook = false
                }
                // An IDLE before OFF_HOOK is the pre-call state — ignore it.
            }
        }
    }

    private fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) = handleState(state)
            }
            telephonyCallback = cb
            try {
                telephony.registerTelephonyCallback(context.mainExecutor, cb)
            } catch (e: SecurityException) {
                // READ_PHONE_STATE missing — no state tracking; the manual End button still works.
            }
        } else {
            val listener = object : PhoneStateListener() {
                @Deprecated("Deprecated in Java")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) = handleState(state)
            }
            legacyListener = listener
            @Suppress("DEPRECATION")
            telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun unregister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { telephony.unregisterTelephonyCallback(it) }
            telephonyCallback = null
        } else {
            legacyListener?.let {
                @Suppress("DEPRECATION")
                telephony.listen(it, PhoneStateListener.LISTEN_NONE)
            }
            legacyListener = null
        }
    }
}
