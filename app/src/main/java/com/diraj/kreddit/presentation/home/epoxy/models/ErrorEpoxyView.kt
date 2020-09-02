package com.diraj.kreddit.presentation.home.epoxy.models

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AfterPropsSet
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.diraj.kreddit.databinding.ErrorLayoutBinding

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ErrorEpoxyView @JvmOverloads constructor(
    c : Context,
    a : AttributeSet? = null,
    defStyleAttr : Int = 0
) : ConstraintLayout(c, a, defStyleAttr) {

    @TextProp
    lateinit var errorMessage: CharSequence

    var retryClickListener: OnClickListener ?= null
        @CallbackProp set

    private val errorLayoutBinding = ErrorLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    @AfterPropsSet
    fun renderError() {
        errorLayoutBinding.tvError.text = errorMessage.toString()
        errorLayoutBinding.root.setOnClickListener(retryClickListener)
    }

}