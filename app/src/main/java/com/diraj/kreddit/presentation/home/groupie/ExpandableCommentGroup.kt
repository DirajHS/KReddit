package com.diraj.kreddit.presentation.home.groupie

import com.diraj.kreddit.network.models.CommentsData
import com.xwray.groupie.ExpandableGroup

class ExpandableCommentGroup constructor(
    comment : CommentsData,
    depth : Int = 0) : ExpandableGroup(ExpandableCommentItem(comment, depth)) {

    init {
        for (comm in comment.children!!) {
            add(ExpandableCommentGroup(comm, depth.plus(1)))
        }
    }

}