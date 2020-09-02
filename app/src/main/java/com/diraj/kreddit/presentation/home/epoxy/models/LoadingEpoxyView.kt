package com.diraj.kreddit.presentation.home.epoxy.models

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelView
import com.diraj.kreddit.databinding.LoadingLayoutBinding

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class LoadingEpoxyView @JvmOverloads constructor(
    c : Context,
    a : AttributeSet? = null,
    defStyleAttr : Int = 0
) : ConstraintLayout(c, a, defStyleAttr) {

    init {
        LoadingLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    }

}