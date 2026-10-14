"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const scoring = require("./leaderboardScoring");

test("batting points honor run, boundary and exclusive milestone boundaries", () => {
  assert.equal(scoring.scoreBatting({ runs: 49, fours: 2, sixes: 1 }), 56);
  assert.equal(scoring.scoreBatting({ runs: 50 }), 65);
  assert.equal(scoring.scoreBatting({ runs: 99 }), 114);
  assert.equal(scoring.scoreBatting({ runs: 100 }), 130);
});

test("bowling points credit wickets, maidens and exclusive haul bonuses", () => {
  assert.equal(scoring.scoreBowling({ wickets: 2, maidens: 1 }), 50);
  assert.equal(scoring.scoreBowling({ wickets: 3 }), 70);
  assert.equal(scoring.scoreBowling({ wickets: 5 }), 125);
  assert.equal(scoring.isBowlerCreditedDismissal("CAUGHT"), true);
  assert.equal(scoring.isBowlerCreditedDismissal("RUN_OUT"), false);
  assert.equal(scoring.isBowlerCreditedDismissal("RETIRED_HURT"), false);
  assert.equal(scoring.isBowlerCreditedDismissal("TIMED_OUT"), true);
  assert.equal(scoring.isBowlerCreditedDismissal("OTHER"), true);
});

test("fielding points score only supplied credited event counts", () => {
  assert.equal(scoring.scoreFielding({ catches: 2, runOuts: 1, stumpings: 1 }), 40);
  assert.equal(scoring.creditedFieldingEvent("CAUGHT", "fielder"), "catches");
  assert.equal(scoring.creditedFieldingEvent("RUN_OUT", "fielder"), "runOuts");
  assert.equal(scoring.creditedFieldingEvent("RUN_OUT", null), null);
  assert.equal(scoring.creditedFieldingEvent("BOWLED", "fielder"), null);
});

test("limited overs scopes include each eligible match once and exclude Test", () => {
  assert.deepEqual(scoring.formatScopes("T10"), ["T10", "LIMITED_OVERS"]);
  assert.deepEqual(scoring.formatScopes("ODI"), ["ODI", "LIMITED_OVERS"]);
  assert.deepEqual(scoring.formatScopes("TEST"), ["TEST"]);
  const scopes = scoring.rankingScopes({ sport: "CRICKET", ballType: "LEATHER", format: "T20", tournamentId: "cup1" });
  assert.equal(scopes.filter((scope) => scope.tournamentId === "").length, 2);
  assert.equal(scopes.filter((scope) => scope.tournamentId === "cup1").length, 2);
  assert.deepEqual(new Set(scopes.map((scope) => scope.format)), new Set(["T20", "LIMITED_OVERS"]));
});

test("replaying an identical contribution changes totals only once when old contribution is replaced", () => {
  const row = { players: { p1: { matches: 1, runs: 40, battingPoints: 40 } }, teams: { t1: { matchesPlayed: 1, wins: 1, points: 3 } } };
  const once = scoring.applyContributionDelta({}, row, 1);
  const retry = scoring.applyContributionDelta(scoring.applyContributionDelta(once, row, -1), row, 1);
  assert.deepEqual(retry.players.p1, once.players.p1);
  assert.deepEqual(retry.teams.t1, once.teams.t1);
});

test("correction replacement removes old figures then applies corrected figures", () => {
  const oldRow = { players: { p1: { matches: 1, runs: 50, battingPoints: 65 } }, teams: { t1: { matchesPlayed: 1, wins: 1, points: 3 } } };
  const newRow = { players: { p1: { matches: 1, runs: 44, battingPoints: 44 } }, teams: { t1: { matchesPlayed: 1, ties: 1, points: 1 } } };
  const oldTotals = scoring.applyContributionDelta({}, oldRow, 1);
  const reverted = scoring.applyContributionDelta(oldTotals, oldRow, -1);
  const corrected = scoring.applyContributionDelta(reverted, newRow, 1);
  assert.equal(corrected.players.p1.runs, 44);
  assert.equal(corrected.teams.t1.points, 1);
  assert.equal(corrected.teams.t1.wins, 0);
});

test("team result points and deterministic NRR components aggregate", () => {
  assert.equal(scoring.teamPointsForResult("WIN"), 3);
  assert.equal(scoring.teamPointsForResult("TIE"), 1);
  assert.equal(scoring.teamPointsForResult("LOSS"), 0);
  assert.equal(scoring.teamPointsForResult("NO_RESULT"), 0);
  assert.equal(scoring.teamPointsForResult("NO_RESULT", { allowNoResultPoints: true }), 1);
  assert.equal(scoring.teamPointsForResult("NO_RESULT", { noResultPoints: 2 }), 2);
  const win = scoring.aggregateTeam(undefined, { matchesPlayed: 1, wins: 1, points: 3, runsFor: 100, ballsFaced: 60, runsAgainst: 90, ballsBowled: 60 });
  const tie = scoring.aggregateTeam(win, { matchesPlayed: 1, ties: 1, points: 1, runsFor: 80, ballsFaced: 60, runsAgainst: 80, ballsBowled: 60 });
  assert.equal(tie.points, 4);
  assert.equal(tie.wins, 1);
  assert.equal(tie.ties, 1);
  assert.equal(tie.netRunRate, 0.5);
});
