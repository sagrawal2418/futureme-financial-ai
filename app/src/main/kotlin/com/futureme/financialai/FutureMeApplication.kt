package com.futureme.financialai

import android.app.Application
import com.futureme.financialai.data.AppContainer

class FutureMeApplication : Application() {
    val container by lazy { AppContainer() }

    // TODO(iOS/Web): Promote domain models and use cases to Kotlin Multiplatform
    // when native iOS and web clients are added.
}
