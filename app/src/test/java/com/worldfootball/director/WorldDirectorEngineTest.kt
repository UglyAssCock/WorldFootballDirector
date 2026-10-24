package com.worldfootball.director

import com.worldfootball.director.data.WorldDatabaseSeeder
import com.worldfootball.director.engine.*
import com.worldfootball.director.model.*
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class WorldDirectorEngineTest {

    @Test
    fun `world database pack contains at least 100 real domestic divisions`() {
        val pack = WorldDatabaseSeeder.buildDefaultWorldPack()
        assertTrue("Expected at least 100 real divisions, found ${pack.divisions.size}", pack.divisions.size >= 100)
        assertTrue(pack.countries.size >= 55)
        assertTrue(pack.nationalCups.isNotEmpty())
        assertTrue(pack.continentalCompetitions.isNotEmpty())
    }

    @Test
    fun `round robin generator creates complete schedule with zero duplicate fixtures`() {
        val clubIds = (1..10).map { "CLUB_$it" }
        val fixtures = CompetitionEngine.generateRoundRobinFixtures(
            competitionId = "ENG_D1",
            seasonYear = 2026,
            clubIds = clubIds,
            doubleRoundRobin = true
        )
        // 10 teams double round robin = 10 * 9 = 90 matches
        assertEquals(90, fixtures.size)

        val pairings = fixtures.map { "${it.homeClubId}_vs_${it.awayClubId}" }.toSet()
        assertEquals("Every ordered home/away pairing must be unique", 90, pairings.size)
    }

    @Test
    fun `deterministic match engine produces identical score with same seed`() {
        val state = WorldDatabaseSeeder.createNewCareerState(
            saveName = "TestSave",
            managerName = "Tester",
            managerNationalityId = "ENG",
            preset = DatabasePreset.SMALL,
            simulationDetail = SimulationDetailLevel.BALANCED,
            activeDivisionIds = listOf("ENG_D1"),
            selectedUserClubId = null
        )
        val fixture = state.fixtures.first { it.competitionId == "ENG_D1" }
        val home = state.clubs.first { it.id == fixture.homeClubId }
        val away = state.clubs.first { it.id == fixture.awayClubId }
        val homePlayers = state.players.filter { it.clubId == home.id }
        val awayPlayers = state.players.filter { it.clubId == away.id }

        val run1 = MatchEngine.simulateMatch(fixture, home, away, homePlayers, awayPlayers, seed = 99999L)
        val run2 = MatchEngine.simulateMatch(fixture, home, away, homePlayers, awayPlayers, seed = 99999L)

        assertTrue(run1.played)
        assertEquals(run1.homeGoals, run2.homeGoals)
        assertEquals(run1.awayGoals, run2.awayGoals)
        assertEquals(run1.events.size, run2.events.size)
    }

    @Test
    fun `promotion and relegation moves clubs between tiers at season end`() {
        val state = WorldDatabaseSeeder.createNewCareerState(
            saveName = "PromoTest",
            managerName = "Tester",
            managerNationalityId = "ENG",
            preset = DatabasePreset.SMALL,
            simulationDetail = SimulationDetailLevel.FAST_MOBILE,
            activeDivisionIds = listOf("ENG_D1", "ENG_D2"),
            selectedUserClubId = null
        )

        val playedFixtures = state.fixtures.mapIndexed { idx, f ->
            f.copy(played = true, homeGoals = (idx % 3) + 1, awayGoals = idx % 2)
        }

        val d1Before = state.clubs.filter { it.divisionId == "ENG_D1" }.map { it.id }.toSet()
        val d2Before = state.clubs.filter { it.divisionId == "ENG_D2" }.map { it.id }.toSet()

        val updatedClubs = CompetitionEngine.applyPromotionAndRelegation(
            countryId = "ENG",
            divisions = state.divisions.filter { it.id in listOf("ENG_D1", "ENG_D2") },
            clubs = state.clubs,
            fixtures = playedFixtures
        )

        val d1After = updatedClubs.filter { it.divisionId == "ENG_D1" }.map { it.id }.toSet()
        val d2After = updatedClubs.filter { it.divisionId == "ENG_D2" }.map { it.id }.toSet()

        assertNotEquals("Promotion and relegation must swap clubs between D1 and D2", d1Before, d1After)
        assertNotEquals("Promotion and relegation must swap clubs between D1 and D2", d2Before, d2After)
    }

    @Test
    fun `transfer engine enforces budget and completes valid transfers`() {
        val state = WorldDatabaseSeeder.createNewCareerState(
            saveName = "TransferTest",
            managerName = "Tester",
            managerNationalityId = "ALG",
            preset = DatabasePreset.SMALL,
            simulationDetail = SimulationDetailLevel.BALANCED,
            activeDivisionIds = listOf("ALG_D1"),
            selectedUserClubId = null
        )
        val buyingClub = state.clubs.first { it.id == state.userClubId }
        val targetPlayer = state.players.first { it.clubId != buyingClub.id }
        val sellingClub = state.clubs.first { it.id == targetPlayer.clubId }

        val validOffer = TransferOffer(
            id = "TEST_OFFER_1",
            playerId = targetPlayer.id,
            playerName = targetPlayer.fullName,
            fromClubId = sellingClub.id,
            toClubId = buyingClub.id,
            feeOffered = targetPlayer.marketValue * 2,
            offeredWageWeekly = targetPlayer.wageWeekly * 2
        )

        val evaluated = TransferAndContractEngine.evaluateTransferOffer(
            offer = validOffer,
            player = targetPlayer,
            sellingClub = sellingClub,
            buyingClub = buyingClub.copy(transferBudget = targetPlayer.marketValue * 5),
            buyingClubPlayers = state.players.filter { it.clubId == buyingClub.id },
            buyingDivision = state.divisions.first { it.id == buyingClub.divisionId }
        )

        assertEquals("ACCEPTED", evaluated.status)

        val afterTransfer = TransferAndContractEngine.executeAcceptedTransfer(state, evaluated)
        val movedPlayer = afterTransfer.players.first { it.id == targetPlayer.id }
        assertEquals(buyingClub.id, movedPlayer.clubId)
    }

    @Test
    fun `database validator and json serialization roundtrip succeed`() {
        val state = WorldDatabaseSeeder.createNewCareerState(
            saveName = "ValidationTest",
            managerName = "Tester",
            managerNationalityId = "ALG",
            preset = DatabasePreset.SMALL,
            simulationDetail = SimulationDetailLevel.BALANCED,
            activeDivisionIds = listOf("ALG_D1"),
            selectedUserClubId = null
        )
        val issues = DatabaseValidator.validateCareerState(state)
        assertTrue("Expected zero validation errors, found: $issues", issues.isEmpty())

        val encoded = WorldDatabaseSeeder.jsonFormat.encodeToString(state)
        val decoded = WorldDatabaseSeeder.jsonFormat.decodeFromString<CareerSaveState>(encoded)
        assertEquals(state.saveId, decoded.saveId)
        assertEquals(state.divisions.size, decoded.divisions.size)
    }
}
