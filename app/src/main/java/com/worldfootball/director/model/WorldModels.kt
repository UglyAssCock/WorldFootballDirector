package com.worldfootball.director.model

import kotlinx.serialization.Serializable

@Serializable
enum class DataProvenance {
    VERIFIED_STRUCTURE,
    GENERATED_SEED,
    USER_CUSTOM
}

@Serializable
enum class Continent {
    EUROPE,
    SOUTH_AMERICA,
    NORTH_CENTRAL_AMERICA,
    AFRICA,
    ASIA,
    OCEANIA,
    WORLD
}

@Serializable
enum class DatabasePreset(val label: String, val clubsPerActiveDiv: Int, val playersPerClub: Int) {
    SMALL("Small Database (Fastest)", 12, 18),
    STANDARD("Standard Database (Balanced)", 16, 22),
    LARGE("Large Database (Deep World)", 20, 25),
    CUSTOM("Custom Universe", 18, 22)
}

@Serializable
enum class SimulationDetailLevel(val label: String) {
    FULL_DETAIL("Full Match Engine (All Active Leagues)"),
    BALANCED("Full User League + Statistical Background"),
    FAST_MOBILE("Fast Statistical Simulation (Low Battery)")
}

@Serializable
enum class BadgePattern {
    VERTICAL_STRIPES,
    HORIZONTAL_HOOPS,
    DIAGONAL_SASH,
    HALF_AND_HALF,
    CHEVRON,
    SOLID_CREST
}

@Serializable
data class Country(
    val id: String,
    val name: String,
    val continent: Continent,
    val fifaRankingPoints: Int = 1400,
    val youthRating: Int = 65,
    val primaryColorHex: String = "#10B981",
    val secondaryColorHex: String = "#FFFFFF",
    val provenance: DataProvenance = DataProvenance.VERIFIED_STRUCTURE
)

@Serializable
data class DivisionDefinition(
    val id: String,
    val countryId: String,
    val name: String,
    val tier: Int,
    val teamCount: Int,
    val doubleRoundRobin: Boolean = true,
    val promotionSpots: Int = 2,
    val playoffSpots: Int = 0,
    val relegationSpots: Int = 2,
    val maxForeignPlayersInSquad: Int = 8,
    val maxSquadSize: Int = 25,
    val prizePoolMoney: Long = 15_000_000L,
    val hasVar: Boolean = true,
    val pointsForWin: Int = 3,
    val pointsForDraw: Int = 1,
    val useHeadToHeadFirst: Boolean = false,
    val provenance: DataProvenance = DataProvenance.VERIFIED_STRUCTURE
)

@Serializable
data class NationalCupDefinition(
    val id: String,
    val countryId: String,
    val name: String,
    val twoLeggedSemiFinals: Boolean = false,
    val extraTimeEnabled: Boolean = true,
    val replaysEnabled: Boolean = false,
    val prizeMoneyWinner: Long = 4_000_000L,
    val provenance: DataProvenance = DataProvenance.VERIFIED_STRUCTURE
)

@Serializable
data class ContinentalCompetitionDefinition(
    val id: String,
    val continent: Continent,
    val name: String,
    val tier: Int,
    val participantCount: Int = 32,
    val usesLeaguePhase: Boolean = true,
    val prizeMoneyWinner: Long = 25_000_000L,
    val provenance: DataProvenance = DataProvenance.VERIFIED_STRUCTURE
)

@Serializable
enum class SquadTeamType {
    MAIN_SQUAD,
    RESERVE_SQUAD,
    YOUTH_U19
}

@Serializable
data class Club(
    val id: String,
    val name: String,
    val shortName: String,
    val countryId: String,
    val region: String,
    val divisionId: String,
    val reputation: Int,
    val domesticReputation: Int,
    val continentalReputation: Int,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val averageAttendance: Int,
    val trainingFacilitiesLevel: Int, // 1..20
    val youthFacilitiesLevel: Int,    // 1..20
    val youthRecruitmentLevel: Int,   // 1..20
    val medicalFacilitiesLevel: Int = 10,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val badgePattern: BadgePattern = BadgePattern.VERTICAL_STRIPES,
    val customBadgeUri: String? = null,
    val kitSponsorText: String = "GLOBAL SPORT",
    val balance: Long,
    val transferBudget: Long,
    val wageBudgetWeekly: Long,
    val debt: Long = 0L,
    val ticketPrice: Int = 32,
    val seasonTicketHolders: Int = 8000,
    val annualSponsorship: Long = 3_500_000L,
    val annualBroadcastRevenue: Long = 6_000_000L,
    val boardConfidence: Int = 75,
    val supporterConfidence: Int = 75,
    val expectedLeagueFinish: Int = 8,
    val tacticalPhilosophy: String = "Balanced Modern",
    val rivalClubIds: List<String> = emptyList(),
    val leagueTitles: Int = 0,
    val cupTitles: Int = 0,
    val continentalTitles: Int = 0,
    val pointsDeduction: Int = 0,
    val tactic: TacticalSetup = TacticalSetup(),
    val provenance: DataProvenance = DataProvenance.GENERATED_SEED
)

@Serializable
enum class Position(val code: String, val line: Int) {
    GK("GK", 0),
    CB("CB", 1),
    LB("LB", 1),
    RB("RB", 1),
    DM("DM", 2),
    CM("CM", 2),
    AM("AM", 3),
    LW("LW", 3),
    RW("RW", 3),
    ST("ST", 4)
}

@Serializable
enum class PreferredFoot {
    RIGHT, LEFT, BOTH
}

@Serializable
enum class SquadStatus(val label: String) {
    STAR_PLAYER("Star Player"),
    IMPORTANT_STARTER("Important Starter"),
    SQUAD_ROTATION("Squad Rotation"),
    BACKUP("Backup"),
    YOUTH_PROSPECT("Youth Prospect"),
    LOAN_LISTED("Listed for Loan"),
    TRANSFER_LISTED("Transfer Listed")
}

@Serializable
data class TechnicalAttributes(
    val finishing: Int = 55,
    val passing: Int = 55,
    val crossing: Int = 50,
    val dribbling: Int = 55,
    val firstTouch: Int = 55,
    val tackling: Int = 50,
    val marking: Int = 50,
    val heading: Int = 52,
    val longShots: Int = 50,
    val technique: Int = 55,
    val setPieces: Int = 48,
    val penalties: Int = 50
)

@Serializable
data class MentalAttributes(
    val decisions: Int = 55,
    val vision: Int = 55,
    val anticipation: Int = 55,
    val composure: Int = 55,
    val concentration: Int = 55,
    val positioning: Int = 55,
    val teamwork: Int = 60,
    val workRate: Int = 60,
    val leadership: Int = 50,
    val aggression: Int = 55,
    val determination: Int = 60,
    val offTheBall: Int = 55
)

@Serializable
data class PhysicalAttributes(
    val pace: Int = 60,
    val acceleration: Int = 60,
    val stamina: Int = 62,
    val strength: Int = 58,
    val balance: Int = 58,
    val jumping: Int = 56,
    val agility: Int = 60,
    val naturalFitness: Int = 65
)

@Serializable
data class GoalkeepingAttributes(
    val reflexes: Int = 20,
    val handling: Int = 20,
    val oneOnOnes: Int = 20,
    val aerialAbility: Int = 20,
    val communication: Int = 20,
    val commandOfArea: Int = 20,
    val distribution: Int = 20,
    val gkPositioning: Int = 20
)

@Serializable
data class Player(
    val id: String,
    val fullName: String,
    val birthYear: Int,
    val age: Int,
    val nationalityId: String,
    val preferredFoot: PreferredFoot = PreferredFoot.RIGHT,
    val mainPosition: Position,
    val secondaryPositions: List<Position> = emptyList(),
    val clubId: String,
    val loanedFromClubId: String? = null,
    val squadType: SquadTeamType = SquadTeamType.MAIN_SQUAD,
    val squadStatus: SquadStatus = SquadStatus.SQUAD_ROTATION,
    val shirtNumber: Int = 10,
    val contractStartYear: Int = 2026,
    val contractExpiryYear: Int = 2029,
    val wageWeekly: Long = 12_000L,
    val marketValue: Long = 1_500_000L,
    val releaseClause: Long? = null,
    val agentName: String = "Elite Touchline Agency",
    val personality: String = "Model Citizen",
    val professionalism: Int = 68,
    val ambition: Int = 65,
    val loyalty: Int = 62,
    val reputation: Int = 58,
    val morale: Int = 78,
    val matchFitness: Int = 92,
    val fatigue: Int = 10,
    val injuryDaysRemaining: Int = 0,
    val injuryDescription: String? = null,
    val technical: TechnicalAttributes = TechnicalAttributes(),
    val mental: MentalAttributes = MentalAttributes(),
    val physical: PhysicalAttributes = PhysicalAttributes(),
    val goalkeeping: GoalkeepingAttributes = GoalkeepingAttributes(),
    val hiddenPotentialAbility: Int = 76,
    val scoutedKnowledgePercent: Int = 35,
    val individualTrainingFocus: String = "Balanced Role",
    val mentorPlayerId: String? = null,
    val seasonAppearances: Int = 0,
    val seasonGoals: Int = 0,
    val seasonAssists: Int = 0,
    val seasonCleanSheets: Int = 0,
    val seasonYellowCards: Int = 0,
    val seasonRedCards: Int = 0,
    val averageRating: Double = 6.80,
    val careerAppearances: Int = 45,
    val careerGoals: Int = 8,
    val provenance: DataProvenance = DataProvenance.GENERATED_SEED
) {
    fun overallRating(): Int {
        return when (mainPosition) {
            Position.GK -> ((goalkeeping.reflexes * 2 + goalkeeping.handling * 2 +
                goalkeeping.oneOnOnes + goalkeeping.commandOfArea +
                goalkeeping.gkPositioning * 2 + mental.concentration + mental.decisions) / 10).coerceIn(1, 99)
            Position.CB, Position.LB, Position.RB -> ((technical.tackling * 2 + technical.marking * 2 +
                technical.heading + mental.positioning * 2 + mental.anticipation +
                physical.strength + physical.pace) / 10).coerceIn(1, 99)
            Position.DM, Position.CM -> ((technical.passing * 2 + technical.firstTouch +
                technical.tackling + mental.vision * 2 + mental.decisions * 2 +
                mental.workRate + physical.stamina) / 10).coerceIn(1, 99)
            Position.AM, Position.LW, Position.RW -> ((technical.dribbling * 2 + technical.passing * 2 +
                technical.crossing + technical.technique + mental.vision +
                mental.offTheBall + physical.pace + physical.acceleration) / 10).coerceIn(1, 99)
            Position.ST -> ((technical.finishing * 3 + technical.firstTouch * 2 +
                technical.heading + mental.composure * 2 + mental.offTheBall +
                physical.pace) / 10).coerceIn(1, 99)
        }
    }

    fun estimatedPotentialRange(): Pair<Int, Int> {
        val uncertainty = ((100 - scoutedKnowledgePercent) / 5).coerceAtLeast(1)
        val low = (hiddenPotentialAbility - uncertainty).coerceIn(overallRating(), 99)
        val high = (hiddenPotentialAbility + uncertainty).coerceIn(low, 99)
        return low to high
    }
}

@Serializable
enum class StaffRole(val title: String) {
    ASSISTANT_MANAGER("Assistant Manager"),
    HEAD_COACH("First Team Coach"),
    YOUTH_DIRECTOR("Head of Youth Development"),
    CHIEF_SCOUT("Chief Scout"),
    HEAD_PHYSIO("Head Physiotherapist"),
    PERFORMANCE_ANALYST("Performance Analyst")
}

@Serializable
data class StaffMember(
    val id: String,
    val fullName: String,
    val nationalityId: String,
    val clubId: String,
    val role: StaffRole,
    val coachingSkill: Int = 68,
    val tacticalKnowledge: Int = 66,
    val judgingPlayerAbility: Int = 68,
    val judgingPlayerPotential: Int = 70,
    val physiotherapySkill: Int = 68,
    val youthWorking: Int = 70,
    val wageWeekly: Long = 4_500L
)

@Serializable
enum class Formation(
    val label: String,
    val defCount: Int,
    val midCount: Int,
    val attCount: Int,
    val coordinates: List<Pair<Float, Float>>
) {
    F_4_3_3(
        "4-3-3 Wide", 4, 3, 3,
        listOf(
            0.50f to 0.90f,
            0.15f to 0.72f, 0.38f to 0.75f, 0.62f to 0.75f, 0.85f to 0.72f,
            0.50f to 0.56f, 0.30f to 0.46f, 0.70f to 0.46f,
            0.18f to 0.24f, 0.50f to 0.16f, 0.82f to 0.24f
        )
    ),
    F_4_2_3_1(
        "4-2-3-1 Control", 4, 5, 1,
        listOf(
            0.50f to 0.90f,
            0.15f to 0.72f, 0.38f to 0.75f, 0.62f to 0.75f, 0.85f to 0.72f,
            0.35f to 0.56f, 0.65f to 0.56f,
            0.18f to 0.34f, 0.50f to 0.34f, 0.82f to 0.34f,
            0.50f to 0.15f
        )
    ),
    F_4_4_2(
        "4-4-2 Classic", 4, 4, 2,
        listOf(
            0.50f to 0.90f,
            0.15f to 0.72f, 0.38f to 0.75f, 0.62f to 0.75f, 0.85f to 0.72f,
            0.16f to 0.48f, 0.38f to 0.50f, 0.62f to 0.50f, 0.84f to 0.48f,
            0.36f to 0.18f, 0.64f to 0.18f
        )
    ),
    F_3_5_2(
        "3-5-2 Wingbacks", 3, 5, 2,
        listOf(
            0.50f to 0.90f,
            0.26f to 0.75f, 0.50f to 0.77f, 0.74f to 0.75f,
            0.12f to 0.50f, 0.34f to 0.52f, 0.50f to 0.58f, 0.66f to 0.52f, 0.88f to 0.50f,
            0.36f to 0.18f, 0.64f to 0.18f
        )
    ),
    F_3_4_3(
        "3-4-3 High Press", 3, 4, 3,
        listOf(
            0.50f to 0.90f,
            0.25f to 0.74f, 0.50f to 0.76f, 0.75f to 0.74f,
            0.14f to 0.50f, 0.38f to 0.52f, 0.62f to 0.52f, 0.86f to 0.50f,
            0.20f to 0.22f, 0.50f to 0.15f, 0.80f to 0.22f
        )
    ),
    F_4_1_4_1(
        "4-1-4-1 Structured", 4, 5, 1,
        listOf(
            0.50f to 0.90f,
            0.15f to 0.72f, 0.38f to 0.75f, 0.62f to 0.75f, 0.85f to 0.72f,
            0.50f to 0.60f,
            0.15f to 0.42f, 0.38f to 0.44f, 0.62f to 0.44f, 0.85f to 0.42f,
            0.50f to 0.16f
        )
    ),
    F_5_3_2(
        "5-3-2 Low Block", 5, 3, 2,
        listOf(
            0.50f to 0.90f,
            0.12f to 0.70f, 0.30f to 0.76f, 0.50f to 0.78f, 0.70f to 0.76f, 0.88f to 0.70f,
            0.28f to 0.50f, 0.50f to 0.52f, 0.72f to 0.50f,
            0.36f to 0.20f, 0.64f to 0.20f
        )
    ),
    F_4_3_1_2(
        "4-3-1-2 Narrow Diamond", 4, 4, 2,
        listOf(
            0.50f to 0.90f,
            0.15f to 0.72f, 0.38f to 0.75f, 0.62f to 0.75f, 0.85f to 0.72f,
            0.50f to 0.58f, 0.30f to 0.48f, 0.70f to 0.48f,
            0.50f to 0.34f,
            0.36f to 0.17f, 0.64f to 0.17f
        )
    )
}

@Serializable
enum class Mentality(val label: String, val attackModifier: Double, val defenseModifier: Double) {
    ULTRA_DEFENSIVE("Park the Bus", 0.72, 1.25),
    CAUTIOUS("Cautious Counter", 0.88, 1.12),
    BALANCED("Balanced", 1.00, 1.00),
    POSITIVE("Positive Possession", 1.12, 0.92),
    ATTACKING("High Attacking", 1.24, 0.82),
    ALL_OUT_ATTACK("All-Out Overload", 1.38, 0.68)
}

@Serializable
data class TacticalSetup(
    val formation: Formation = Formation.F_4_3_3,
    val mentality: Mentality = Mentality.BALANCED,
    val passingDirectness: Int = 55, // 1..100 (Short -> Direct)
    val tempo: Int = 60,             // 1..100
    val width: Int = 60,             // 1..100
    val pressingIntensity: Int = 65, // 1..100
    val defensiveLine: Int = 58,     // 1..100
    val counterPress: Boolean = true,
    val counterAttack: Boolean = true,
    val playOutOfDefense: Boolean = true,
    val offsideTrap: Boolean = false,
    val timeWasting: Boolean = false,
    val tacklingAggression: Int = 55,
    val tacticalFamiliarity: Int = 78,
    val teamCohesion: Int = 75,
    val captainPlayerId: String? = null,
    val penaltyTakerId: String? = null,
    val freeKickTakerId: String? = null,
    val startingElevenIds: List<String> = emptyList(),
    val benchIds: List<String> = emptyList()
)

@Serializable
enum class CompetitionType {
    DOMESTIC_LEAGUE,
    NATIONAL_CUP,
    CONTINENTAL_CUP
}

@Serializable
data class MatchEvent(
    val minute: Int,
    val type: String, // GOAL, SHOT_SAVED, YELLOW_CARD, RED_CARD, SUBSTITUTION, INJURY, VAR_CHECK, PENALTY
    val clubId: String,
    val primaryPlayerName: String,
    val secondaryPlayerName: String? = null,
    val commentary: String
)

@Serializable
data class Fixture(
    val id: String,
    val competitionId: String,
    val competitionType: CompetitionType,
    val seasonYear: Int,
    val matchday: Int,
    val dayOfYear: Int,
    val roundName: String = "Regular Season",
    val homeClubId: String,
    val awayClubId: String,
    val played: Boolean = false,
    val homeGoals: Int = 0,
    val awayGoals: Int = 0,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val homePossession: Int = 50,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val events: List<MatchEvent> = emptyList(),
    val playerRatings: Map<String, Double> = emptyMap()
)

@Serializable
data class StandingRow(
    val clubId: String,
    val clubName: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val pointsDeduction: Int = 0,
    val points: Int = 0,
    val form: List<String> = emptyList()
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

@Serializable
data class TransferOffer(
    val id: String,
    val playerId: String,
    val playerName: String,
    val fromClubId: String,
    val toClubId: String,
    val feeOffered: Long,
    val offeredWageWeekly: Long,
    val contractYears: Int = 4,
    val sellOnPercentage: Int = 10,
    val goalBonus: Long = 250_000L,
    val isLoan: Boolean = false,
    val loanOptionToBuy: Long? = null,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COUNTERED, COMPLETED
    val reason: String = ""
)

@Serializable
data class LedgerEntry(
    val dayOfYear: Int,
    val seasonYear: Int,
    val category: String,
    val description: String,
    val amount: Long // Positive for income, negative for expense
)

@Serializable
data class InfrastructureProject(
    val id: String,
    val clubId: String,
    val type: String, // STADIUM_EXPANSION, TRAINING_UPGRADE, YOUTH_UPGRADE, MEDICAL_UPGRADE
    val cost: Long,
    val daysRemaining: Int,
    val completed: Boolean = false
)

@Serializable
data class NewsItem(
    val id: String,
    val dayOfYear: Int,
    val seasonYear: Int,
    val category: String, // TRANSFER, MATCH_REPORT, INJURY, BOARD, CUP_DRAW, AWARD, PRESS
    val headline: String,
    val body: String,
    val relatedClubId: String? = null
)

@Serializable
data class PressQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val moraleEffects: List<Int>,
    val boardEffects: List<Int>
)

@Serializable
data class SeasonAwardRecord(
    val seasonYear: Int,
    val divisionId: String,
    val divisionName: String,
    val championClubName: String,
    val runnerUpClubName: String,
    val topScorerName: String,
    val topScorerGoals: Int,
    val playerOfTheSeasonName: String,
    val bestYoungPlayerName: String
)

@Serializable
data class ValidationIssue(
    val severity: String, // ERROR, WARNING
    val code: String,
    val message: String
)

@Serializable
data class WorldDatabasePack(
    val schemaVersion: Int = 1,
    val packName: String = "World Football Director Default 108-Division Pack",
    val author: String = "World Football Director Engine",
    val countries: List<Country>,
    val divisions: List<DivisionDefinition>,
    val nationalCups: List<NationalCupDefinition>,
    val continentalCompetitions: List<ContinentalCompetitionDefinition>,
    val clubs: List<Club> = emptyList(),
    val players: List<Player> = emptyList()
)

@Serializable
data class CareerSaveState(
    val saveId: String,
    val saveName: String,
    val createdTimestamp: Long,
    val managerName: String,
    val managerNationalityId: String,
    val managerReputation: Int = 60,
    val userClubId: String,
    val seasonYear: Int = 2026,
    val dayOfYear: Int = 1, // 1..365
    val databasePreset: DatabasePreset = DatabasePreset.STANDARD,
    val simulationDetail: SimulationDetailLevel = SimulationDetailLevel.BALANCED,
    val activeDivisionIds: List<String>,
    val countries: List<Country>,
    val divisions: List<DivisionDefinition>,
    val nationalCups: List<NationalCupDefinition>,
    val continentalCompetitions: List<ContinentalCompetitionDefinition>,
    val clubs: List<Club>,
    val players: List<Player>,
    val staff: List<StaffMember>,
    val fixtures: List<Fixture>,
    val transferOffers: List<TransferOffer> = emptyList(),
    val shortlistPlayerIds: List<String> = emptyList(),
    val ledger: List<LedgerEntry> = emptyList(),
    val projects: List<InfrastructureProject> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val awardsHistory: List<SeasonAwardRecord> = emptyList(),
    val inGameEditorEnabled: Boolean = true
)
