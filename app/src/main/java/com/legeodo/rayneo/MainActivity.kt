package com.legeodo.rayneo

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalconxr.mercury.ipc.helpers.RingIPCHelper
import com.legeodo.rayneo.databinding.ActivityMainBinding
import com.rayneo.arsdk.android.touch.TempleAction
import com.rayneo.arsdk.android.ui.activity.BaseMirrorActivity
import com.rayneo.arsdk.android.ui.toast.FToast
import com.rayneo.arsdk.android.util.FLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

import com.ffalconxr.mercury.ipc.Launcher
import com.ffalconxr.mercury.ipc.Launcher.OnResponseListener
import com.ffalconxr.mercury.ipc.helpers.GPSIPCHelper

class MainActivity : BaseMirrorActivity<ActivityMainBinding>() {

    companion object
    {
        private const val TAG: String = "MainActivity"
    }

    private var mLauncher: Launcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLauncher = Launcher.getInstance(this)
        mLauncher?.enableLog()
        mLauncher?.addOnResponseListener(response)

        GPSIPCHelper.registerGPSInfo(this)

        initEvent()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        GPSIPCHelper.unRegisterGPSInfo(this)
        mLauncher!!.removeOnResponseListener(response)
        mLauncher!!.disConnect()
    }

    private fun initEvent()
    {
        var isWaitingForDoubleClick = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect {
                    when (it) {
                        is TempleAction.DoubleClick -> {
                            if (isWaitingForDoubleClick) {
                                // 두 번 클릭된 상태이므로 finish()를 호출하여 종료합니다.
                                finish()
                            } else {
                                // 첫 번째 클릭일 때 팝업을 띄웁니다.
                                FToast.show("두 번 클릭하면 종료됩니다", short = true, yOffset = 350)
                                isWaitingForDoubleClick = true

                                // 일정 시간 후에 isWaitingForDoubleClick를 false로 설정합니다.
                                lifecycleScope.launch {
                                    delay(2000)  // 2초 후에 초기화
                                    isWaitingForDoubleClick = false
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private val response =
        OnResponseListener { response ->
            if (response?.getData() == null) return@OnResponseListener
            try
            {
                val jo = JSONObject(response.getData())
                if (jo.has("datatype") && jo.getString("datatype") == "ring_info")
                {
                    val isRingConnected = jo.getBoolean("ring_connected")
                    val imuStatus =
                        jo.getInt("ring_imu_status") //-1: Ring is not connected, 0: Ring is not IMU enabled, 1: Ring is IMU enabled

                    if (isRingConnected)
                    { //Ring connection
                        Log.i(MainActivity.TAG, "Ring Connected ")
                        if (imuStatus != 1)
                        { //IMU is not turned on
                            Log.i(MainActivity.TAG, "The ring is connected, but imu is not turned on")
                            RingIPCHelper.setRingIMU(this@MainActivity, true) //Turn on the IMU
                        }
                    } else
                    {
                        Log.i(MainActivity.TAG, "Ring not connected ")
                    }
                } else if (jo.has("datatype") && jo.getString("datatype") == "ring_quaternion")
                {
                    //Get quaternion
                    val w = jo.getDouble("w")
                    val x = jo.getDouble("x")
                    val y = jo.getDouble("y")
                    val z = jo.getDouble("z")
                    Log.i(MainActivity.TAG, "Ring quaternion: w=$w x=$x y=$y z=$z")
                } else if (jo.has("datatype") && jo.getString("datatype") == "gps_push")
                {
                    val resultCode =
                        jo.getInt("resultCode") //0 Mobile phone is connected 1 Mobile phone is not connected, Mobile phone is not connected and sent 1, user connects and then re-registers for streaming 2. Streaming timeout, please check the mobile phone connection and try again
                    val resultMessage = jo.getString("resultMessage")
                    Log.i(
                        MainActivity.TAG,
                        "gps_push: resultCode=$resultCode    resultMessage:$resultMessage"
                    )
                } else if (jo.has("mLatitude") && jo.has("mLongitude") && jo.has("mAltitude"))
                { //GPS data, previously transmitted with navigation without encapsulation datatype, compatible with previous versions, no repackaging datatype
                    val mProvider = jo.getString("mProvider")
                    val mTime = jo.getLong("mTime")
                    val mElapsedRealtimeNanos = jo.getLong("mElapsedRealtimeNanos")
                    val mLatitude = jo.getDouble("mLatitude")
                    val mLongitude = jo.getDouble("mLongitude")
                    val mAltitude = jo.getDouble("mAltitude")
                    val mSpeed = jo.getDouble("mSpeed")
                    val mBearing = jo.getDouble("mBearing")
                    val mHorizontalAccuracyMeters = jo.getDouble("mHorizontalAccuracyMeters")
                    val mVerticalAccuracyMeters = jo.getDouble("mVerticalAccuracyMeters")
                    val mSpeedAccuracyMetersPerSecond =
                        jo.getDouble("mSpeedAccuracyMetersPerSecond")
                    val mBearingAccuracyDegrees = jo.getDouble("mBearingAccuracyDegrees")
                    Log.i(
                        MainActivity.TAG,
                        "======  mProvider:" + mProvider + "  mTime:" + mTime + "  mElapsedRealtimeNanos:" + mElapsedRealtimeNanos
                                + "  mLatitude:" + mLatitude + "  mLongitude:" + mLongitude + "  mAltitude:" + mAltitude + "  mSpeed:" + mSpeed
                                + "  mBearing:" + mBearing + "  mHorizontalAccuracyMeters:" + mHorizontalAccuracyMeters + "  mVerticalAccuracyMeters:" + mVerticalAccuracyMeters
                                + "  mSpeedAccuracyMetersPerSecond:" + mSpeedAccuracyMetersPerSecond + "  mBearingAccuracyDegrees:" + mBearingAccuracyDegrees + "   ======"
                    )
                }
            } catch (e: JSONException)
            {
                e.printStackTrace()
            }
        }

}
