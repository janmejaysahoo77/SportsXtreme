package com.sportsxtreme.app.data.remote.mapper

interface RemoteMapper<Remote, Domain> {
    fun mapToDomain(remote: Remote): Domain
}
