package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match

interface MatchRepository {
    suspend fun startMatch(match: Match): Resource<Match>
}
