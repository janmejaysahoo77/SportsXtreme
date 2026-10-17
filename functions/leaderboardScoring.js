"use strict";

const SCORING_VERSION = "sportsxtreme-cricket-v1";
const SCORING = Object.freeze({
  batting: Object.freeze({ run: 1, four: 2, six: 3, halfCentury: 15, century: 30 }),
  bowling: Object.freeze({ wicket: 20, maiden: 10, threeWicketHaul: 10, fiveWicketHaul: 25 }),
  fielding: Object.freeze({ catch: 10, runOut: 10, stumping: 10 }),
  team: Object.freeze({ win: 3, tie: 1, noResult: 1 })
});
// Keep this aligned with InningsProjectionCalculator. The scorer currently
// treats these four dismissal types as not credited to the bowler.
const NON_BOWLER_CREDITED_DISMISSALS = new Set(["RUN_OUT", "RETIRED_OUT", "RETIRED_HURT", "OBSTRUCTING_THE_FIELD", "HIT_BALL_TWICE"]);

function scoreBatting({ runs = 0, fours = 0, sixes = 0 }) {
  const milestone = runs >= 100 ? SCORING.batting.century : runs >= 50 ? SCORING.batting.halfCentury : 0;
  return runs + fours * SCORING.batting.four + sixes * SCORING.batting.six + milestone;
}

function scoreBowling({ wickets = 0, maidens = 0 }) {
  const haul = wickets >= 5 ? SCORING.bowling.fiveWicketHaul : wickets >= 3 ? SCORING.bowling.threeWicketHaul : 0;
  return wickets * SCORING.bowling.wicket + maidens * SCORING.bowling.maiden + haul;
}

function scoreFielding({ catches = 0, runOuts = 0, stumpings = 0 }) {
  return (catches + runOuts + stumpings) * SCORING.fielding.catch;
}

function teamPointsForResult(result, rules = {}) {
  // Optional tournament document shape: pointsRules.{winPoints,tiePoints,
  // noResultPoints,allowNoResultPoints}. Missing values use the V1 defaults.
  const configured = (key, fallback) => Number.isInteger(rules[key]) && rules[key] >= 0 ? rules[key] : fallback;
  if (result === "WIN") return configured("winPoints", SCORING.team.win);
  if (result === "TIE") return configured("tiePoints", SCORING.team.tie);
  if (result === "NO_RESULT") {
    if (Number.isInteger(rules.noResultPoints) && rules.noResultPoints >= 0) return rules.noResultPoints;
    return rules.allowNoResultPoints === true ? SCORING.team.noResult : 0;
  }
  return 0;
}

function creditedFieldingEvent(type, fielderId) {
  if (!fielderId) return null;
  if (type === "CAUGHT") return "catches";
  if (type === "RUN_OUT") return "runOuts";
  if (type === "STUMPED") return "stumpings";
  return null;
}

function isBowlerCreditedDismissal(type) {
  return !NON_BOWLER_CREDITED_DISMISSALS.has(type);
}

function battingMilestone(runs) {
  return runs >= 100 ? "CENTURY" : runs >= 50 ? "HALF_CENTURY" : null;
}

function bowlingHaul(wickets) {
  return wickets >= 5 ? "FIVE_WICKET" : wickets >= 3 ? "THREE_WICKET" : null;
}

function formatScopes(format) {
  const value = String(format || "").toUpperCase();
  return [value, ...(new Set(["T10", "T20", "ODI"]).has(value) ? ["LIMITED_OVERS"] : [])];
}

function aggregatePlayer(stats, delta) {
  const next = { ...(stats || {}) };
  for (const key of [
    "matches", "battingInnings", "runs", "balls", "fours", "sixes", "outs", "battingPoints",
    "bowlingInnings", "legalBalls", "runsConceded", "wickets", "maidens", "bowlingPoints",
    "catches", "runOuts", "stumpings", "fieldingPoints", "allRounderPoints"
  ]) next[key] = (next[key] || 0) + (delta[key] || 0);
  return next;
}

function aggregateTeam(stats, delta) {
  const next = { ...(stats || {}) };
  for (const key of ["matchesPlayed", "wins", "losses", "ties", "noResults", "points", "runsFor", "ballsFaced", "runsAgainst", "ballsBowled"]) {
    next[key] = (next[key] || 0) + (delta[key] || 0);
  }
  next.netRunRate = (next.ballsFaced > 0 ? next.runsFor * 6 / next.ballsFaced : 0)
    - (next.ballsBowled > 0 ? next.runsAgainst * 6 / next.ballsBowled : 0);
  return next;
}

function applyContributionDelta(current = {}, contribution, multiplier) {
  const result = { players: {}, teams: {} };
  for (const [id, stats] of Object.entries(contribution.players || {})) {
    result.players[id] = aggregatePlayer(current.players?.[id], Object.fromEntries(Object.entries(stats).filter(([key, value]) => typeof value === "number").map(([key, value]) => [key, value * multiplier])));
  }
  for (const [id, stats] of Object.entries(contribution.teams || {})) {
    result.teams[id] = aggregateTeam(current.teams?.[id], Object.fromEntries(Object.entries(stats).filter(([key, value]) => typeof value === "number").map(([key, value]) => [key, value * multiplier])));
  }
  return result;
}

function rankingScopes(match) {
  const format = String(match.format || "").toUpperCase();
  const ballType = String(match.ballType || "").toUpperCase();
  if (!format || !ballType) throw new Error("A finalized match needs a known ball type and format for ranking.");
  return formatScopes(format).flatMap((formatKey) => [
    { sport: String(match.sport || "CRICKET").toUpperCase(), ballType, format: formatKey, tournamentId: "" },
    ...(match.tournamentId ? [{ sport: String(match.sport || "CRICKET").toUpperCase(), ballType, format: formatKey, tournamentId: match.tournamentId }] : [])
  ]);
}

module.exports = {
  SCORING_VERSION, SCORING, scoreBatting, scoreBowling, scoreFielding, teamPointsForResult,
  creditedFieldingEvent, isBowlerCreditedDismissal, battingMilestone, bowlingHaul, formatScopes, rankingScopes,
  aggregatePlayer, aggregateTeam, applyContributionDelta
};
