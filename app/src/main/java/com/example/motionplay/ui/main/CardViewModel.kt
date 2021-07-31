package com.example.motionplay.ui.main

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import timber.log.Timber

class CardViewModel(val index: Int) {
    val text = MutableLiveData<String>()
    var dismissOnClick = { view: View, viewModel: CardViewModel ->
        Timber.d("card dismissed: $index")
    }
}