package com.alexandr7035.gitstat.data.remote.mappers

import com.alexandr7035.gitstat.core.Mapper
import com.alexandr7035.gitstat.data.local.model.RepositoryEntity
import com.alexandr7035.gitstat.data.remote.model.RepositoryModel

class RepositoryRemoteToCacheMapper: Mapper<RepositoryModel, RepositoryEntity> {
    override fun transform(data: RepositoryModel): RepositoryEntity {
        if (data.language == null) {
            data.language = "Unknown"
        }

        return RepositoryEntity(
            id = data.id,
            name = data.name,
            language = data.language,
            isPrivate = data.private
        )
    }
}