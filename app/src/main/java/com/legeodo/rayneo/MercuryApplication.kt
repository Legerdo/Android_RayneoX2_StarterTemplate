package com.legeodo.rayneo

import android.app.Application
import com.rayneo.arsdk.android.MercurySDK

class MercuryApplication: Application()
{
    override fun onCreate()
    {
        super.onCreate()
        MercurySDK.init(this)
    }
}