package com.futureme.financialai

import android.app.Application
import com.futureme.shared.domain.FutureMeProduct

class FutureMeApplication : Application() {
    val product by lazy { FutureMeProduct() }
}
