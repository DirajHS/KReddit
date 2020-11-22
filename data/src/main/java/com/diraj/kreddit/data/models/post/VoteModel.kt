package com.diraj.kreddit.data.models.post

data class VoteModel(val id: String, val dir: String, val ups: Int ?= null, val likes: Boolean ?= null)