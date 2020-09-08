package com.diraj.kreddit.presentation.home.groupie

import androidx.lifecycle.LifecycleOwner
import com.diraj.kreddit.network.models.CommentsData
import com.diraj.kreddit.presentation.home.viewmodel.SharedViewModel
import com.xwray.groupie.ExpandableGroup

class ExpandableCommentGroup constructor(
    comment : CommentsData,
    depth : Int = 0,
    sharedViewModel: SharedViewModel,
    viewLifecycleOwner: LifecycleOwner) : ExpandableGroup(ExpandableCommentItem(comment, depth, sharedViewModel, viewLifecycleOwner)) {

    init {
        for (comm in comment.children!!) {
            add(ExpandableCommentGroup(comm, depth.plus(1), sharedViewModel, viewLifecycleOwner))
        }
    }
}
