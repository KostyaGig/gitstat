package com.alexandr7035.gitstat.data.remote

data class ReposSearchModel(
    val total_count: Long,
    val incomplete_results: Boolean,
    val items: List<RepositoryModel>
)