# Start Match Backend Architecture

This phase supports Friendly Matches only. The model and Firestore layout are intentionally shaped for tournament matches, spectators, history, undo, player stats, and analytics.

## Package Structure

```text
com.example.sportsxtreme
  data
    di
      FirebaseModule
      MatchUseCaseModule
    remote.firestore
      FirestoreMatchDataSource
      FirestoreTeamDataSource
      FirestoreScoringDataSource
  domain
    model
      Match, Team, Player, Toss, PlayingXI, Innings, BallEvent, MatchState
      FriendlyMatchFixtures
    repository
      MatchRepository, TeamRepository, ScoringRepository
    usecase
      CreateMatchUseCase
      SelectPlayingXIUseCase
      SaveTossUseCase
      SelectOpeningPlayersUseCase
      StartMatchUseCase
      RecordBallUseCase
      UndoBallUseCase
      FinishInningsUseCase
      FinishMatchUseCase
      ObserveMatchStateUseCase
```

## Firestore Architecture

```text
users/{userId}
teams/{teamId}
teams/{teamId}/players/{playerId}
matches/{matchId}
matches/{matchId}/innings/{inningsId}
matches/{matchId}/innings/{inningsId}/events/{eventId}
matches/{matchId}/state/current
matches/{matchId}/spectators/{userId}
matches/{matchId}/undoLog/{undoId}
tournaments/{tournamentId}
tournaments/{tournamentId}/matches/{matchId}
playerStats/{playerId}
analytics/matches/{matchId}
```

`users`: Firebase Authentication identity and future organiser/player profiles.

`teams`: team metadata. Friendly test teams use the same collection as future user-created and tournament teams.

`teams/{teamId}/players`: roster source. Current phase stores guest players `dA1..dA15` and `dB1..dB15`; future phases can link each player to `users/{userId}`.

`matches`: match header and lifecycle state: type, organiser, selected teams, playing XI ids, toss, status, timestamps.

`matches/{matchId}/innings`: innings headers and snapshots: batting team, bowling team, score, wickets, legal balls, current players, target.

`matches/{matchId}/innings/{inningsId}/events`: immutable `BallEvent` stream. This is the source of truth for scoring.

`matches/{matchId}/state/current`: denormalized `MatchState` projection for Live Scoring and spectators. It is rebuilt from events when needed.

`matches/{matchId}/spectators`: live presence and lightweight spectator metadata without touching scoring documents.

`matches/{matchId}/undoLog`: audit trail for undo requests. Undo appends a compensating event/log entry instead of deleting prior balls.

`tournaments`: tournament metadata for later phases.

`tournaments/{tournamentId}/matches`: tournament schedule/index documents pointing to `matches/{matchId}`.

`playerStats`: aggregated player statistics updated from finalized events. This avoids expensive history scans.

`analytics`: future event projections for dashboards, win probability, run rate graphs, and usage analysis.

## Event Driven Scoring

Each delivery is an immutable `BallEvent`.

The app never edits or deletes previous ball events. `RecordBallUseCase` appends a new event, then the repository implementation will update `matches/{matchId}/state/current` in a Firestore transaction. `UndoBallUseCase` will append an undo/audit record and create a corrected projection from the event stream.

This keeps scoring auditable, lets match history be replayed, and makes future analytics possible without changing the UI contract.

## MatchState Contract

`MatchState` is the single observable model for Live Scoring. It contains:

- current score
- wickets
- overs
- striker
- non-striker
- bowler
- batting team
- bowling team
- current over events
- match status
- target and last event pointer

The Live Scoring ViewModel should observe `ObserveMatchStateUseCase(matchId)` and render from this model only.

## Dependency Graph

```text
UI ViewModel
  -> MatchUseCases
      -> MatchRepository / ScoringRepository / TeamRepository
          -> FirestoreMatchDataSource / FirestoreScoringDataSource / FirestoreTeamDataSource
              -> FirebaseFirestore + FirebaseAuth
```

Hilt owns Firebase singleton providers now. Repository and datasource implementations are intentionally not bound in this phase because only contracts were requested.

## Scalability

The architecture scales by separating writes and reads:

- Ball events are append-only, so live scoring is safe and auditable.
- `MatchState` is denormalized for fast UI and spectator reads.
- Player stats and analytics are separate projections, so they can be rebuilt from `BallEvent`.
- Tournament matches reuse the same `matches` collection and add tournament indexes instead of a separate scoring model.
- Friendly guest players and real user-linked players share the same `Player` domain model.
