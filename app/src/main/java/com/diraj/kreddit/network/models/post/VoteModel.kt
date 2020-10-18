package com.diraj.kreddit.network.models.post

data class VoteModel(val id: String, val dir: String, val ups: Int ?= null, val likes: Boolean ?= null)