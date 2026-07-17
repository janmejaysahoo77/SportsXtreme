package com.example.sportsxtreme.data.remote.mapper

interface RemoteMapper<Remote, Domain> {
    fun mapToDomain(remote: Remote): Domain
}
