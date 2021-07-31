package com.example.motionplay.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.motionplay.R
import com.example.motionplay.databinding.MainFragmentBinding
import timber.log.Timber

class MainFragment : Fragment(R.layout.main_fragment) {
    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: MainFragmentBinding

    private val buttonOnClick: (View) -> Unit = { _ ->
        with (binding) {
            if (button.text != "up") {
                motion.transitionToStart()
                this@MainFragment.viewModel.buttonLabel.postValue("up")
            } else {
                motion.transitionToEnd()
                this@MainFragment.viewModel.buttonLabel.postValue("down")
            }
        }
    }

    private val viewOnClick: (View) -> Unit = ::zoomView

    private val transitionListener = object: TransitionLoggingListener(resources) {
        override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            super.onTransitionCompleted(p0, p1)
            viewModel.buttonLabel.postValue(if (p1 == R.id.start) "up" else "down")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = MainFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@MainFragment.viewModel
        }

        viewModel.buttonLabel.postValue("up")
        binding.button.setOnClickListener(buttonOnClick)
        binding.view1.setOnClickListener(viewOnClick)
        binding.view2.setOnClickListener(viewOnClick)
        binding.view3.setOnClickListener(viewOnClick)

        binding.motion.addTransitionListener(transitionListener)
    }

    private fun zoomView(view: View) {
        val currentState = binding.motion.currentState

        val cs = binding.motion.getConstraintSet(currentState)

        cs.constrainHeight(view.id, binding.root.height)
        cs.constrainWidth(view.id, binding.root.width)

        cs.connect(view.id, START, binding.root.id, START, 0)
        cs.connect(view.id, TOP, binding.root.id, TOP, 0)
        cs.connect(view.id, END, binding.root.id, END, 0)
        cs.connect(view.id, BOTTOM, binding.root.id, BOTTOM, 0)

        cs.setElevation(view.id, 1f)

        binding.motion.updateState(currentState, cs)
        binding.motion.transitionToState(currentState)
    }
}



open class TransitionLoggingListener(val resources: Resources) : MotionLayout.TransitionListener {
    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
        Timber.d("onTransitionStarted: start:${resources.getResourceName(p1)} end:${resources.getResourceName(p2)}")
    }

    override fun onTransitionChange(layout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
        Timber.d("onTransitionChange: start:${resources.getResourceName(startId)} end:${resources.getResourceName(endId)} progress:$progress")
    }

    override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
        Timber.d("onTransitionCompleted:${resources.getResourceName(p1)}")
    }

    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
        Timber.d("onTransitionTrigger:${resources.getResourceName(p1)}")
    }
}
