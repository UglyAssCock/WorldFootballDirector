package com.worldfootball.director.data

import android.content.Context
import com.worldfootball.director.engine.CompetitionEngine
import com.worldfootball.director.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random

object WorldDatabaseSeeder {

    val jsonFormat = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun buildDefaultWorldPack(): WorldDatabasePack {
        val countries = buildCountries()
        val divisions = build108RealDivisions()
        val cups = buildNationalCups(countries)
        val continental = buildContinentalCompetitions()
        return WorldDatabasePack(
            schemaVersion = 1,
            packName = "World Football Director 108-Division Global Pack",
            author = "World Football Director",
            countries = countries,
            divisions = divisions,
            nationalCups = cups,
            continentalCompetitions = continental
        )
    }

    fun buildCountries(): List<Country> = listOf(
        // EUROPE (29)
        Country("ENG", "England", Continent.EUROPE, 1800, 88, "#FFFFFF", "#CE1124"),
        Country("ESP", "Spain", Continent.EUROPE, 1810, 90, "#AA151B", "#F1BF00"),
        Country("GER", "Germany", Continent.EUROPE, 1760, 87, "#FFFFFF", "#000000"),
        Country("ITA", "Italy", Continent.EUROPE, 1750, 86, "#0064AA", "#FFFFFF"),
        Country("FRA", "France", Continent.EUROPE, 1820, 91, "#002654", "#FFFFFF"),
        Country("POR", "Portugal", Continent.EUROPE, 1730, 85, "#DA291C", "#046A38"),
        Country("NED", "Netherlands", Continent.EUROPE, 1740, 84, "#F36C21", "#FFFFFF"),
        Country("BEL", "Belgium", Continent.EUROPE, 1710, 80, "#E30613", "#000000"),
        Country("SCO", "Scotland", Continent.EUROPE, 1520, 68, "#003399", "#FFFFFF"),
        Country("WAL", "Wales", Continent.EUROPE, 1490, 62, "#D30731", "#00AB39"),
        Country("NIR", "Northern Ireland", Continent.EUROPE, 1410, 58, "#006633", "#FFFFFF"),
        Country("IRL", "Republic of Ireland", Continent.EUROPE, 1435, 62, "#169B62", "#FFFFFF"),
        Country("AUT", "Austria", Continent.EUROPE, 1580, 72, "#ED2939", "#FFFFFF"),
        Country("SUI", "Switzerland", Continent.EUROPE, 1610, 73, "#DA291C", "#FFFFFF"),
        Country("TUR", "Turkey", Continent.EUROPE, 1595, 76, "#E30A17", "#FFFFFF"),
        Country("GRE", "Greece", Continent.EUROPE, 1515, 70, "#0D5EAF", "#FFFFFF"),
        Country("DEN", "Denmark", Continent.EUROPE, 1600, 74, "#C60C30", "#FFFFFF"),
        Country("SWE", "Sweden", Continent.EUROPE, 1530, 70, "#FECC02", "#006AA7"),
        Country("NOR", "Norway", Continent.EUROPE, 1545, 72, "#BA0C2F", "#00205B"),
        Country("FIN", "Finland", Continent.EUROPE, 1415, 60, "#FFFFFF", "#002F6C"),
        Country("POL", "Poland", Continent.EUROPE, 1535, 71, "#FFFFFF", "#DC143C"),
        Country("CZE", "Czechia", Continent.EUROPE, 1550, 72, "#D7141A", "#11457E"),
        Country("SVK", "Slovakia", Continent.EUROPE, 1485, 66, "#0B4EA2", "#EE1C25"),
        Country("CRO", "Croatia", Continent.EUROPE, 1680, 82, "#FF0000", "#0093DD"),
        Country("SRB", "Serbia", Continent.EUROPE, 1540, 77, "#C6363C", "#0C4076"),
        Country("ROU", "Romania", Continent.EUROPE, 1495, 68, "#FCD116", "#002B7F"),
        Country("BUL", "Bulgaria", Continent.EUROPE, 1410, 62, "#FFFFFF", "#00966E"),
        Country("HUN", "Hungary", Continent.EUROPE, 1525, 67, "#CD2A3E", "#436F4D"),
        Country("UKR", "Ukraine", Continent.EUROPE, 1565, 74, "#FFD700", "#0057B7"),

        // SOUTH AMERICA (10)
        Country("BRA", "Brazil", Continent.SOUTH_AMERICA, 1815, 92, "#FFDF00", "#009C3B"),
        Country("ARG", "Argentina", Continent.SOUTH_AMERICA, 1850, 90, "#74ACDF", "#FFFFFF"),
        Country("URU", "Uruguay", Continent.SOUTH_AMERICA, 1690, 81, "#5BC2E7", "#000000"),
        Country("CHI", "Chile", Continent.SOUTH_AMERICA, 1510, 73, "#D52B1E", "#0039A6"),
        Country("COL", "Colombia", Continent.SOUTH_AMERICA, 1670, 80, "#FCD116", "#003893"),
        Country("PAR", "Paraguay", Continent.SOUTH_AMERICA, 1490, 71, "#D52B1E", "#FFFFFF"),
        Country("PER", "Peru", Continent.SOUTH_AMERICA, 1480, 68, "#D91023", "#FFFFFF"),
        Country("ECU", "Ecuador", Continent.SOUTH_AMERICA, 1570, 75, "#FFD100", "#0033A0"),
        Country("BOL", "Bolivia", Continent.SOUTH_AMERICA, 1380, 62, "#007934", "#F9E300"),
        Country("VEN", "Venezuela", Continent.SOUTH_AMERICA, 1475, 69, "#8B0000", "#FFCC00"),

        // NORTH & CENTRAL AMERICA (6)
        Country("USA", "United States", Continent.NORTH_CENTRAL_AMERICA, 1640, 74, "#002868", "#BF0A30"),
        Country("CAN", "Canada", Continent.NORTH_CENTRAL_AMERICA, 1530, 66, "#FF0000", "#FFFFFF"),
        Country("MEX", "Mexico", Continent.NORTH_CENTRAL_AMERICA, 1650, 78, "#006847", "#FFFFFF"),
        Country("CRC", "Costa Rica", Continent.NORTH_CENTRAL_AMERICA, 1460, 65, "#CE1126", "#002B7F"),
        Country("HON", "Honduras", Continent.NORTH_CENTRAL_AMERICA, 1390, 63, "#0073CF", "#FFFFFF"),
        Country("GUA", "Guatemala", Continent.NORTH_CENTRAL_AMERICA, 1340, 58, "#4997D0", "#FFFFFF"),

        // AFRICA (11)
        Country("ALG", "Algeria", Continent.AFRICA, 1540, 77, "#006233", "#FFFFFF"),
        Country("MAR", "Morocco", Continent.AFRICA, 1675, 81, "#C1272D", "#006233"),
        Country("TUN", "Tunisia", Continent.AFRICA, 1495, 72, "#E70013", "#FFFFFF"),
        Country("EGY", "Egypt", Continent.AFRICA, 1555, 76, "#CE1126", "#000000"),
        Country("RSA", "South Africa", Continent.AFRICA, 1470, 71, "#FFB81C", "#007749"),
        Country("NGA", "Nigeria", Continent.AFRICA, 1560, 82, "#008751", "#FFFFFF"),
        Country("GHA", "Ghana", Continent.AFRICA, 1450, 77, "#FFFFFF", "#006B3F"),
        Country("SEN", "Senegal", Continent.AFRICA, 1620, 79, "#00853F", "#FDEF42"),
        Country("CMR", "Cameroon", Continent.AFRICA, 1480, 76, "#007A5E", "#CE1126"),
        Country("CIV", "Ivory Coast", Continent.AFRICA, 1575, 80, "#F77F00", "#009E60"),
        Country("COD", "DR Congo", Continent.AFRICA, 1445, 72, "#007FFF", "#F7D618"),

        // ASIA & OCEANIA (12)
        Country("JPN", "Japan", Continent.ASIA, 1655, 79, "#000555", "#FFFFFF"),
        Country("KOR", "South Korea", Continent.ASIA, 1615, 76, "#CD2E3A", "#0047A0"),
        Country("CHN", "China", Continent.ASIA, 1360, 62, "#EE1C25", "#FFFF00"),
        Country("KSA", "Saudi Arabia", Continent.ASIA, 1485, 70, "#006C35", "#FFFFFF"),
        Country("QAT", "Qatar", Continent.ASIA, 1470, 66, "#8A1538", "#FFFFFF"),
        Country("UAE", "United Arab Emirates", Continent.ASIA, 1430, 64, "#FFFFFF", "#00732F"),
        Country("IND", "India", Continent.ASIA, 1250, 56, "#138808", "#FF9933"),
        Country("IDN", "Indonesia", Continent.ASIA, 1310, 62, "#FF0000", "#FFFFFF"),
        Country("MAS", "Malaysia", Continent.ASIA, 1295, 58, "#FFCC00", "#000066"),
        Country("THA", "Thailand", Continent.ASIA, 1335, 61, "#00247D", "#EF3340"),
        Country("AUS", "Australia", Continent.ASIA, 1580, 71, "#FFCD00", "#00843D"),
        Country("NZL", "New Zealand", Continent.OCEANIA, 1350, 60, "#000000", "#FFFFFF")
    )

    fun build108RealDivisions(): List<DivisionDefinition> {
        val specs = listOf(
            // England (5)
            DivSpec("ENG_D1", "ENG", "English Premier League", 1, 20, 0, 0, 3, 95_000_000L),
            DivSpec("ENG_D2", "ENG", "EFL Championship", 2, 24, 2, 4, 3, 28_000_000L),
            DivSpec("ENG_D3", "ENG", "EFL League One", 3, 24, 2, 4, 4, 9_000_000L),
            DivSpec("ENG_D4", "ENG", "EFL League Two", 4, 24, 3, 4, 2, 4_500_000L),
            DivSpec("ENG_D5", "ENG", "National League", 5, 24, 1, 6, 4, 2_000_000L),
            // Spain (3)
            DivSpec("ESP_D1", "ESP", "LaLiga EA Sports", 1, 20, 0, 0, 3, 75_000_000L),
            DivSpec("ESP_D2", "ESP", "LaLiga Hypermotion", 2, 22, 2, 4, 4, 18_000_000L),
            DivSpec("ESP_D3", "ESP", "Primera Federación", 3, 20, 2, 4, 4, 4_000_000L),
            // Germany (3)
            DivSpec("GER_D1", "GER", "Bundesliga", 1, 18, 0, 0, 2, 72_000_000L),
            DivSpec("GER_D2", "GER", "2. Bundesliga", 2, 18, 2, 1, 2, 22_000_000L),
            DivSpec("GER_D3", "GER", "3. Liga", 3, 20, 2, 1, 4, 5_500_000L),
            // Italy (3)
            DivSpec("ITA_D1", "ITA", "Serie A", 1, 20, 0, 0, 3, 68_000_000L),
            DivSpec("ITA_D2", "ITA", "Serie B", 2, 20, 2, 6, 3, 16_000_000L),
            DivSpec("ITA_D3", "ITA", "Serie C", 3, 20, 3, 4, 3, 4_000_000L),
            // France (3)
            DivSpec("FRA_D1", "FRA", "Ligue 1", 1, 18, 0, 0, 2, 55_000_000L),
            DivSpec("FRA_D2", "FRA", "Ligue 2", 2, 18, 2, 3, 2, 14_000_000L),
            DivSpec("FRA_D3", "FRA", "Championnat National", 3, 18, 2, 1, 3, 3_500_000L),
            // Portugal (2)
            DivSpec("POR_D1", "POR", "Primeira Liga", 1, 18, 0, 0, 2, 28_000_000L),
            DivSpec("POR_D2", "POR", "Liga Portugal 2", 2, 18, 2, 1, 2, 6_000_000L),
            // Netherlands (2)
            DivSpec("NED_D1", "NED", "Eredivisie", 1, 18, 0, 0, 2, 30_000_000L),
            DivSpec("NED_D2", "NED", "Keuken Kampioen Divisie", 2, 20, 2, 6, 0, 6_500_000L),
            // Belgium (2)
            DivSpec("BEL_D1", "BEL", "Belgian Pro League", 1, 16, 0, 0, 2, 24_000_000L),
            DivSpec("BEL_D2", "BEL", "Challenger Pro League", 2, 16, 2, 4, 2, 5_000_000L),
            // Scotland (3)
            DivSpec("SCO_D1", "SCO", "Scottish Premiership", 1, 12, 0, 0, 1, 14_000_000L),
            DivSpec("SCO_D2", "SCO", "Scottish Championship", 2, 10, 1, 3, 1, 4_000_000L),
            DivSpec("SCO_D3", "SCO", "Scottish League One", 3, 10, 1, 3, 1, 1_500_000L),
            // Wales, NI, Ireland (4)
            DivSpec("WAL_D1", "WAL", "Cymru Premier", 1, 12, 0, 0, 2, 1_800_000L),
            DivSpec("NIR_D1", "NIR", "NIFL Premiership", 1, 12, 0, 0, 1, 2_000_000L),
            DivSpec("IRL_D1", "IRL", "League of Ireland Premier Division", 1, 10, 0, 0, 1, 3_000_000L),
            DivSpec("IRL_D2", "IRL", "League of Ireland First Division", 2, 10, 1, 4, 0, 1_100_000L),
            // Austria, Switzerland, Turkey, Greece (8)
            DivSpec("AUT_D1", "AUT", "Austrian Bundesliga", 1, 12, 0, 0, 1, 16_000_000L),
            DivSpec("AUT_D2", "AUT", "2. Liga Austria", 2, 16, 1, 0, 3, 4_200_000L),
            DivSpec("SUI_D1", "SUI", "Swiss Super League", 1, 12, 0, 0, 1, 18_000_000L),
            DivSpec("SUI_D2", "SUI", "Swiss Challenge League", 2, 10, 1, 1, 1, 4_500_000L),
            DivSpec("TUR_D1", "TUR", "Süper Lig", 1, 18, 0, 0, 3, 32_000_000L),
            DivSpec("TUR_D2", "TUR", "TFF First League", 2, 18, 2, 5, 3, 7_500_000L),
            DivSpec("GRE_D1", "GRE", "Super League Greece", 1, 14, 0, 0, 2, 18_000_000L),
            DivSpec("GRE_D2", "GRE", "Super League Greece 2", 2, 16, 2, 0, 3, 4_000_000L),
            // Nordics (7)
            DivSpec("DEN_D1", "DEN", "Danish Superliga", 1, 12, 0, 0, 2, 16_000_000L),
            DivSpec("DEN_D2", "DEN", "Danish 1st Division", 2, 12, 2, 0, 2, 4_200_000L),
            DivSpec("SWE_D1", "SWE", "Allsvenskan", 1, 16, 0, 0, 2, 14_000_000L),
            DivSpec("SWE_D2", "SWE", "Superettan", 2, 16, 2, 1, 2, 3_800_000L),
            DivSpec("NOR_D1", "NOR", "Eliteserien", 1, 16, 0, 0, 2, 15_000_000L),
            DivSpec("NOR_D2", "NOR", "OBOS-ligaen", 2, 16, 2, 4, 2, 4_000_000L),
            DivSpec("FIN_D1", "FIN", "Veikkausliiga", 1, 12, 0, 0, 1, 4_500_000L),
            // Central & Eastern Europe (13)
            DivSpec("POL_D1", "POL", "Ekstraklasa", 1, 18, 0, 0, 3, 18_000_000L),
            DivSpec("POL_D2", "POL", "I Liga Poland", 2, 18, 2, 4, 3, 4_800_000L),
            DivSpec("CZE_D1", "CZE", "Czech First League", 1, 16, 0, 0, 2, 14_000_000L),
            DivSpec("CZE_D2", "CZE", "Czech National Football League", 2, 16, 1, 2, 2, 3_500_000L),
            DivSpec("SVK_D1", "SVK", "Slovak First Football League", 1, 12, 0, 0, 1, 6_500_000L),
            DivSpec("CRO_D1", "CRO", "HNL Croatia", 1, 10, 0, 0, 1, 12_000_000L),
            DivSpec("CRO_D2", "CRO", "Prva NL", 2, 12, 1, 0, 1, 2_800_000L),
            DivSpec("SRB_D1", "SRB", "Serbian SuperLiga", 1, 16, 0, 0, 2, 11_000_000L),
            DivSpec("SRB_D2", "SRB", "Serbian First League", 2, 16, 2, 2, 4, 2_500_000L),
            DivSpec("ROU_D1", "ROU", "Liga I Romania", 1, 16, 0, 0, 2, 11_000_000L),
            DivSpec("BUL_D1", "BUL", "First Professional Football League", 1, 16, 0, 0, 2, 7_500_000L),
            DivSpec("HUN_D1", "HUN", "Nemzeti Bajnokság I", 1, 12, 0, 0, 2, 10_000_000L),
            DivSpec("UKR_D1", "UKR", "Ukrainian Premier League", 1, 16, 0, 0, 2, 13_000_000L),
            // South America (15)
            DivSpec("BRA_D1", "BRA", "Brasileirão Série A", 1, 20, 0, 0, 4, 48_000_000L),
            DivSpec("BRA_D2", "BRA", "Brasileirão Série B", 2, 20, 4, 0, 4, 14_000_000L),
            DivSpec("BRA_D3", "BRA", "Brasileirão Série C", 3, 20, 4, 0, 4, 4_200_000L),
            DivSpec("ARG_D1", "ARG", "Liga Profesional Argentina", 1, 20, 0, 0, 2, 34_000_000L),
            DivSpec("ARG_D2", "ARG", "Primera Nacional", 2, 20, 2, 4, 2, 8_500_000L),
            DivSpec("URU_D1", "URU", "Uruguayan Primera División", 1, 16, 0, 0, 3, 9_500_000L),
            DivSpec("URU_D2", "URU", "Uruguayan Segunda División", 2, 14, 3, 0, 2, 2_400_000L),
            DivSpec("CHI_D1", "CHI", "Campeonato Nacional Chile", 1, 16, 0, 0, 2, 11_500_000L),
            DivSpec("CHI_D2", "CHI", "Primera B de Chile", 2, 16, 2, 0, 1, 3_000_000L),
            DivSpec("COL_D1", "COL", "Categoría Primera A", 1, 20, 0, 0, 2, 13_500_000L),
            DivSpec("COL_D2", "COL", "Categoría Primera B", 2, 16, 2, 0, 0, 3_200_000L),
            DivSpec("PAR_D1", "PAR", "Paraguayan Primera División", 1, 12, 0, 0, 2, 8_000_000L),
            DivSpec("PER_D1", "PER", "Liga 1 Peru", 1, 18, 0, 0, 3, 8_500_000L),
            DivSpec("ECU_D1", "ECU", "LigaPro Serie A", 1, 16, 0, 0, 2, 10_500_000L),
            DivSpec("BOL_D1", "BOL", "División Profesional Bolivia", 1, 16, 0, 0, 2, 5_500_000L),
            DivSpec("VEN_D1", "VEN", "Liga FUTVE", 1, 14, 0, 0, 1, 5_200_000L),
            // North & Central America (8)
            DivSpec("USA_D1", "USA", "Major League Soccer", 1, 20, 0, 8, 0, 38_000_000L),
            DivSpec("USA_D2", "USA", "USL Championship", 2, 18, 0, 8, 0, 7_500_000L),
            DivSpec("CAN_D1", "CAN", "Canadian Premier League", 1, 10, 0, 4, 0, 4_500_000L),
            DivSpec("MEX_D1", "MEX", "Liga MX", 1, 18, 0, 8, 0, 36_000_000L),
            DivSpec("MEX_D2", "MEX", "Liga de Expansión MX", 2, 16, 0, 6, 0, 7_000_000L),
            DivSpec("CRC_D1", "CRC", "Liga FPD Costa Rica", 1, 12, 0, 4, 1, 4_200_000L),
            DivSpec("HON_D1", "HON", "Liga Nacional Honduras", 1, 10, 0, 4, 1, 3_200_000L),
            DivSpec("GUA_D1", "GUA", "Liga Nacional Guatemala", 1, 12, 0, 4, 2, 2_800_000L),
            // Africa (15)
            DivSpec("ALG_D1", "ALG", "Ligue Professionnelle 1", 1, 16, 0, 0, 2, 10_500_000L),
            DivSpec("ALG_D2", "ALG", "Ligue 2 Algeria", 2, 16, 2, 0, 3, 3_400_000L),
            DivSpec("MAR_D1", "MAR", "Botola Pro Inwi", 1, 16, 0, 0, 2, 12_500_000L),
            DivSpec("MAR_D2", "MAR", "Botola Pro 2", 2, 16, 2, 0, 2, 3_800_000L),
            DivSpec("TUN_D1", "TUN", "Ligue Professionnelle 1 Tunisia", 1, 16, 0, 0, 2, 9_000_000L),
            DivSpec("EGY_D1", "EGY", "Egyptian Premier League", 1, 18, 0, 0, 3, 14_000_000L),
            DivSpec("EGY_D2", "EGY", "Egyptian Second Division A", 2, 18, 3, 0, 3, 4_000_000L),
            DivSpec("RSA_D1", "RSA", "Betway Premiership", 1, 16, 0, 0, 2, 12_000_000L),
            DivSpec("RSA_D2", "RSA", "Motsepe Foundation Championship", 2, 16, 2, 0, 2, 3_500_000L),
            DivSpec("NGA_D1", "NGA", "Nigeria Premier Football League", 1, 20, 0, 0, 4, 6_500_000L),
            DivSpec("GHA_D1", "GHA", "Ghana Premier League", 1, 18, 0, 0, 3, 5_000_000L),
            DivSpec("SEN_D1", "SEN", "Ligue 1 Senegal", 1, 14, 0, 0, 2, 4_800_000L),
            DivSpec("CMR_D1", "CMR", "Elite One Cameroon", 1, 16, 0, 0, 3, 4_500_000L),
            DivSpec("CIV_D1", "CIV", "Ligue 1 Ivory Coast", 1, 16, 0, 0, 2, 5_200_000L),
            DivSpec("COD_D1", "COD", "Linafoot Ligue 1", 1, 16, 0, 0, 2, 5_000_000L),
            // Asia & Oceania (16)
            DivSpec("JPN_D1", "JPN", "J1 League", 1, 20, 0, 0, 3, 30_000_000L),
            DivSpec("JPN_D2", "JPN", "J2 League", 2, 20, 3, 0, 3, 10_000_000L),
            DivSpec("JPN_D3", "JPN", "J3 League", 3, 20, 3, 0, 0, 3_500_000L),
            DivSpec("KOR_D1", "KOR", "K League 1", 1, 12, 0, 0, 2, 20_000_000L),
            DivSpec("KOR_D2", "KOR", "K League 2", 2, 14, 2, 0, 0, 6_500_000L),
            DivSpec("CHN_D1", "CHN", "Chinese Super League", 1, 16, 0, 0, 2, 22_000_000L),
            DivSpec("CHN_D2", "CHN", "China League One", 2, 16, 2, 0, 2, 6_000_000L),
            DivSpec("KSA_D1", "KSA", "Saudi Pro League", 1, 18, 0, 0, 3, 55_000_000L),
            DivSpec("KSA_D2", "KSA", "Saudi First Division League", 2, 18, 3, 0, 3, 12_000_000L),
            DivSpec("QAT_D1", "QAT", "Qatar Stars League", 1, 12, 0, 0, 1, 24_000_000L),
            DivSpec("UAE_D1", "UAE", "UAE Pro League", 1, 14, 0, 0, 2, 20_000_000L),
            DivSpec("IND_D1", "IND", "Indian Super League", 1, 14, 0, 4, 0, 9_000_000L),
            DivSpec("IDN_D1", "IDN", "Liga 1 Indonesia", 1, 18, 0, 0, 3, 8_000_000L),
            DivSpec("MAS_D1", "MAS", "Malaysia Super League", 1, 14, 0, 0, 0, 7_000_000L),
            DivSpec("THA_D1", "THA", "Thai League 1", 1, 16, 0, 0, 3, 8_500_000L),
            DivSpec("AUS_D1", "AUS", "A-League Men", 1, 12, 0, 6, 0, 12_500_000L),
            DivSpec("NZL_D1", "NZL", "New Zealand National League", 1, 10, 0, 2, 0, 3_000_000L)
        )

        return specs.map { s ->
            DivisionDefinition(
                id = s.id,
                countryId = s.countryId,
                name = s.name,
                tier = s.tier,
                teamCount = s.teams,
                doubleRoundRobin = true,
                promotionSpots = s.promo,
                playoffSpots = s.playoff,
                relegationSpots = s.releg,
                maxForeignPlayersInSquad = if (s.tier == 1) 8 else 5,
                maxSquadSize = 26,
                prizePoolMoney = s.prize,
                hasVar = s.tier <= 2,
                provenance = DataProvenance.VERIFIED_STRUCTURE
            )
        }
    }

    private data class DivSpec(
        val id: String,
        val countryId: String,
        val name: String,
        val tier: Int,
        val teams: Int,
        val promo: Int,
        val playoff: Int,
        val releg: Int,
        val prize: Long
    )

    fun buildNationalCups(countries: List<Country>): List<NationalCupDefinition> {
        val namedCups = mapOf(
            "ENG" to "FA Cup",
            "ESP" to "Copa del Rey",
            "GER" to "DFB-Pokal",
            "ITA" to "Coppa Italia",
            "FRA" to "Coupe de France",
            "NED" to "KNVB Cup",
            "POR" to "Taça de Portugal",
            "SCO" to "Scottish Cup",
            "ALG" to "Algerian Cup (Coupe d'Algérie)",
            "MAR" to "Moroccan Throne Cup",
            "TUN" to "Tunisian Cup",
            "EGY" to "Egypt Cup",
            "BRA" to "Copa do Brasil",
            "ARG" to "Copa Argentina",
            "USA" to "U.S. Open Cup",
            "CAN" to "Canadian Championship",
            "JPN" to "Emperor's Cup",
            "KSA" to "King's Cup"
        )
        return countries.map { c ->
            NationalCupDefinition(
                id = "CUP_${c.id}",
                countryId = c.id,
                name = namedCups[c.id] ?: "${c.name} National Cup",
                twoLeggedSemiFinals = c.id in listOf("ESP", "ITA", "BRA"),
                extraTimeEnabled = true,
                replaysEnabled = false,
                prizeMoneyWinner = (c.fifaRankingPoints * 3_500L).coerceAtLeast(1_500_000L),
                provenance = DataProvenance.VERIFIED_STRUCTURE
            )
        }
    }

    fun buildContinentalCompetitions(): List<ContinentalCompetitionDefinition> = listOf(
        ContinentalCompetitionDefinition("UEFA_CL", Continent.EUROPE, "UEFA Champions League", 1, 32, true, 45_000_000L),
        ContinentalCompetitionDefinition("UEFA_EL", Continent.EUROPE, "UEFA Europa League", 2, 32, true, 22_000_000L),
        ContinentalCompetitionDefinition("UEFA_ECL", Continent.EUROPE, "UEFA Conference League", 3, 32, true, 12_000_000L),
        ContinentalCompetitionDefinition("CONMEBOL_LIB", Continent.SOUTH_AMERICA, "Copa Libertadores", 1, 32, false, 26_000_000L),
        ContinentalCompetitionDefinition("CONMEBOL_SUD", Continent.SOUTH_AMERICA, "Copa Sudamericana", 2, 32, false, 12_000_000L),
        ContinentalCompetitionDefinition("CONCACAF_CC", Continent.NORTH_CENTRAL_AMERICA, "CONCACAF Champions Cup", 1, 16, false, 14_000_000L),
        ContinentalCompetitionDefinition("CAF_CL", Continent.AFRICA, "CAF Champions League", 1, 16, false, 12_000_000L),
        ContinentalCompetitionDefinition("CAF_CC", Continent.AFRICA, "CAF Confederation Cup", 2, 16, false, 6_000_000L),
        ContinentalCompetitionDefinition("AFC_CLE", Continent.ASIA, "AFC Champions League Elite", 1, 24, true, 18_000_000L),
        ContinentalCompetitionDefinition("AFC_CL2", Continent.ASIA, "AFC Champions League Two", 2, 16, false, 8_000_000L),
        ContinentalCompetitionDefinition("OFC_CL", Continent.OCEANIA, "OFC Champions League", 1, 8, false, 2_500_000L),
        ContinentalCompetitionDefinition("FIFA_CWC", Continent.WORLD, "FIFA Club World Cup", 1, 32, false, 60_000_000L)
    )

    private val cityNamesByCountry = mapOf(
        "ENG" to listOf("North London", "Manchester", "Merseyside", "West London", "Birmingham", "Tyneside", "Yorkshire", "East Midlands", "South Coast", "wearside", "Bristol", "Norwich"),
        "ESP" to listOf("Madrid", "Barcelona", "Seville", "Valencia", "Bilbao", "San Sebastián", "Villarreal", "Vigo", "Zaragoza", "Málaga", "Girona", "Mallorca"),
        "GER" to listOf("Munich", "Dortmund", "Leipzig", "Leverkusen", "Frankfurt", "Stuttgart", "Hamburg", "Bremen", "Gelsenkirchen", "Cologne", "Berlin", "Freiburg"),
        "ITA" to listOf("Milan", "Turin", "Rome", "Naples", "Florence", "Bergamo", "Bologna", "Genoa", "Verona", "Palermo", "Bari", "Cagliari"),
        "FRA" to listOf("Paris", "Marseille", "Lyon", "Lille", "Monaco", "Rennes", "Nice", "Lens", "Nantes", "Bordeaux", "Strasbourg", "Toulouse"),
        "ALG" to listOf("Algiers", "Oran", "Constantine", "Sétif", "Tizi Ouzou", "Béjaïa", "Chlef", "Annaba", "Biskra", "Saïda", "Tlemcen", "Blida"),
        "MAR" to listOf("Casablanca", "Rabat", "Tangier", "Fes", "Marrakesh", "Agadir", "Berkane", "Oujda", "Meknes", "Tetouan"),
        "TUN" to listOf("Tunis", "Sfax", "Sousse", "Bizerte", "Monastir", "Kairouan", "Gabès", "Béja"),
        "EGY" to listOf("Cairo", "Alexandria", "Giza", "Ismailia", "Port Said", "Suez", "Aswan", "Mansoura"),
        "BRA" to listOf("São Paulo", "Rio de Janeiro", "Belo Horizonte", "Porto Alegre", "Curitiba", "Salvador", "Fortaleza", "Recife", "Santos", "Campinas"),
        "ARG" to listOf("Buenos Aires", "Avellaneda", "Rosario", "La Plata", "Córdoba", "Mendoza", "Santa Fe", "Tucumán", "Lanús", "Banfield"),
        "USA" to listOf("Los Angeles", "New York", "Atlanta", "Seattle", "Miami", "Columbus", "Portland", "Chicago", "Dallas", "Austin"),
        "JPN" to listOf("Tokyo", "Yokohama", "Osaka", "Kobe", "Nagoya", "Kashima", "Hiroshima", "Urawa", "Fukuoka", "Sapporo"),
        "KSA" to listOf("Riyadh", "Jeddah", "Dammam", "Mecca", "Al-Ahsa", "Buraidah", "Abha", "Tabuk")
    )

    private val clubSuffixes = listOf(
        "United", "City", "Athletic", "Rovers", "Sporting", "Olympic", "Wanderers", "Dynamo", "Real", "Union", "FC", "SC"
    )

    private val firstNamesByRegion = mapOf(
        Continent.EUROPE to listOf("Lucas", "Mateo", "Leo", "Hugo", "Jude", "Florian", "Alessandro", "Kylian", "Pedri", "Martin", "Declan", "Nico", "Gonçalo", "Viktor", "Luka", "Dusan"),
        Continent.SOUTH_AMERICA to listOf("Gabriel", "Vinícius", "Enzo", "Lautaro", "Federico", "Julian", "Thiago", "Matías", "Rodrygo", "Darwin", "Santiago", "Moises"),
        Continent.AFRICA to listOf("Riyad", "Ismaël", "Achraf", "Youssef", "Mohamed", "Victor", "Sadio", "Amine", "Baghdad", "Houssem", "Ramy", "Serhou", "Ibrahim", "Sofyan"),
        Continent.ASIA to listOf("Takefusa", "Kaoru", "Heung-Min", "Mehdi", "Salem", "Wataru", "Daichi", "Saud", "Akram", "Feras", "Jackson", "Craig"),
        Continent.NORTH_CENTRAL_AMERICA to listOf("Christian", "Alphonso", "Santiago", "Edson", "Weston", "Jonathan", "Keylor", "Hirving", "Ricardo"),
        Continent.OCEANIA to listOf("Chris", "Liberato", "Joe", "Callum", "Ben", "Kosta")
    )

    private val lastNamesByRegion = mapOf(
        Continent.EUROPE to listOf("Silva", "Martinez", "Müller", "Rossi", "Dubois", "Bellingham", "Wirtz", "Lindqvist", "Kowalski", "Novak", "Modric", "Vlahovic", "Fernandes", "De Jong"),
        Continent.SOUTH_AMERICA to listOf("Santos", "Oliveira", "Alvarez", "Valverde", "Diaz", "Caicedo", "Gimenez", "Romero", "Paredes", "Araujo", "Guimarães"),
        Continent.AFRICA to listOf("Mahrez", "Bennacer", "Hakimi", "Salah", "Osimhen", "Bensebaini", "Amoura", "Aouar", "Ziyech", "Kudus", "Marmoush", "Guirassy"),
        Continent.ASIA to listOf("Kubo", "Mitoma", "Son", "Al-Dawsari", "Endo", "Kamada", "Lee", "Afif", "Irvine", "Goodwin"),
        Continent.NORTH_CENTRAL_AMERICA to listOf("Pulisic", "Davies", "Gimenez", "Alvarez", "McKennie", "David", "Lozano", "Reyna"),
        Continent.OCEANIA to listOf("Wood", "Cacace", "Bell", "Stamenic", "Waine")
    )

    private val palettePairs = listOf(
        "#D90429" to "#FFFFFF",
        "#003566" to "#FFC300",
        "#007F5F" to "#FFFFFF",
        "#7209B7" to "#FFFFFF",
        "#F77F00" to "#000814",
        "#1D3557" to "#A8DADC",
        "#9B2226" to "#E9D8A6",
        "#10B981" to "#0A1118"
    )

    fun createNewCareerState(
        saveName: String,
        managerName: String,
        managerNationalityId: String,
        preset: DatabasePreset,
        simulationDetail: SimulationDetailLevel,
        activeDivisionIds: List<String>,
        selectedUserClubId: String?,
        basePack: WorldDatabasePack = buildDefaultWorldPack()
    ): CareerSaveState {
        val rng = Random(20261024L)
        val countriesById = basePack.countries.associateBy { it.id }

        val generatedClubs = mutableListOf<Club>()
        val generatedPlayers = mutableListOf<Player>()
        val generatedStaff = mutableListOf<StaffMember>()

        val effectiveActiveIds = if (activeDivisionIds.isEmpty()) {
            listOf("ENG_D1", "ENG_D2", "ESP_D1", "GER_D1", "ITA_D1", "FRA_D1", "ALG_D1", "BRA_D1")
        } else {
            activeDivisionIds
        }

        for (div in basePack.divisions) {
            val isActive = div.id in effectiveActiveIds
            val country = countriesById[div.countryId] ?: basePack.countries.first()
            val clubCount = if (isActive) {
                div.teamCount.coerceAtMost(preset.clubsPerActiveDiv)
            } else {
                8 // lightweight background division size for fast simulation
            }

            val cities = cityNamesByCountry[country.id]
                ?: listOf("${country.name} Capital", "${country.name} North", "${country.name} South", "${country.name} Port", "${country.name} Central", "${country.name} Metro")

            for (idx in 0 until clubCount) {
                val clubId = "CLUB_${div.id}_${idx + 1}"
                val city = cities[idx % cities.size]
                val suffix = clubSuffixes[(idx + div.tier) % clubSuffixes.size]
                val clubName = if (idx < cities.size) "$city $suffix" else "$city $suffix ${idx + 1}"
                val shortName = (city.take(3) + suffix.take(2)).uppercase()
                val baseRep = ((country.fifaRankingPoints / 22) - (div.tier - 1) * 11 - idx).coerceIn(25, 95)
                val colors = palettePairs[(idx + div.tier) % palettePairs.size]
                val pattern = BadgePattern.entries[(idx + div.tier) % BadgePattern.entries.size]

                val playersCount = if (isActive) preset.playersPerClub else 14
                val clubPlayers = generateSquadForClub(
                    clubId = clubId,
                    country = country,
                    baseSkill = baseRep,
                    count = playersCount,
                    rng = rng
                )
                generatedPlayers.addAll(clubPlayers)

                val best11 = clubPlayers.sortedByDescending { it.overallRating() }.take(11)
                val bench = clubPlayers.filter { it.id !in best11.map { p -> p.id } }.take(7)
                val formation = Formation.entries[(idx + div.tier) % Formation.entries.size]

                val stadiumCap = (baseRep * 650).coerceIn(4_500, 82_000)
                val balance = (baseRep * 480_000L).coerceAtLeast(600_000L)
                val transferBudget = (balance * 0.45).toLong()
                val wageBudget = (baseRep * 18_500L).coerceAtLeast(35_000L)

                val club = Club(
                    id = clubId,
                    name = clubName,
                    shortName = shortName,
                    countryId = country.id,
                    region = city,
                    divisionId = div.id,
                    reputation = baseRep,
                    domesticReputation = baseRep,
                    continentalReputation = (baseRep - 5).coerceAtLeast(15),
                    stadiumName = "$city Arena",
                    stadiumCapacity = stadiumCap,
                    averageAttendance = (stadiumCap * 0.82).toInt(),
                    trainingFacilitiesLevel = (baseRep / 5).coerceIn(4, 20),
                    youthFacilitiesLevel = (country.youthRating / 5).coerceIn(4, 20),
                    youthRecruitmentLevel = (country.youthRating / 5).coerceIn(4, 20),
                    primaryColorHex = colors.first,
                    secondaryColorHex = colors.second,
                    badgePattern = pattern,
                    balance = balance,
                    transferBudget = transferBudget,
                    wageBudgetWeekly = wageBudget,
                    ticketPrice = (18 + baseRep / 3),
                    seasonTicketHolders = (stadiumCap * 0.45).toInt(),
                    annualSponsorship = div.prizePoolMoney / 4,
                    annualBroadcastRevenue = div.prizePoolMoney / 2,
                    expectedLeagueFinish = (idx + 1).coerceIn(1, clubCount),
                    tactic = TacticalSetup(
                        formation = formation,
                        mentality = Mentality.entries[idx % Mentality.entries.size],
                        captainPlayerId = best11.firstOrNull()?.id,
                        penaltyTakerId = best11.lastOrNull()?.id,
                        freeKickTakerId = best11.getOrNull(5)?.id,
                        startingElevenIds = best11.map { it.id },
                        benchIds = bench.map { it.id }
                    ),
                    provenance = DataProvenance.GENERATED_SEED
                )
                generatedClubs.add(club)

                if (isActive) {
                    generatedStaff.addAll(generateStaffForClub(clubId, country, baseRep, rng))
                }
            }
        }

        val chosenClubId = selectedUserClubId?.takeIf { id -> generatedClubs.any { it.id == id } }
            ?: generatedClubs.firstOrNull { it.divisionId in effectiveActiveIds }?.id
            ?: generatedClubs.first().id

        val fixtures = CompetitionEngine.generateFullSeasonFixtures(
            seasonYear = 2026,
            divisions = basePack.divisions.filter { it.id in effectiveActiveIds },
            nationalCups = basePack.nationalCups,
            continentalCompetitions = basePack.continentalCompetitions,
            clubs = generatedClubs
        )

        val userClub = generatedClubs.first { it.id == chosenClubId }
        val welcomeNews = listOf(
            NewsItem(
                id = "NEWS_INIT_1",
                dayOfYear = 1,
                seasonYear = 2026,
                category = "BOARD",
                headline = "$managerName Appointed Manager of ${userClub.name}",
                body = "The Board of Directors at ${userClub.name} have officially unveiled $managerName at ${userClub.stadiumName}. Supporters expect a top-${userClub.expectedLeagueFinish} finish this campaign.",
                relatedClubId = userClub.id
            ),
            NewsItem(
                id = "NEWS_INIT_2",
                dayOfYear = 1,
                seasonYear = 2026,
                category = "TRANSFER",
                headline = "Global Transfer Window Opens Across ${basePack.divisions.size} Divisions",
                body = "Scouts and sporting directors are active as the 2026/27 transfer window opens with full squad registration rules in effect.",
                relatedClubId = userClub.id
            )
        )

        return CareerSaveState(
            saveId = "SAVE_${System.currentTimeMillis()}",
            saveName = saveName,
            createdTimestamp = System.currentTimeMillis(),
            managerName = managerName,
            managerNationalityId = managerNationalityId,
            userClubId = chosenClubId,
            seasonYear = 2026,
            dayOfYear = 1,
            databasePreset = preset,
            simulationDetail = simulationDetail,
            activeDivisionIds = effectiveActiveIds,
            countries = basePack.countries,
            divisions = basePack.divisions,
            nationalCups = basePack.nationalCups,
            continentalCompetitions = basePack.continentalCompetitions,
            clubs = generatedClubs,
            players = generatedPlayers,
            staff = generatedStaff,
            fixtures = fixtures,
            news = welcomeNews
        )
    }

    fun generateSquadForClub(
        clubId: String,
        country: Country,
        baseSkill: Int,
        count: Int,
        rng: Random
    ): List<Player> {
        val positionTemplate = listOf(
            Position.GK, Position.GK,
            Position.LB, Position.CB, Position.CB, Position.CB, Position.RB,
            Position.DM, Position.CM, Position.CM, Position.CM, Position.AM,
            Position.LW, Position.RW, Position.ST, Position.ST,
            Position.CB, Position.LB, Position.DM, Position.AM, Position.LW, Position.ST,
            Position.GK, Position.RB, Position.CM
        )

        val fNames = firstNamesByRegion[country.continent] ?: firstNamesByRegion[Continent.EUROPE]!!
        val lNames = lastNamesByRegion[country.continent] ?: lastNamesByRegion[Continent.EUROPE]!!

        return (0 until count).map { idx ->
            val pos = positionTemplate[idx % positionTemplate.size]
            val age = rng.nextInt(17, 34)
            val skill = (baseSkill + rng.nextInt(-7, 8)).coerceIn(38, 92)
            val potential = (skill + (28 - age).coerceAtLeast(1) + rng.nextInt(0, 10)).coerceIn(skill, 97)
            val fullName = "${fNames[rng.nextInt(fNames.size)]} ${lNames[rng.nextInt(lNames.size)]}"

            val isGk = pos == Position.GK
            val isDef = pos in listOf(Position.CB, Position.LB, Position.RB)
            val isMid = pos in listOf(Position.DM, Position.CM, Position.AM)
            val isAtt = pos in listOf(Position.LW, Position.RW, Position.ST)

            val tech = TechnicalAttributes(
                finishing = if (isAtt) (skill + rng.nextInt(-3, 7)).coerceIn(25, 98) else (skill - 15).coerceIn(15, 85),
                passing = if (isMid) (skill + rng.nextInt(0, 8)).coerceIn(30, 98) else skill.coerceIn(25, 90),
                crossing = if (pos in listOf(Position.LB, Position.RB, Position.LW, Position.RW)) (skill + 4).coerceIn(25, 95) else (skill - 8).coerceIn(20, 85),
                dribbling = if (isAtt || pos == Position.AM) (skill + 5).coerceIn(30, 97) else (skill - 6).coerceIn(20, 88),
                firstTouch = (skill + rng.nextInt(-4, 6)).coerceIn(25, 96),
                tackling = if (isDef || pos == Position.DM) (skill + 6).coerceIn(30, 97) else (skill - 18).coerceIn(15, 80),
                marking = if (isDef || pos == Position.DM) (skill + 5).coerceIn(30, 97) else (skill - 18).coerceIn(15, 80),
                heading = if (pos in listOf(Position.CB, Position.ST)) (skill + 4).coerceIn(30, 96) else (skill - 8).coerceIn(20, 85),
                longShots = (skill + rng.nextInt(-8, 6)).coerceIn(20, 95),
                technique = (skill + rng.nextInt(-4, 6)).coerceIn(25, 97),
                setPieces = (skill + rng.nextInt(-10, 10)).coerceIn(20, 95),
                penalties = (skill + rng.nextInt(-8, 10)).coerceIn(25, 96)
            )

            val mental = MentalAttributes(
                decisions = (skill + rng.nextInt(-5, 6)).coerceIn(30, 96),
                vision = if (isMid || isAtt) (skill + rng.nextInt(-3, 7)).coerceIn(30, 97) else (skill - 8).coerceIn(25, 86),
                anticipation = (skill + rng.nextInt(-4, 6)).coerceIn(30, 96),
                composure = (skill + rng.nextInt(-5, 6)).coerceIn(30, 96),
                concentration = (skill + rng.nextInt(-5, 6)).coerceIn(30, 96),
                positioning = if (isDef || isGk) (skill + 5).coerceIn(30, 97) else skill.coerceIn(25, 90),
                teamwork = (skill + rng.nextInt(-6, 8)).coerceIn(35, 96),
                workRate = (skill + rng.nextInt(-6, 8)).coerceIn(35, 96),
                leadership = (40 + (age - 17) * 2 + rng.nextInt(-8, 10)).coerceIn(20, 96),
                aggression = rng.nextInt(40, 86),
                determination = rng.nextInt(50, 95),
                offTheBall = if (isAtt || isMid) (skill + 4).coerceIn(30, 97) else (skill - 8).coerceIn(25, 85)
            )

            val physical = PhysicalAttributes(
                pace = (skill + (28 - age) / 2 + rng.nextInt(-5, 7)).coerceIn(32, 98),
                acceleration = (skill + (28 - age) / 2 + rng.nextInt(-5, 7)).coerceIn(32, 98),
                stamina = (skill + rng.nextInt(-5, 8)).coerceIn(40, 97),
                strength = (skill + rng.nextInt(-6, 8)).coerceIn(35, 96),
                balance = (skill + rng.nextInt(-5, 6)).coerceIn(35, 95),
                jumping = (skill + rng.nextInt(-6, 8)).coerceIn(35, 95),
                agility = (skill + rng.nextInt(-5, 7)).coerceIn(35, 96),
                naturalFitness = rng.nextInt(58, 96)
            )

            val gk = if (isGk) {
                GoalkeepingAttributes(
                    reflexes = (skill + rng.nextInt(-3, 7)).coerceIn(40, 97),
                    handling = (skill + rng.nextInt(-3, 6)).coerceIn(40, 96),
                    oneOnOnes = (skill + rng.nextInt(-4, 6)).coerceIn(38, 95),
                    aerialAbility = (skill + rng.nextInt(-4, 6)).coerceIn(38, 95),
                    communication = (skill + rng.nextInt(-4, 6)).coerceIn(38, 95),
                    commandOfArea = (skill + rng.nextInt(-4, 6)).coerceIn(38, 95),
                    distribution = (skill + rng.nextInt(-5, 6)).coerceIn(35, 94),
                    gkPositioning = (skill + rng.nextInt(-3, 7)).coerceIn(40, 97)
                )
            } else {
                GoalkeepingAttributes()
            }

            val marketVal = ((skill * skill * 8_500L) * (if (age < 24) 1.35 else 1.0)).toLong().coerceAtLeast(75_000L)
            val weeklyWage = (marketVal / 260L).coerceAtLeast(1_200L)

            Player(
                id = "PLY_${clubId}_${idx + 1}",
                fullName = fullName,
                birthYear = 2026 - age,
                age = age,
                nationalityId = country.id,
                preferredFoot = if (rng.nextInt(100) < 72) PreferredFoot.RIGHT else PreferredFoot.LEFT,
                mainPosition = pos,
                secondaryPositions = if (isMid) listOf(Position.CM, Position.DM) else emptyList(),
                clubId = clubId,
                squadType = if (age <= 19 && idx >= 18) SquadTeamType.YOUTH_U19 else SquadTeamType.MAIN_SQUAD,
                squadStatus = when {
                    idx < 4 -> SquadStatus.STAR_PLAYER
                    idx < 11 -> SquadStatus.IMPORTANT_STARTER
                    idx < 18 -> SquadStatus.SQUAD_ROTATION
                    else -> SquadStatus.YOUTH_PROSPECT
                },
                shirtNumber = idx + 1,
                contractStartYear = 2025,
                contractExpiryYear = 2026 + rng.nextInt(1, 5),
                wageWeekly = weeklyWage,
                marketValue = marketVal,
                releaseClause = (marketVal * 1.65).toLong(),
                reputation = skill,
                technical = tech,
                mental = mental,
                physical = physical,
                goalkeeping = gk,
                hiddenPotentialAbility = potential,
                scoutedKnowledgePercent = if (idx < 11) 70 else 40
            )
        }
    }

    private fun generateStaffForClub(
        clubId: String,
        country: Country,
        baseSkill: Int,
        rng: Random
    ): List<StaffMember> {
        val fNames = firstNamesByRegion[country.continent] ?: firstNamesByRegion[Continent.EUROPE]!!
        val lNames = lastNamesByRegion[country.continent] ?: lastNamesByRegion[Continent.EUROPE]!!
        return StaffRole.entries.mapIndexed { idx, role ->
            val skill = (baseSkill + rng.nextInt(-6, 7)).coerceIn(40, 94)
            StaffMember(
                id = "STAFF_${clubId}_${idx + 1}",
                fullName = "${fNames[rng.nextInt(fNames.size)]} ${lNames[rng.nextInt(lNames.size)]}",
                nationalityId = country.id,
                clubId = clubId,
                role = role,
                coachingSkill = skill,
                tacticalKnowledge = skill,
                judgingPlayerAbility = skill,
                judgingPlayerPotential = (skill + 3).coerceAtMost(98),
                physiotherapySkill = skill,
                youthWorking = skill,
                wageWeekly = (skill * 85L).coerceAtLeast(1_500L)
            )
        }
    }
}

object SaveRepository {
    private const val SAVE_DIR = "wfd_saves"
    private const val CUSTOM_PACK_FILE = "custom_world_pack.json"

    fun saveCareerAtomically(context: Context, state: CareerSaveState): Boolean {
        return try {
            val dir = File(context.filesDir, SAVE_DIR).apply { mkdirs() }
            val tempFile = File(dir, "${state.saveId}.tmp")
            val targetFile = File(dir, "${state.saveId}.json")
            val backupFile = File(dir, "${state.saveId}.bak")

            val serialized = WorldDatabaseSeeder.jsonFormat.encodeToString(state)
            tempFile.writeText(serialized)

            if (targetFile.exists()) {
                targetFile.copyTo(backupFile, overwrite = true)
            }
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun loadAllSaves(context: Context): List<CareerSaveState> {
        val dir = File(context.filesDir, SAVE_DIR)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    WorldDatabaseSeeder.jsonFormat.decodeFromString<CareerSaveState>(file.readText())
                } catch (e: Exception) {
                    val backup = File(dir, "${file.nameWithoutExtension}.bak")
                    if (backup.exists()) {
                        runCatching {
                            WorldDatabaseSeeder.jsonFormat.decodeFromString<CareerSaveState>(backup.readText())
                        }.getOrNull()
                    } else null
                }
            }
            ?.sortedByDescending { it.createdTimestamp }
            ?: emptyList()
    }

    fun deleteSave(context: Context, saveId: String) {
        val dir = File(context.filesDir, SAVE_DIR)
        File(dir, "$saveId.json").delete()
        File(dir, "$saveId.bak").delete()
    }

    fun saveCustomWorldPack(context: Context, pack: WorldDatabasePack) {
        val file = File(context.filesDir, CUSTOM_PACK_FILE)
        file.writeText(WorldDatabaseSeeder.jsonFormat.encodeToString(pack))
    }

    fun loadCustomWorldPackOrDefault(context: Context): WorldDatabasePack {
        val file = File(context.filesDir, CUSTOM_PACK_FILE)
        if (!file.exists()) return WorldDatabaseSeeder.buildDefaultWorldPack()
        return runCatching {
            WorldDatabaseSeeder.jsonFormat.decodeFromString<WorldDatabasePack>(file.readText())
        }.getOrElse { WorldDatabaseSeeder.buildDefaultWorldPack() }
    }
}
