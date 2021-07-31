package com.example.motionplay.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.motionplay.R
import com.example.motionplay.databinding.CardBinding
import com.example.motionplay.databinding.Play1FragmentBinding
import kotlin.random.Random

class Play1Fragment : Fragment(R.layout.play1_fragment) {
    companion object {
        fun newInstance() = Play1Fragment()
        val defaultMarge = 16.dp
    }

    private val allPossibleCardBindings by lazy {
        listOf(binding.view1, binding.view2, binding.view3, binding.view4, binding.view5, binding.view6, binding.view7, binding.view8)
    }

    private val cardBindings =  MutableLiveData<List<CardBinding>>()
    private val cardViews = cardBindings.map { it.map { binding -> binding.root } }

    private lateinit var binding: Play1FragmentBinding

    private val numberOfCards = Random.nextInt(1, 8)

    private val dismissCardOnClick = { view: View, cardViewModel: CardViewModel ->
        TransitionManager.beginDelayedTransition(binding.motion, AutoTransition())

        binding.motion.removeView(view)

        cardBindings.value?.let {
            val newList = it.toMutableList().apply {  removeAt(cardViewModel.index) }
            cardBindings.postValue(newList)
        }
        Unit
    }

    private val updateCollapseState = Observer<List<View>> { views ->
        val cs = binding.motion.getConstraintSet(R.id.collapsed)

        views.forEachIndexed { index, textView ->
            cs.clear(textView.id)
            cs.constrainHeight(textView.id, 75.dp)
            cs.constrainWidth(textView.id, MATCH_CONSTRAINT)

            val offSet = when (index) {
                1 -> 10.dp
                2 -> 20.dp
                else -> 0
            }

            cs.connect(textView.id, START, PARENT_ID, START, defaultMarge + offSet)
            cs.connect(textView.id, END, PARENT_ID, END, defaultMarge + offSet)
            cs.connect(textView.id, TOP, PARENT_ID, TOP, defaultMarge + offSet)
        }

        if (views.isNotEmpty()) {
            val buttonBottomIndex = views[views.lastIndex.coerceAtMost( 2)].id
            cs.connect(binding.button.id, TOP, buttonBottomIndex, BOTTOM, defaultMarge / 2)
        }

        binding.motion.updateState(R.id.collapsed, cs)
    }

    private val updateExpandedState = Observer<List<View>> { views ->
        val cs = binding.motion.getConstraintSet(R.id.expanded)

        views.forEachIndexed { index, textView ->
            cs.clear(textView.id)
            cs.constrainHeight(textView.id, WRAP_CONTENT)
            cs.constrainWidth(textView.id, MATCH_CONSTRAINT)

            cs.connect(textView.id, START, PARENT_ID, START, defaultMarge)
            cs.connect(textView.id, END, PARENT_ID, END, defaultMarge)
            if (index == 0) {
                cs.connect(textView.id, TOP, PARENT_ID, TOP, defaultMarge)
            } else {
                cs.connect(textView.id, TOP, views[index - 1].id, BOTTOM, defaultMarge)
            }
        }

        views.lastOrNull()?.let {
            cs.connect(binding.button.id, TOP, it.id, BOTTOM, defaultMarge / 2)
            cs.setRotationX(binding.button.id, 180f)
        } ?: run {
            cs.setVisibility(binding.button.id, View.GONE)
        }

        binding.motion.updateState(R.id.expanded, cs)
    }

    private val updateCardsTransitionListener by lazy {
        object : TransitionLoggingListener(resources) {
            override fun onTransitionChange(layout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
//                super.onTransitionChange(layout, startId, endId, progress)
                if (endId == R.id.expanded) {
                    cardBindings.value?.forEach { card ->
                        card.card.progress = progress
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        binding = Play1FragmentBinding.bind(view)

        val (keep, delete) = allPossibleCardBindings
            .withIndex()
            .partition {
                it.index < numberOfCards
            }

        cardBindings.postValue(keep.map { it.value })

        delete.forEach {
            binding.motion.removeView(it.value.root)
        }

        binding.motion.setTransitionListener(updateCardsTransitionListener)

        binding.button.setOnClickListener {
            animate()
        }

        cardBindings.observe(viewLifecycleOwner) {
            it.forEachIndexed { index, cardBinding ->
                cardBinding.viewModel = CardViewModel(index).apply {
                    text.value = resources.getString(R.string.lorem_ipsum)
                    dismissOnClick = dismissCardOnClick
                }
            }
        }

        cardViews.observe(viewLifecycleOwner, updateCollapseState)
        cardViews.observe(viewLifecycleOwner, updateExpandedState)

        binding.motion.setTransition(R.id.start_to_collapsed)
        binding.motion.transitionToStart()
        binding.root.postDelayed( {
            binding.motion.transitionToEnd()
        }, 1500)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.play1, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_main_reset -> {
                requireActivity().recreate()
                true
            }
            else -> false
        }

    private fun animate() {
        binding.motion.setTransition(R.id.collapsed_to_expanded)
        if (binding.motion.currentState == R.id.expanded) {
            binding.motion.transitionToStart()
        } else {
            binding.motion.transitionToEnd()
        }
    }
}

val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics).toInt()