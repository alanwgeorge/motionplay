package com.example.motionplay.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val buttonLabel = MutableLiveData<String>()
}