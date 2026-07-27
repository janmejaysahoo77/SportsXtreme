package com.example.sportsxtreme.data.remote.firestore

object FirestoreDataSourceResponsibilities {
    const val MATCH =
        "Owns match documents, match lifecycle fields, playing XI, toss, innings headers, and observable MatchState snapshots."
    const val TEAM =
        "Owns team documents, temporary friendly test rosters, and future user-created or tournament team rosters."
    const val SCORING =
        "Owns immutable BallEvent appends, undo event appends, and transactional MatchState projection updates."
}
