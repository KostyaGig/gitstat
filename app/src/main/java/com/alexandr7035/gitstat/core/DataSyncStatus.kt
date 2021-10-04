package com.alexandr7035.gitstat.core

enum class DataSyncStatus {
    PENDING_PROFILE,
    PENDING_REPOSITORIES,
    PENDING_CONTRIBUTIONS,
    SUCCESS,
    FAILED_WITH_CACHE,
    FAILED_WITH_NO_CACHE
}