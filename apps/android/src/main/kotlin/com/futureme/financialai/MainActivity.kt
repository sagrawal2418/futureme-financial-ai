package com.futureme.financialai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futureme.financialai.presentation.FutureMeViewModel
import com.futureme.financialai.ui.FutureMeApp
import com.futureme.financialai.ui.theme.FutureMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val product = (application as FutureMeApplication).product
        setContent {
            FutureMeTheme {
                val viewModel: FutureMeViewModel = viewModel(
                    factory = FutureMeViewModel.factory(product),
                )
                FutureMeApp(viewModel)
            }
        }
    }
}
