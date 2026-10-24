package com.worldfootball.director.engine

import com.worldfootball.director.data.WorldDatabaseSeeder
import com.worldfootball.director.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CompetitionEngine {

    fun generateRoundRobinFixtures(
        competitionId: String,
        seasonYear: Int,
        clubIds: List<String>,
        doubleRoundRobin: Boolean = true,
        startDayOfYear: Int = 14
    ): List<Fixture> {
        if (clubIds.size < 2) return emptyList()
        val teams = clubIds.distinct().toMutableList()
        val hasBye = teams.size % 2 != 0
        if (hasBye) teams.add("BYE_TEAM")

        val n = teams.size
        val roundsCount = n - 1
        val halfSize = n / 2
        val fixtures = mutableListOf<Fixture>()

        val rotation = teams.drop(1).toMutableList()

        for (roundIndex in 0 until roundsCount) {
            val matchday = roundIndex + 1
            val dayOfYear = (startDayOfYear + roundIndex * 7).coerceAtMost(350)
            val firstTeam = teams.first()
            val opponentForFirst = rotation.last()

            if (firstTeam != "BYE_TEAM" && opponentForFirst != "BYE_TEAM") {
                val isHome = roundIndex % 2 == 0
                fixtures.add(
                    Fixture(
                        id = "FIX_${competitionId}_${seasonYear}_M${matchday}_${firstTeam}_$opponentForFirst",
                        competitionId = competitionId,
                        competitionType = CompetitionType.DOMESTIC_LEAGUE,
                        seasonYear = seasonYear,
                        matchday = matchday,
                        dayOfYear = dayOfYear,
                        roundName = "Matchday $matchday",
                        homeClubId = if (isHome) firstTeam else opponentForFirst,
                        awayClubId = if (isHome) opponentForFirst else firstTeam
                    )
                )
            }

            for (pairIndex in 0 until (halfSize - 1)) {
                val teamA = rotation[pairIndex]
                val teamB = rotation[rotation.size - 2 - pairIndex]
                if (teamA != "BYE_TEAM" && teamB != "BYE_TEAM") {
                    val swap = (roundIndex + pairIndex) % 2 == 1
                    fixtures.add(
                        Fixture(
                            id = "FIX_${competitionId}_${seasonYear}_M${matchday}_${teamA}_$teamB",
                            competitionId = competitionId,
                            competitionType = CompetitionType.DOMESTIC_LEAGUE,
                            seasonYear = seasonYear,
                            matchday = matchday,
                            dayOfYear = dayOfYear,
                            roundName = "Matchday $matchday",
                            homeClubId = if (swap) teamB else teamA,
                            awayClubId = if (swap) teamA else teamB
                        )
                    )
                }
            }

            // Rotate clockwise
            val last = rotation.removeAt(rotation.lastIndex)
            rotation.add(0, last)
        }

        if (doubleRoundRobin) {
            val returnFixtures = fixtures.map { f ->
                val returnMatchday = f.matchday + roundsCount
                val returnDay = (startDayOfYear + (returnMatchday - 1) * 6).coerceAtMost(352)
                f.copy(
                    id = "FIX_${competitionId}_${seasonYear}_M${returnMatchday}_${f.awayClubId}_${f.homeClubId}",
                    matchday = returnMatchday,
                    dayOfYear = returnDay,
                    roundName = "Matchday $returnMatchday",
                    homeClubId = f.awayClubId,
                    awayClubId = f.homeClubId
                )
            }
            fixtures.addAll(returnFixtures)
        }

        return fixtures.sortedWith(compareBy<Fixture> { it.dayOfYear }.thenBy { it.id })
    }

    fun generateCupRoundFixtures(
        cupId: String,
        competitionType: CompetitionType,
        seasonYear: Int,
        roundNumber: Int,
        roundName: String,
        dayOfYear: Int,
        clubIds: List<String>
    ): List<Fixture> {
        val eligible = clubIds.distinct()
        val pairCount = eligible.size / 2
        val result = mutableListOf<Fixture>()
        for (i in 0 until pairCount) {
            val home = eligible[i * 2]
            val away = eligible[i * 2 + 1]
            result.add(
                Fixture(
                    id = "CUP_${cupId}_${seasonYear}_R${roundNumber}_${home}_$away",
                    competitionId = cupId,
                    competitionType = competitionType,
                    seasonYear = seasonYear,
                    matchday = roundNumber,
                    dayOfYear = dayOfYear,
                    roundName = roundName,
                    homeClubId = home,
                    awayClubId = away
                )
            )
        }
        return result
    }

    fun generateFullSeasonFixtures(
        seasonYear: Int,
        divisions: List<DivisionDefinition>,
        nationalCups: List<NationalCupDefinition>,
        continentalCompetitions: List<ContinentalCompetitionDefinition>,
        clubs: List<Club>
    ): List<Fixture> {
        val all = mutableListOf<Fixture>()
        for (div in divisions) {
            val divClubs = clubs.filter { it.divisionId == div.id }.map { it.id }
            all.addAll(
                generateRoundRobinFixtures(
                    competitionId = div.id,
                    seasonYear = seasonYear,
                    clubIds = divClubs,
                    doubleRoundRobin = div.doubleRoundRobin,
                    startDayOfYear = 14
                )
            )
        }

        val activeCountries = divisions.map { it.countryId }.distinct()
        for (countryId in activeCountries) {
            val cup = nationalCups.firstOrNull { it.countryId == countryId } ?: continue
            val countryClubs = clubs.filter { it.countryId == countryId }
                .sortedByDescending { it.reputation }
                .take(16)
                .map { it.id }
            if (countryClubs.size >= 4) {
                all.addAll(
                    generateCupRoundFixtures(
                        cupId = cup.id,
                        competitionType = CompetitionType.NATIONAL_CUP,
                        seasonYear = seasonYear,
                        roundNumber = 1,
                        roundName = "Round of 16",
                        dayOfYear = 60,
                        clubIds = countryClubs
                    )
                )
            }
        }

        val topClubs = clubs.sortedByDescending { it.continentalReputation }.take(16).map { it.id }
        val mainContinental = continentalCompetitions.firstOrNull()
        if (mainContinental != null && topClubs.size >= 8) {
            all.addAll(
                generateCupRoundFixtures(
                    cupId = mainContinental.id,
                    competitionType = CompetitionType.CONTINENTAL_CUP,
                    seasonYear = seasonYear,
                    roundNumber = 1,
                    roundName = "Continental Knockout Phase",
                    dayOfYear = 95,
                    clubIds = topClubs
                )
            )
        }

        return all.sortedBy { it.dayOfYear }
    }

    fun calculateStandings(
        division: DivisionDefinition,
        clubs: List<Club>,
        fixtures: List<Fixture>
    ): List<StandingRow> {
        val divClubs = clubs.filter { it.divisionId == division.id }
        val divFixtures = fixtures.filter { it.competitionId == division.id && it.played }

        val rows = divClubs.associate { club ->
            club.id to StandingRow(
                clubId = club.id,
                clubName = club.name,
                pointsDeduction = club.pointsDeduction,
                points = -club.pointsDeduction
            )
        }.toMutableMap()

        for (fix in divFixtures) {
            val home = rows[fix.homeClubId] ?: continue
            val away = rows[fix.awayClubId] ?: continue

            val homeWin = fix.homeGoals > fix.awayGoals
            val draw = fix.homeGoals == fix.awayGoals

            val homePts = when {
                homeWin -> division.pointsForWin
                draw -> division.pointsForDraw
                else -> 0
            }
            val awayPts = when {
                !homeWin && !draw -> division.pointsForWin
                draw -> division.pointsForDraw
                else -> 0
            }

            val homeBadge = when {
                homeWin -> "W"
                draw -> "D"
                else -> "L"
            }
            val awayBadge = when {
                !homeWin && !draw -> "W"
                draw -> "D"
                else -> "L"
            }

            rows[fix.homeClubId] = home.copy(
                played = home.played + 1,
                won = home.won + if (homeWin) 1 else 0,
                drawn = home.drawn + if (draw) 1 else 0,
                lost = home.lost + if (!homeWin && !draw) 1 else 0,
                goalsFor = home.goalsFor + fix.homeGoals,
                goalsAgainst = home.goalsAgainst + fix.awayGoals,
                points = home.points + homePts,
                form = (home.form + homeBadge).takeLast(5)
            )

            rows[fix.awayClubId] = away.copy(
                played = away.played + 1,
                won = away.won + if (!homeWin && !draw) 1 else 0,
                drawn = away.drawn + if (draw) 1 else 0,
                lost = away.lost + if (homeWin) 1 else 0,
                goalsFor = away.goalsFor + fix.awayGoals,
                goalsAgainst = away.goalsAgainst + fix.homeGoals,
                points = away.points + awayPts,
                form = (away.form + awayBadge).takeLast(5)
            )
        }

        return rows.values.sortedWith(
            Comparator { a, b ->
                if (a.points != b.points) return@Comparator b.points.compareTo(a.points)
                if (division.useHeadToHeadFirst) {
                    val h2h = compareHeadToHead(a.clubId, b.clubId, divFixtures)
                    if (h2h != 0) return@Comparator h2h
                }
                if (a.goalDifference != b.goalDifference) return@Comparator b.goalDifference.compareTo(a.goalDifference)
                if (a.goalsFor != b.goalsFor) return@Comparator b.goalsFor.compareTo(a.goalsFor)
                a.clubName.compareTo(b.clubName)
            }
        )
    }

    private fun compareHeadToHead(clubA: String, clubB: String, fixtures: List<Fixture>): Int {
        var ptsA = 0
        var ptsB = 0
        for (f in fixtures) {
            if ((f.homeClubId == clubA && f.awayClubId == clubB) || (f.homeClubId == clubB && f.awayClubId == clubA)) {
                val goalsA = if (f.homeClubId == clubA) f.homeGoals else f.awayGoals
                val goalsB = if (f.homeClubId == clubB) f.homeGoals else f.awayGoals
                when {
                    goalsA > goalsB -> ptsA += 3
                    goalsB > goalsA -> ptsB += 3
                    else -> {
                        ptsA += 1
                        ptsB += 1
                    }
                }
            }
        }
        return ptsB.compareTo(ptsA)
    }

    fun applyPromotionAndRelegation(
        countryId: String,
        divisions: List<DivisionDefinition>,
        clubs: List<Club>,
        fixtures: List<Fixture>
    ): List<Club> {
        val countryDivs = divisions.filter { it.countryId == countryId }.sortedBy { it.tier }
        if (countryDivs.size < 2) return clubs

        val clubDivisionUpdates = mutableMapOf<String, String>()

        for (i in 0 until countryDivs.size - 1) {
            val upperDiv = countryDivs[i]
            val lowerDiv = countryDivs[i + 1]

            val upperTable = calculateStandings(upperDiv, clubs, fixtures)
            val lowerTable = calculateStandings(lowerDiv, clubs, fixtures)

            val moveCount = min(upperDiv.relegationSpots, lowerDiv.promotionSpots)
                .coerceAtMost(min(upperTable.size / 2, lowerTable.size / 2))

            if (moveCount > 0) {
                val relegated = upperTable.takeLast(moveCount).map { it.clubId }
                val promoted = lowerTable.take(moveCount).map { it.clubId }

                relegated.forEach { id -> clubDivisionUpdates[id] = lowerDiv.id }
                promoted.forEach { id -> clubDivisionUpdates[id] = upperDiv.id }
            }
        }

        return clubs.map { c ->
            val newDivId = clubDivisionUpdates[c.id]
            if (newDivId != null) c.copy(divisionId = newDivId) else c
        }
    }
}

object MatchEngine {

    fun simulateMatch(
        fixture: Fixture,
        homeClub: Club,
        awayClub: Club,
        homePlayers: List<Player>,
        awayPlayers: List<Player>,
        isKnockout: Boolean = false,
        hasVar: Boolean = true,
        seed: Long = fixture.id.hashCode().toLong()
    ): Fixture {
        val rng = Random(seed)

        val homeXI = selectActiveEleven(homeClub, homePlayers)
        val awayXI = selectActiveEleven(awayClub, awayPlayers)

        val homeAttack = computeUnitAttack(homeClub, homeXI)
        val homeDefense = computeUnitDefense(homeClub, homeXI)
        val homeMidfield = computeUnitMidfield(homeClub, homeXI)

        val awayAttack = computeUnitAttack(awayClub, awayXI)
        val awayDefense = computeUnitDefense(awayClub, awayXI)
        val awayMidfield = computeUnitMidfield(awayClub, awayXI)

        val homeAdvantage = 1.07
        val rawHomePoss = 50.0 + (homeMidfield * homeAdvantage - awayMidfield) * 0.45
        val homePossession = rawHomePoss.toInt().coerceIn(28, 72)

        var homeGoals = 0
        var awayGoals = 0
        var homeShots = 0
        var awayShots = 0
        var homeSoT = 0
        var awaySoT = 0
        var homeRedCards = 0
        var awayRedCards = 0

        val events = mutableListOf<MatchEvent>()
        val ratings = mutableMapOf<String, Double>()
        (homeXI + awayXI).forEach { ratings[it.id] = 6.7 }

        val totalMinutes = 90
        for (minute in 1..totalMinutes) {
            val homeRedPenalty = 1.0 - (homeRedCards * 0.14)
            val awayRedPenalty = 1.0 - (awayRedCards * 0.14)

            val homeChanceProb = ((homeAttack * homeAdvantage * homeRedPenalty) / (awayDefense * 28.0)).coerceIn(0.012, 0.085)
            val awayChanceProb = ((awayAttack * awayRedPenalty) / (homeDefense * 30.0)).coerceIn(0.010, 0.080)

            // Home attacking sequence
            if (rng.nextDouble() < homeChanceProb) {
                homeShots++
                val shooter = pickAttacker(homeXI, rng)
                val assister = pickCreator(homeXI, shooter.id, rng)
                val oppGk = awayXI.firstOrNull { it.mainPosition == Position.GK } ?: awayXI.first()

                val onTargetProb = ((shooter.technical.finishing + shooter.mental.composure) / 165.0).coerceIn(0.28, 0.72)
                if (rng.nextDouble() < onTargetProb) {
                    homeSoT++
                    val goalProb = ((shooter.technical.finishing * 1.15 - oppGk.goalkeeping.reflexes * 0.65 + 25.0) / 115.0).coerceIn(0.16, 0.62)
                    if (rng.nextDouble() < goalProb) {
                        if (hasVar && rng.nextDouble() < 0.06) {
                            events.add(
                                MatchEvent(
                                    minute = minute,
                                    type = "VAR_CHECK",
                                    clubId = homeClub.id,
                                    primaryPlayerName = shooter.fullName,
                                    commentary = "$minute' VAR Check: Goal by ${shooter.fullName} ruled offside after review!"
                                )
                            )
                        } else {
                            homeGoals++
                            ratings[shooter.id] = (ratings[shooter.id] ?: 6.7) + 1.1
                            ratings[assister.id] = (ratings[assister.id] ?: 6.7) + 0.6
                            events.add(
                                MatchEvent(
                                    minute = minute,
                                    type = "GOAL",
                                    clubId = homeClub.id,
                                    primaryPlayerName = shooter.fullName,
                                    secondaryPlayerName = assister.fullName,
                                    commentary = "$minute' GOAL! ${shooter.fullName} scores for ${homeClub.name} (Assist: ${assister.fullName})!"
                                )
                            )
                        }
                    } else {
                        ratings[oppGk.id] = (ratings[oppGk.id] ?: 6.7) + 0.35
                        events.add(
                            MatchEvent(
                                minute = minute,
                                type = "SHOT_SAVED",
                                clubId = awayClub.id,
                                primaryPlayerName = oppGk.fullName,
                                secondaryPlayerName = shooter.fullName,
                                commentary = "$minute' Fine save by ${oppGk.fullName} to deny ${shooter.fullName}!"
                            )
                        )
                    }
                }
            }

            // Away attacking sequence
            if (rng.nextDouble() < awayChanceProb) {
                awayShots++
                val shooter = pickAttacker(awayXI, rng)
                val assister = pickCreator(awayXI, shooter.id, rng)
                val oppGk = homeXI.firstOrNull { it.mainPosition == Position.GK } ?: homeXI.first()

                val onTargetProb = ((shooter.technical.finishing + shooter.mental.composure) / 165.0).coerceIn(0.28, 0.72)
                if (rng.nextDouble() < onTargetProb) {
                    awaySoT++
                    val goalProb = ((shooter.technical.finishing * 1.15 - oppGk.goalkeeping.reflexes * 0.65 + 25.0) / 115.0).coerceIn(0.16, 0.62)
                    if (rng.nextDouble() < goalProb) {
                        awayGoals++
                        ratings[shooter.id] = (ratings[shooter.id] ?: 6.7) + 1.1
                        ratings[assister.id] = (ratings[assister.id] ?: 6.7) + 0.6
                        events.add(
                            MatchEvent(
                                minute = minute,
                                type = "GOAL",
                                clubId = awayClub.id,
                                primaryPlayerName = shooter.fullName,
                                secondaryPlayerName = assister.fullName,
                                commentary = "$minute' GOAL! ${shooter.fullName} finishes clinically for ${awayClub.name}!"
                            )
                        )
                    } else {
                        ratings[oppGk.id] = (ratings[oppGk.id] ?: 6.7) + 0.35
                        events.add(
                            MatchEvent(
                                minute = minute,
                                type = "SHOT_SAVED",
                                clubId = homeClub.id,
                                primaryPlayerName = oppGk.fullName,
                                secondaryPlayerName = shooter.fullName,
                                commentary = "$minute' ${oppGk.fullName} parries away a fierce strike from ${shooter.fullName}!"
                            )
                        )
                    }
                }
            }

            // Discipline & tactical events
            if (rng.nextDouble() < 0.025) {
                val isHomeCard = rng.nextBoolean()
                val squad = if (isHomeCard) homeXI else awayXI
                val offender = squad[rng.nextInt(squad.size)]
                val clubId = if (isHomeCard) homeClub.id else awayClub.id
                if (rng.nextDouble() < 0.08) {
                    if (isHomeCard) homeRedCards++ else awayRedCards++
                    ratings[offender.id] = (ratings[offender.id] ?: 6.7) - 1.5
                    events.add(
                        MatchEvent(
                            minute = minute,
                            type = "RED_CARD",
                            clubId = clubId,
                            primaryPlayerName = offender.fullName,
                            commentary = "$minute' RED CARD! ${offender.fullName} is sent off for a reckless challenge!"
                        )
                    )
                } else {
                    ratings[offender.id] = (ratings[offender.id] ?: 6.7) - 0.3
                    events.add(
                        MatchEvent(
                            minute = minute,
                            type = "YELLOW_CARD",
                            clubId = clubId,
                            primaryPlayerName = offender.fullName,
                            commentary = "$minute' Yellow card shown to ${offender.fullName}."
                        )
                    )
                }
            }
        }

        var homePens: Int? = null
        var awayPens: Int? = null
        if (isKnockout && homeGoals == awayGoals) {
            homePens = rng.nextInt(3, 6)
            awayPens = (homePens + if (rng.nextBoolean()) 1 else -1).coerceAtLeast(1)
            if (homePens == awayPens) homePens += 1
            events.add(
                MatchEvent(
                    minute = 120,
                    type = "PENALTY",
                    clubId = if (homePens > awayPens) homeClub.id else awayClub.id,
                    primaryPlayerName = "Penalty Shootout",
                    commentary = "120' Shootout Finished: ${homeClub.shortName} $homePens - $awayPens ${awayClub.shortName}"
                )
            )
        }

        val normalizedRatings = ratings.mapValues { (_, v) ->
            (Math.round(v.coerceIn(4.5, 10.0) * 10.0) / 10.0)
        }

        return fixture.copy(
            played = true,
            homeGoals = homeGoals,
            awayGoals = awayGoals,
            homePenalties = homePens,
            awayPenalties = awayPens,
            homePossession = homePossession,
            homeShots = max(homeShots, homeGoals),
            awayShots = max(awayShots, awayGoals),
            homeShotsOnTarget = max(homeSoT, homeGoals),
            awayShotsOnTarget = max(awaySoT, awayGoals),
            events = events,
            playerRatings = normalizedRatings
        )
    }

    private fun selectActiveEleven(club: Club, players: List<Player>): List<Player> {
        val byId = players.associateBy { it.id }
        val configured = club.tactic.startingElevenIds.mapNotNull { byId[it] }
        if (configured.size == 11) return configured
        return players.filter { it.injuryDaysRemaining == 0 }
            .sortedByDescending { it.overallRating() }
            .take(11)
            .ifEmpty { players.take(11) }
    }

    private fun computeUnitAttack(club: Club, xi: List<Player>): Double {
        val attackers = xi.filter { it.mainPosition.line >= 3 }.ifEmpty { xi }
        val avg = attackers.map {
            (it.technical.finishing + it.technical.dribbling + it.mental.offTheBall + it.physical.pace) / 4.0
        }.average()
        val tempoBonus = 1.0 + (club.tactic.tempo - 50) * 0.002
        return avg * club.tactic.mentality.attackModifier * tempoBonus
    }

    private fun computeUnitMidfield(club: Club, xi: List<Player>): Double {
        val mids = xi.filter { it.mainPosition.line == 2 || it.mainPosition == Position.AM }.ifEmpty { xi }
        val avg = mids.map {
            (it.technical.passing + it.mental.vision + it.mental.decisions + it.physical.stamina) / 4.0
        }.average()
        val cohesionBonus = 0.90 + (club.tactic.teamCohesion / 500.0)
        return avg * cohesionBonus
    }

    private fun computeUnitDefense(club: Club, xi: List<Player>): Double {
        val defs = xi.filter { it.mainPosition.line <= 1 }.ifEmpty { xi }
        val avg = defs.map {
            if (it.mainPosition == Position.GK) {
                (it.goalkeeping.reflexes + it.goalkeeping.gkPositioning + it.goalkeeping.handling) / 3.0
            } else {
                (it.technical.tackling + it.technical.marking + it.mental.positioning + it.physical.strength) / 4.0
            }
        }.average()
        val pressBonus = 1.0 + (club.tactic.pressingIntensity - 50) * 0.0015
        return avg * club.tactic.mentality.defenseModifier * pressBonus
    }

    private fun pickAttacker(xi: List<Player>, rng: Random): Player {
        val forwards = xi.filter { it.mainPosition in listOf(Position.ST, Position.LW, Position.RW, Position.AM) }
        val pool = forwards.ifEmpty { xi }
        return pool[rng.nextInt(pool.size)]
    }

    private fun pickCreator(xi: List<Player>, excludeId: String, rng: Random): Player {
        val creators = xi.filter { it.id != excludeId && it.mainPosition != Position.GK }
        val pool = creators.ifEmpty { xi }
        return pool[rng.nextInt(pool.size)]
    }
}

object TransferAndContractEngine {

    fun evaluateTransferOffer(
        offer: TransferOffer,
        player: Player,
        sellingClub: Club,
        buyingClub: Club,
        buyingClubPlayers: List<Player>,
        buyingDivision: DivisionDefinition?
    ): TransferOffer {
        if (offer.fromClubId == offer.toClubId) {
            return offer.copy(status = "REJECTED", reason = "Cannot transfer a player to the same club.")
        }

        if (!offer.isLoan && buyingClub.transferBudget < offer.feeOffered) {
            return offer.copy(status = "REJECTED", reason = "Insufficient transfer budget at ${buyingClub.name}.")
        }

        if (buyingDivision != null) {
            if (buyingClubPlayers.size >= buyingDivision.maxSquadSize) {
                return offer.copy(status = "REJECTED", reason = "Squad size limit (${buyingDivision.maxSquadSize}) reached.")
            }
            val currentForeigners = buyingClubPlayers.count { it.nationalityId != buyingClub.countryId }
            if (player.nationalityId != buyingClub.countryId && currentForeigners >= buyingDivision.maxForeignPlayersInSquad) {
                return offer.copy(
                    status = "REJECTED",
                    reason = "Foreign player registration limit (${buyingDivision.maxForeignPlayersInSquad}) reached."
                )
            }
        }

        if (offer.isLoan) {
            val loanAcceptable = player.squadStatus in listOf(
                SquadStatus.YOUTH_PROSPECT,
                SquadStatus.LOAN_LISTED,
                SquadStatus.BACKUP,
                SquadStatus.SQUAD_ROTATION
            ) || offer.feeOffered >= (player.marketValue / 10)
            return if (loanAcceptable) {
                offer.copy(status = "ACCEPTED", reason = "Loan agreement accepted by ${sellingClub.name} and player.")
            } else {
                offer.copy(status = "REJECTED", reason = "${sellingClub.name} considers ${player.fullName} essential to the first team.")
            }
        }

        // Check release clause
        if (player.releaseClause != null && offer.feeOffered >= player.releaseClause) {
            val minWage = (player.wageWeekly * 0.95).toLong()
            return if (offer.offeredWageWeekly >= minWage) {
                offer.copy(status = "ACCEPTED", reason = "Release clause triggered and personal terms agreed.")
            } else {
                offer.copy(status = "REJECTED", reason = "Release clause met, but player rejected wage offer.")
            }
        }

        val statusMultiplier = when (player.squadStatus) {
            SquadStatus.STAR_PLAYER -> 1.35
            SquadStatus.IMPORTANT_STARTER -> 1.15
            SquadStatus.TRANSFER_LISTED -> 0.80
            else -> 1.00
        }
        val requiredFee = (player.marketValue * statusMultiplier).toLong()

        if (offer.feeOffered < requiredFee) {
            val counterFee = (requiredFee * 1.05).toLong()
            return offer.copy(
                status = "COUNTERED",
                feeOffered = counterFee,
                reason = "${sellingClub.name} demands at least €${counterFee / 1_000}K."
            )
        }

        val requiredWage = (player.wageWeekly * 1.05).toLong()
        if (offer.offeredWageWeekly < requiredWage) {
            return offer.copy(
                status = "REJECTED",
                reason = "${player.fullName}'s agent demands at least €${requiredWage}/wk."
            )
        }

        return offer.copy(status = "ACCEPTED", reason = "Transfer fee and player contract terms accepted.")
    }

    fun executeAcceptedTransfer(
        state: CareerSaveState,
        offer: TransferOffer
    ): CareerSaveState {
        val evaluated = offer
        if (evaluated.status != "ACCEPTED") return state

        val updatedPlayers = state.players.map { p ->
            if (p.id == offer.playerId) {
                p.copy(
                    clubId = offer.toClubId,
                    loanedFromClubId = if (offer.isLoan) offer.fromClubId else null,
                    wageWeekly = offer.offeredWageWeekly,
                    contractStartYear = state.seasonYear,
                    contractExpiryYear = state.seasonYear + offer.contractYears,
                    squadStatus = SquadStatus.IMPORTANT_STARTER
                )
            } else p
        }

        val updatedClubs = state.clubs.map { c ->
            when (c.id) {
                offer.toClubId -> c.copy(
                    balance = c.balance - offer.feeOffered,
                    transferBudget = (c.transferBudget - offer.feeOffered).coerceAtLeast(0L)
                )
                offer.fromClubId -> c.copy(
                    balance = c.balance + offer.feeOffered,
                    transferBudget = c.transferBudget + (offer.feeOffered * 0.80).toLong()
                )
                else -> c
            }
        }

        val sellingClubName = state.clubs.firstOrNull { it.id == offer.fromClubId }?.name ?: offer.fromClubId
        val buyingClubName = state.clubs.firstOrNull { it.id == offer.toClubId }?.name ?: offer.toClubId

        val ledgerAdditions = mutableListOf<LedgerEntry>()
        if (offer.toClubId == state.userClubId && offer.feeOffered > 0) {
            ledgerAdditions.add(
                LedgerEntry(state.dayOfYear, state.seasonYear, "TRANSFER_SPEND", "Signed ${offer.playerName} from $sellingClubName", -offer.feeOffered)
            )
        }
        if (offer.fromClubId == state.userClubId && offer.feeOffered > 0) {
            ledgerAdditions.add(
                LedgerEntry(state.dayOfYear, state.seasonYear, "TRANSFER_INCOME", "Sold ${offer.playerName} to $buyingClubName", offer.feeOffered)
            )
        }

        val newsEntry = NewsItem(
            id = "NEWS_TRF_${System.currentTimeMillis()}",
            dayOfYear = state.dayOfYear,
            seasonYear = state.seasonYear,
            category = "TRANSFER",
            headline = "${offer.playerName} Joins $buyingClubName",
            body = "$buyingClubName have completed the ${if (offer.isLoan) "loan" else "permanent transfer"} of ${offer.playerName} from $sellingClubName for €${offer.feeOffered / 1_000}K.",
            relatedClubId = offer.toClubId
        )

        val completedOffer = offer.copy(status = "COMPLETED")
        return state.copy(
            players = updatedPlayers,
            clubs = updatedClubs,
            transferOffers = state.transferOffers.filter { it.id != offer.id } + completedOffer,
            ledger = state.ledger + ledgerAdditions,
            news = listOf(newsEntry) + state.news.take(120)
        )
    }
}

object CalendarEngine {

    fun advanceDays(state: CareerSaveState, daysToAdvance: Int): CareerSaveState {
        var current = state
        repeat(daysToAdvance.coerceIn(1, 30)) {
            current = advanceSingleDay(current)
        }
        return current
    }

    fun advanceSingleDay(state: CareerSaveState): CareerSaveState {
        val day = state.dayOfYear
        val clubMap = state.clubs.associateBy { it.id }.toMutableMap()
        val playersByClub = state.players.groupBy { it.clubId }

        val updatedFixtures = mutableListOf<Fixture>()
        val generatedNews = mutableListOf<NewsItem>()
        val playerStatsDelta = mutableMapOf<String, PlayerStatUpdate>()

        for (fix in state.fixtures) {
            if (!fix.played && fix.dayOfYear == day) {
                val home = clubMap[fix.homeClubId]
                val away = clubMap[fix.awayClubId]
                if (home != null && away != null) {
                    val hPlayers = playersByClub[home.id].orEmpty()
                    val aPlayers = playersByClub[away.id].orEmpty()
                    val isKnockout = fix.competitionType != CompetitionType.DOMESTIC_LEAGUE

                    val simulated = MatchEngine.simulateMatch(
                        fixture = fix,
                        homeClub = home,
                        awayClub = away,
                        homePlayers = hPlayers,
                        awayPlayers = aPlayers,
                        isKnockout = isKnockout
                    )
                    updatedFixtures.add(simulated)

                    // Record appearances, goals, assists
                    simulated.playerRatings.forEach { (pid, rating) ->
                        val cur = playerStatsDelta.getOrPut(pid) { PlayerStatUpdate() }
                        cur.appearances += 1
                        cur.ratingSum += rating
                    }
                    simulated.events.filter { it.type == "GOAL" }.forEach { ev ->
                        val allMatchPlayers = hPlayers + aPlayers
                        allMatchPlayers.firstOrNull { it.fullName == ev.primaryPlayerName }?.let { scorer ->
                            playerStatsDelta.getOrPut(scorer.id) { PlayerStatUpdate() }.goals += 1
                        }
                        allMatchPlayers.firstOrNull { it.fullName == ev.secondaryPlayerName }?.let { assister ->
                            playerStatsDelta.getOrPut(assister.id) { PlayerStatUpdate() }.assists += 1
                        }
                    }

                    if (home.id == state.userClubId || away.id == state.userClubId) {
                        generatedNews.add(
                            NewsItem(
                                id = "NEWS_MATCH_${simulated.id}",
                                dayOfYear = day,
                                seasonYear = state.seasonYear,
                                category = "MATCH_REPORT",
                                headline = "Full Time: ${home.name} ${simulated.homeGoals} - ${simulated.awayGoals} ${away.name}",
                                body = "Possession: ${simulated.homePossession}% - ${100 - simulated.homePossession}%. Shots on Target: ${simulated.homeShotsOnTarget} - ${simulated.awayShotsOnTarget}.",
                                relatedClubId = state.userClubId
                            )
                        )
                    }
                } else {
                    updatedFixtures.add(fix)
                }
            } else {
                updatedFixtures.add(fix)
            }
        }

        // Update players (stats + weekly training/recovery on every 7th day)
        val isWeeklyTick = day % 7 == 0
        val updatedPlayers = state.players.map { p ->
            val delta = playerStatsDelta[p.id]
            var next = if (delta != null) {
                val totalApps = p.seasonAppearances + delta.appearances
                val newAvg = if (totalApps > 0) {
                    ((p.averageRating * p.seasonAppearances) + delta.ratingSum) / totalApps
                } else p.averageRating
                p.copy(
                    seasonAppearances = totalApps,
                    seasonGoals = p.seasonGoals + delta.goals,
                    seasonAssists = p.seasonAssists + delta.assists,
                    careerAppearances = p.careerAppearances + delta.appearances,
                    careerGoals = p.careerGoals + delta.goals,
                    averageRating = (Math.round(newAvg * 100.0) / 100.0),
                    fatigue = (p.fatigue + 14).coerceAtMost(95)
                )
            } else p

            if (next.injuryDaysRemaining > 0) {
                next = next.copy(
                    injuryDaysRemaining = next.injuryDaysRemaining - 1,
                    injuryDescription = if (next.injuryDaysRemaining - 1 == 0) null else next.injuryDescription
                )
            }

            if (isWeeklyTick) {
                val growth = if (next.age < 25 && next.overallRating() < next.hiddenPotentialAbility && day % 28 == 0) 1 else 0
                next = next.copy(
                    fatigue = (next.fatigue - 18).coerceAtLeast(0),
                    matchFitness = (next.matchFitness + 4).coerceAtMost(100),
                    technical = if (growth > 0) next.technical.copy(
                        finishing = (next.technical.finishing + 1).coerceAtMost(99),
                        passing = (next.technical.passing + 1).coerceAtMost(99),
                        tackling = (next.technical.tackling + 1).coerceAtMost(99)
                    ) else next.technical
                )
            }
            next
        }

        // Weekly club finances & infrastructure progress
        val newLedgerEntries = mutableListOf<LedgerEntry>()
        val updatedProjects = state.projects.map { proj ->
            if (!proj.completed && proj.daysRemaining > 0) {
                val rem = proj.daysRemaining - 1
                proj.copy(daysRemaining = rem, completed = rem == 0)
            } else proj
        }

        val updatedClubs = state.clubs.map { club ->
            var nextClub = club
            // Apply newly completed projects
            updatedProjects.filter { it.clubId == club.id && it.completed && it.daysRemaining == 0 }.forEach { proj ->
                nextClub = when (proj.type) {
                    "STADIUM_EXPANSION" -> nextClub.copy(stadiumCapacity = nextClub.stadiumCapacity + 4_000)
                    "TRAINING_UPGRADE" -> nextClub.copy(trainingFacilitiesLevel = (nextClub.trainingFacilitiesLevel + 1).coerceAtMost(20))
                    "YOUTH_UPGRADE" -> nextClub.copy(youthFacilitiesLevel = (nextClub.youthFacilitiesLevel + 1).coerceAtMost(20))
                    else -> nextClub
                }
            }

            if (isWeeklyTick) {
                val clubWages = updatedPlayers.filter { it.clubId == club.id }.sumOf { it.wageWeekly }
                val weeklyBroadcastAndSponsor = (club.annualBroadcastRevenue + club.annualSponsorship) / 52L
                val matchdayTicketIncome = (club.averageAttendance * club.ticketPrice) / 2L
                val netWeekly = weeklyBroadcastAndSponsor + matchdayTicketIncome - clubWages

                nextClub = nextClub.copy(balance = club.balance + netWeekly)
                if (club.id == state.userClubId) {
                    newLedgerEntries.add(
                        LedgerEntry(day, state.seasonYear, "WEEKLY_OPERATIONS", "Matchday, Broadcast & Wage Payroll", netWeekly)
                    )
                }
            }
            nextClub
        }

        val nextDay = day + 1
        val PreEndState = state.copy(
            dayOfYear = nextDay,
            clubs = updatedClubs,
            players = updatedPlayers,
            fixtures = updatedFixtures,
            projects = updatedProjects,
            ledger = state.ledger + newLedgerEntries,
            news = (generatedNews + state.news).take(140)
        )

        return if (nextDay > 360) {
            transitionToNextSeason(PreEndState)
        } else {
            PreEndState
        }
    }

    fun transitionToNextSeason(state: CareerSaveState): CareerSaveState {
        val nextYear = state.seasonYear + 1

        // 1. Generate Season Awards for Active Divisions
        val newAwards = mutableListOf<SeasonAwardRecord>()
        for (divId in state.activeDivisionIds) {
            val div = state.divisions.firstOrNull { it.id == divId } ?: continue
            val table = CompetitionEngine.calculateStandings(div, state.clubs, state.fixtures)
            val champ = table.firstOrNull()?.clubName ?: "N/A"
            val runnerUp = table.getOrNull(1)?.clubName ?: "N/A"
            val divClubIds = state.clubs.filter { it.divisionId == divId }.map { it.id }.toSet()
            val divPlayers = state.players.filter { it.clubId in divClubIds }
            val topScorer = divPlayers.maxByOrNull { it.seasonGoals }
            val bestPlayer = divPlayers.maxByOrNull { it.averageRating }
            val bestYoung = divPlayers.filter { it.age <= 21 }.maxByOrNull { it.averageRating }

            newAwards.add(
                SeasonAwardRecord(
                    seasonYear = state.seasonYear,
                    divisionId = div.id,
                    divisionName = div.name,
                    championClubName = champ,
                    runnerUpClubName = runnerUp,
                    topScorerName = topScorer?.fullName ?: "N/A",
                    topScorerGoals = topScorer?.seasonGoals ?: 0,
                    playerOfTheSeasonName = bestPlayer?.fullName ?: "N/A",
                    bestYoungPlayerName = bestYoung?.fullName ?: "N/A"
                )
            )
        }

        // 2. Apply Promotion and Relegation per Country
        var updatedClubs = state.clubs
        val activeCountries = state.divisions.filter { it.id in state.activeDivisionIds }.map { it.countryId }.distinct()
        for (countryId in activeCountries) {
            updatedClubs = CompetitionEngine.applyPromotionAndRelegation(
                countryId = countryId,
                divisions = state.divisions.filter { it.id in state.activeDivisionIds },
                clubs = updatedClubs,
                fixtures = state.fixtures
            )
        }

        // 3. Age players, reset season stats, and generate Youth Intake Newgens
        val rng = Random(nextYear.toLong())
        val agedPlayers = state.players.mapNotNull { p ->
            val newAge = p.age + 1
            if (newAge > 38) {
                null // Player retires
            } else {
                p.copy(
                    age = newAge,
                    seasonAppearances = 0,
                    seasonGoals = 0,
                    seasonAssists = 0,
                    seasonCleanSheets = 0,
                    seasonYellowCards = 0,
                    seasonRedCards = 0,
                    averageRating = 6.80,
                    fatigue = 0,
                    matchFitness = 95
                )
            }
        }.toMutableList()

        // Generate 1 high-potential youth academy newgen for each active club
        val countryMap = state.countries.associateBy { it.id }
        val activeClubs = updatedClubs.filter { it.divisionId in state.activeDivisionIds }
        for (club in activeClubs) {
            val country = countryMap[club.countryId] ?: state.countries.first()
            val newgen = WorldDatabaseSeeder.generateSquadForClub(
                clubId = club.id,
                country = country,
                baseSkill = (club.reputation - 10).coerceIn(38, 76),
                count = 1,
                rng = rng
            ).first().copy(
                id = "NEWGEN_${nextYear}_${club.id}",
                age = 16,
                birthYear = nextYear - 16,
                squadType = SquadTeamType.YOUTH_U19,
                squadStatus = SquadStatus.YOUTH_PROSPECT,
                hiddenPotentialAbility = (club.youthFacilitiesLevel * 4 + rng.nextInt(5, 18)).coerceIn(68, 96)
            )
            agedPlayers.add(newgen)
        }

        // 4. Generate New Season Fixtures
        val newFixtures = CompetitionEngine.generateFullSeasonFixtures(
            seasonYear = nextYear,
            divisions = state.divisions.filter { it.id in state.activeDivisionIds },
            nationalCups = state.nationalCups,
            continentalCompetitions = state.continentalCompetitions,
            clubs = updatedClubs
        )

        val seasonRolloverNews = NewsItem(
            id = "NEWS_NEW_SEASON_$nextYear",
            dayOfYear = 1,
            seasonYear = nextYear,
            category = "AWARD",
            headline = "$nextYear/${(nextYear + 1) % 100} Season Begins: Promotions, Relegations & Youth Intake Complete",
            body = "All league tables have been finalized, promoted and relegated clubs have moved divisions, and academy newgens have signed youth contracts.",
            relatedClubId = state.userClubId
        )

        return state.copy(
            seasonYear = nextYear,
            dayOfYear = 1,
            clubs = updatedClubs,
            players = agedPlayers,
            fixtures = newFixtures,
            awardsHistory = newAwards + state.awardsHistory,
            news = listOf(seasonRolloverNews) + state.news.take(100)
        )
    }

    private class PlayerStatUpdate(
        var appearances: Int = 0,
        var goals: Int = 0,
        var assists: Int = 0,
        var ratingSum: Double = 0.0
    )
}

object DatabaseValidator {

    fun validateCareerState(state: CareerSaveState): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        val duplicateClubIds = state.clubs.groupBy { it.id }.filter { it.value.size > 1 }.keys
        if (duplicateClubIds.isNotEmpty()) {
            issues.add(ValidationIssue("ERROR", "DUPLICATE_CLUB_ID", "Duplicate Club IDs found: $duplicateClubIds"))
        }

        val duplicatePlayerIds = state.players.groupBy { it.id }.filter { it.value.size > 1 }.keys
        if (duplicatePlayerIds.isNotEmpty()) {
            issues.add(ValidationIssue("ERROR", "DUPLICATE_PLAYER_ID", "Duplicate Player IDs found: $duplicatePlayerIds"))
        }

        val validDivisionIds = state.divisions.map { it.id }.toSet()
        state.clubs.forEach { club ->
            if (club.divisionId !in validDivisionIds) {
                issues.add(
                    ValidationIssue("ERROR", "INVALID_DIVISION_REF", "Club ${club.name} references unknown division ${club.divisionId}")
                )
            }
        }

        val validClubIds = state.clubs.map { it.id }.toSet()
        state.players.forEach { player ->
            if (player.clubId !in validClubIds) {
                issues.add(
                    ValidationIssue("ERROR", "INVALID_CLUB_REF", "Player ${player.fullName} references missing club ${player.clubId}")
                )
            }
            if (player.overallRating() !in 1..100) {
                issues.add(
                    ValidationIssue("ERROR", "ATTRIBUTE_OUT_OF_RANGE", "Player ${player.fullName} has invalid overall rating ${player.overallRating()}")
                )
            }
        }

        state.divisions.forEach { div ->
            if (div.promotionSpots + div.relegationSpots > div.teamCount) {
                issues.add(
                    ValidationIssue("ERROR", "IMPOSSIBLE_PROMOTION_RULE", "Division ${div.name} has more promotion+relegation spots than teams.")
                )
            }
        }

        return issues
    }
}
