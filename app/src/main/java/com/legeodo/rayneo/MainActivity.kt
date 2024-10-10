package com.legeodo.rayneo

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.legeodo.rayneo.databinding.ActivityMainBinding
import com.rayneo.arsdk.android.touch.TempleAction
import com.rayneo.arsdk.android.ui.activity.BaseMirrorActivity
import com.rayneo.arsdk.android.ui.toast.FToast
import com.rayneo.arsdk.android.util.FLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseMirrorActivity<ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEvent()
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

}
