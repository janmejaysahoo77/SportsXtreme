package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository

class MatchRepositoryImpl : MatchRepository {
    override suspend fun startMatch(match: Match): Resource<Match> {
        return Resource.Success(match)
    }
}
