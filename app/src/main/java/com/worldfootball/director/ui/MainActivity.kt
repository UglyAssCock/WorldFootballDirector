package com.worldfootball.director.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldfootball.director.data.SaveRepository
import com.worldfootball.director.data.WorldDatabaseSeeder
import com.worldfootball.director.engine.*
import com.worldfootball.director.model.*
import kotlinx.serialization.encodeToString

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorldFootballDirectorTheme {
                WorldDirectorRootApp()
            }
        }
    }
}

private val NavyBackground = Color(0xFF0A1118)
private val NavySurface = Color(0xFF121E2B)
private val NavyCard = Color(0xFF192838)
private val EmeraldAccent = Color(0xFF10B981)
private val AmberAccent = Color(0xFFF59E0B)
private val CrimsonDanger = Color(0xFFEF4444)
private val SlateText = Color(0xFF94A3B8)

@Composable
fun WorldFootballDirectorTheme(content: @Composable () -> Unit) {
    val darkPalette = darkColorScheme(
        primary = EmeraldAccent,
        secondary = AmberAccent,
        background = NavyBackground,
        surface = NavySurface,
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )
    MaterialTheme(colorScheme = darkPalette, content = content)
}

fun parseHexColor(hex: String, fallback: Color = EmeraldAccent): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}

/**
 * Pure Flat 2D Vector Club Crest — Zero shadows, zero 3D gradients or bevels.
 */
@Composable
fun ClubBadge2D(
    club: Club,
    size: Dp = 44.dp
) {
    val primary = parseHexColor(club.primaryColorHex, EmeraldAccent)
    val secondary = parseHexColor(club.secondaryColorHex, Color.White)

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val shieldPath = Path().apply {
                moveTo(w * 0.10f, h * 0.08f)
                lineTo(w * 0.90f, h * 0.08f)
                lineTo(w * 0.90f, h * 0.58f)
                lineTo(w * 0.50f, h * 0.94f)
                lineTo(w * 0.10f, h * 0.58f)
                close()
            }

            drawPath(path = shieldPath, color = primary)

            when (club.badgePattern) {
                BadgePattern.VERTICAL_STRIPES -> {
                    drawRect(
                        color = secondary,
                        topLeft = Offset(w * 0.36f, h * 0.10f),
                        size = Size(w * 0.12f, h * 0.68f)
                    )
                    drawRect(
                        color = secondary,
                        topLeft = Offset(w * 0.54f, h * 0.10f),
                        size = Size(w * 0.12f, h * 0.68f)
                    )
                }
                BadgePattern.HORIZONTAL_HOOPS -> {
                    drawRect(
                        color = secondary,
                        topLeft = Offset(w * 0.14f, h * 0.30f),
                        size = Size(w * 0.72f, h * 0.14f)
                    )
                    drawRect(
                        color = secondary,
                        topLeft = Offset(w * 0.14f, h * 0.52f),
                        size = Size(w * 0.72f, h * 0.14f)
                    )
                }
                BadgePattern.HALF_AND_HALF -> {
                    drawRect(
                        color = secondary,
                        topLeft = Offset(w * 0.50f, h * 0.10f),
                        size = Size(w * 0.38f, h * 0.55f)
                    )
                }
                BadgePattern.DIAGONAL_SASH, BadgePattern.CHEVRON, BadgePattern.SOLID_CREST -> {
                    drawLine(
                        color = secondary,
                        start = Offset(w * 0.22f, h * 0.20f),
                        end = Offset(w * 0.78f, h * 0.68f),
                        strokeWidth = w * 0.12f
                    )
                }
            }

            drawPath(
                path = shieldPath,
                color = secondary,
                style = Stroke(width = w * 0.06f)
            )
        }
        Text(
            text = club.shortName.take(3),
            color = Color.White,
            fontSize = (size.value * 0.24f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

enum class MainTab(val title: String) {
    HOME("Home"),
    SQUAD("Squad"),
    TACTICS("Tactics"),
    COMPETITIONS("Leagues"),
    HUB("Director Hub")
}

enum class HubSection(val label: String) {
    TRANSFERS_SCOUTING("Transfers & Scouting"),
    FINANCES_INFRA("Finances & Stadium"),
    YOUTH_TRAINING("Youth & Staff"),
    WORLD_RECORDS("World & Records"),
    IN_GAME_EDITOR("Database Editor & JSON")
}

@Composable
fun WorldDirectorRootApp() {
    val context = LocalContext.current
    var worldPack by remember { mutableStateOf(SaveRepository.loadCustomWorldPackOrDefault(context)) }
    var savedCareers by remember { mutableStateOf(SaveRepository.loadAllSaves(context)) }
    var activeCareer by remember { mutableStateOf<CareerSaveState?>(savedCareers.firstOrNull()) }
    var inMainMenu by remember { mutableStateOf(activeCareer == null) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = NavyBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            statusBanner?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldAccent.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(msg, color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { statusBanner = null }) {
                        Text("Dismiss", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            if (inMainMenu || activeCareer == null) {
                MainMenuScreen(
                    worldPack = worldPack,
                    savedCareers = savedCareers,
                    onContinueCareer = { career ->
                        activeCareer = career
                        inMainMenu = false
                    },
                    onCreateNewCareer = { newCareer ->
                        SaveRepository.saveCareerAtomically(context, newCareer)
                        savedCareers = SaveRepository.loadAllSaves(context)
                        activeCareer = newCareer
                        inMainMenu = false
                        statusBanner = "Career created and saved (${newCareer.divisions.size} divisions loaded)."
                    },
                    onDeleteSave = { saveId ->
                        SaveRepository.deleteSave(context, saveId)
                        savedCareers = SaveRepository.loadAllSaves(context)
                        if (activeCareer?.saveId == saveId) activeCareer = null
                    },
                    onUpdateWorldPack = { updatedPack ->
                        worldPack = updatedPack
                        SaveRepository.saveCustomWorldPack(context, updatedPack)
                        statusBanner = "Pre-game World Database saved."
                    }
                )
            } else {
                CareerWorkspaceScreen(
                    career = activeCareer!!,
                    onUpdateCareer = { updated ->
                        activeCareer = updated
                        SaveRepository.saveCareerAtomically(context, updated)
                    },
                    onExitToMainMenu = {
                        savedCareers = SaveRepository.loadAllSaves(context)
                        inMainMenu = true
                    },
                    onBanner = { statusBanner = it }
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    worldPack: WorldDatabasePack,
    savedCareers: List<CareerSaveState>,
    onContinueCareer: (CareerSaveState) -> Unit,
    onCreateNewCareer: (CareerSaveState) -> Unit,
    onDeleteSave: (String) -> Unit,
    onUpdateWorldPack: (WorldDatabasePack) -> Unit
) {
    var menuView by remember { mutableStateOf("MAIN") } // MAIN, NEW_CAREER, SAVES, PRE_EDITOR
    var managerName by remember { mutableStateOf("Alex Touzani") }
    var selectedCountryId by remember { mutableStateOf("ALG") }
    var selectedPreset by remember { mutableStateOf(DatabasePreset.STANDARD) }
    var selectedSimDetail by remember { mutableStateOf(SimulationDetailLevel.BALANCED) }
    var selectedContinentFilter by remember { mutableStateOf<Continent?>(null) }
    val selectedDivisions = remember {
        mutableStateListOf("ENG_D1", "ENG_D2", "ESP_D1", "GER_D1", "ITA_D1", "FRA_D1", "ALG_D1", "ALG_D2", "MAR_D1", "BRA_D1")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "WORLD FOOTBALL DIRECTOR",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldAccent
        )
        Text(
            text = "${worldPack.divisions.size} Real Domestic Divisions • ${worldPack.countries.size} Nations • Offline Engine",
            fontSize = 12.sp,
            color = SlateText
        )
        Spacer(modifier = Modifier.height(14.dp))

        when (menuView) {
            "MAIN" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (savedCareers.isNotEmpty()) {
                        val latest = savedCareers.first()
                        val userClub = latest.clubs.firstOrNull { it.id == latest.userClubId }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavyCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("ACTIVE CAREER SAVE", color = EmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${latest.saveName} — ${userClub?.name ?: latest.userClubId}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Season ${latest.seasonYear}, Day ${latest.dayOfYear} • Manager: ${latest.managerName}",
                                    fontSize = 12.sp,
                                    color = SlateText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onContinueCareer(latest) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                                ) {
                                    Text("CONTINUE CAREER", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { menuView = "NEW_CAREER" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                    ) {
                        Text("START NEW CAREER (108 DIVISIONS)", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    OutlinedButton(
                        onClick = { menuView = "SAVES" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LOAD / MANAGE SAVES (${savedCareers.size})")
                    }

                    OutlinedButton(
                        onClick = { menuView = "PRE_EDITOR" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PRE-GAME WORLD EDITOR & JSON PACKS")
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Verified Architecture Summary", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "• ${worldPack.divisions.size} real domestic divisions across Europe, South America, North America, Africa, Asia & Oceania.\n" +
                                    "• ${worldPack.nationalCups.size} National Cups & ${worldPack.continentalCompetitions.size} Continental Club Competitions.\n" +
                                    "• Deterministic match engine, full promotion/relegation, youth intake & in-game editor.",
                                fontSize = 12.sp,
                                color = SlateText
                            )
                        }
                    }
                }
            }

            "NEW_CAREER" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("New Career Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { menuView = "MAIN" }) { Text("Back") }
                    }

                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("Manager Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DatabasePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset },
                                label = { Text(preset.name, fontSize = 11.sp) }
                            )
                        }
                        SimulationDetailLevel.entries.forEach { detail ->
                            FilterChip(
                                selected = selectedSimDetail == detail,
                                onClick = { selectedSimDetail = detail },
                                label = { Text(detail.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select Playable Divisions (${selectedDivisions.size} / ${worldPack.divisions.size} active):",
                        fontSize = 12.sp,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedContinentFilter == null,
                            onClick = { selectedContinentFilter = null },
                            label = { Text("All (${worldPack.divisions.size})", fontSize = 11.sp) }
                        )
                        Continent.entries.filter { it != Continent.WORLD }.forEach { cont ->
                            FilterChip(
                                selected = selectedContinentFilter == cont,
                                onClick = { selectedContinentFilter = cont },
                                label = { Text(cont.name, fontSize = 11.sp) }
                            )
                        }
                    }

                    val countryById = remember(worldPack) { worldPack.countries.associateBy { it.id } }
                    val filteredDivisions = worldPack.divisions.filter { div ->
                        selectedContinentFilter == null || countryById[div.countryId]?.continent == selectedContinentFilter
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredDivisions, key = { it.id }) { div ->
                            val isChecked = div.id in selectedDivisions
                            val countryName = countryById[div.countryId]?.name ?: div.countryId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NavyCard)
                                    .clickable {
                                        if (isChecked && selectedDivisions.size > 1) {
                                            selectedDivisions.remove(div.id)
                                        } else if (!isChecked) {
                                            selectedDivisions.add(div.id)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${div.name} (Tier ${div.tier})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("$countryName • ${div.teamCount} Clubs • Promo: ${div.promotionSpots} / Releg: ${div.relegationSpots}", fontSize = 11.sp, color = SlateText)
                                }
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (isChecked && selectedDivisions.size > 1) {
                                            selectedDivisions.remove(div.id)
                                        } else if (!isChecked) {
                                            selectedDivisions.add(div.id)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val newState = WorldDatabaseSeeder.createNewCareerState(
                                saveName = "${managerName.take(12)} Career",
                                managerName = managerName.ifBlank { "Manager" },
                                managerNationalityId = selectedCountryId,
                                preset = selectedPreset,
                                simulationDetail = selectedSimDetail,
                                activeDivisionIds = selectedDivisions.toList(),
                                selectedUserClubId = null,
                                basePack = worldPack
                            )
                            onCreateNewCareer(newState)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                    ) {
                        Text("INITIALIZE WORLD & LAUNCH CAREER", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            "SAVES" -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved Careers", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { menuView = "MAIN" }) { Text("Back") }
                    }
                    if (savedCareers.isEmpty()) {
                        Text("No saved careers yet.", color = SlateText)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(savedCareers, key = { it.saveId }) { save ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(save.saveName, fontWeight = FontWeight.Bold)
                                            Text("Season ${save.seasonYear} • Day ${save.dayOfYear} • ${save.managerName}", fontSize = 12.sp, color = SlateText)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(onClick = { onContinueCareer(save) }) { Text("Load") }
                                            OutlinedButton(onClick = { onDeleteSave(save.saveId) }) { Text("Delete") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "PRE_EDITOR" -> {
                PreGameWorldEditorScreen(
                    worldPack = worldPack,
                    onSavePack = onUpdateWorldPack,
                    onBack = { menuView = "MAIN" }
                )
            }
        }
    }
}

@Composable
fun PreGameWorldEditorScreen(
    worldPack: WorldDatabasePack,
    onSavePack: (WorldDatabasePack) -> Unit,
    onBack: () -> Unit
) {
    var newDivId by remember { mutableStateOf("CUSTOM_D1") }
    var newDivCountry by remember { mutableStateOf("ALG") }
    var newDivName by remember { mutableStateOf("Algerian Super Division") }
    var newDivTier by remember { mutableStateOf("1") }
    var jsonExportPreview by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pre-Game Database & Division Editor", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("Back") }
        }

        Card(colors = CardDefaults.cardColors(containerColor = NavyCard)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add Custom Domestic Division", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = newDivId, onValueChange = { newDivId = it }, label = { Text("Unique Division ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newDivCountry, onValueChange = { newDivCountry = it }, label = { Text("Country Code (e.g. ALG, ENG, ESP)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newDivName, onValueChange = { newDivName = it }, label = { Text("Division Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newDivTier, onValueChange = { newDivTier = it }, label = { Text("Pyramid Tier (1, 2, 3...)") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        if (newDivId.isNotBlank() && worldPack.divisions.none { it.id == newDivId }) {
                            val created = DivisionDefinition(
                                id = newDivId.trim().uppercase(),
                                countryId = newDivCountry.trim().uppercase(),
                                name = newDivName.trim(),
                                tier = newDivTier.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                                teamCount = 16,
                                provenance = DataProvenance.USER_CUSTOM
                            )
                            onSavePack(worldPack.copy(divisions = worldPack.divisions + created))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("ADD DIVISION TO WORLD PACK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    jsonExportPreview = WorldDatabaseSeeder.jsonFormat.encodeToString(
                        worldPack.copy(divisions = worldPack.divisions.take(5))
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Export JSON Sample")
            }
            OutlinedButton(
                onClick = { onSavePack(WorldDatabaseSeeder.buildDefaultWorldPack()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset to Default 108")
            }
        }

        if (jsonExportPreview.isNotBlank()) {
            OutlinedTextField(
                value = jsonExportPreview,
                onValueChange = { jsonExportPreview = it },
                label = { Text("JSON Database Pack (Editable for Import)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            Button(
                onClick = {
                    runCatching {
                        val parsed = WorldDatabaseSeeder.jsonFormat.decodeFromString<WorldDatabasePack>(jsonExportPreview)
                        if (parsed.divisions.isNotEmpty()) onSavePack(parsed)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Validate & Import JSON Pack")
            }
        }
    }
}

@Composable
fun CareerWorkspaceScreen(
    career: CareerSaveState,
    onUpdateCareer: (CareerSaveState) -> Unit,
    onExitToMainMenu: () -> Unit,
    onBanner: (String) -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedPlayerForModal by remember { mutableStateOf<Player?>(null) }

    val userClub = career.clubs.firstOrNull { it.id == career.userClubId } ?: career.clubs.first()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClubBadge2D(club = userClub, size = 36.dp)
                    Column {
                        Text(userClub.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Season ${career.seasonYear} • Day ${career.dayOfYear} • €${userClub.balance / 1_000_000}M",
                            fontSize = 11.sp,
                            color = EmeraldAccent
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = {
                            val next = CalendarEngine.advanceSingleDay(career)
                            onUpdateCareer(next)
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                    ) {
                        Text("+1 Day", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            val next = CalendarEngine.advanceDays(career, 7)
                            onUpdateCareer(next)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("+7d", fontSize = 12.sp)
                    }
                    IconButton(onClick = onExitToMainMenu) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Menu", tint = SlateText)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = NavySurface) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            val icon = when (tab) {
                                MainTab.HOME -> Icons.Default.Home
                                MainTab.SQUAD -> Icons.Default.Groups
                                MainTab.TACTICS -> Icons.Default.SportsSoccer
                                MainTab.COMPETITIONS -> Icons.Default.EmojiEvents
                                MainTab.HUB -> Icons.Default.Dashboard
                            }
                            Icon(icon, contentDescription = tab.title)
                        },
                        label = { Text(tab.title, fontSize = 10.sp) }
                    )
                }
            }
        },
        containerColor = NavyBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeDashboardTab(
                    career = career,
                    userClub = userClub,
                    onUpdateCareer = onUpdateCareer,
                    onBanner = onBanner
                )
                MainTab.SQUAD -> SquadManagementTab(
                    career = career,
                    userClub = userClub,
                    onSelectPlayer = { selectedPlayerForModal = it }
                )
                MainTab.TACTICS -> TacticsPitchTab(
                    career = career,
                    userClub = userClub,
                    onUpdateCareer = onUpdateCareer,
                    onBanner = onBanner
                )
                MainTab.COMPETITIONS -> CompetitionsStandingsTab(
                    career = career,
                    userClub = userClub
                )
                MainTab.HUB -> DirectorHubTab(
                    career = career,
                    userClub = userClub,
                    onUpdateCareer = onUpdateCareer,
                    onSelectPlayer = { selectedPlayerForModal = it },
                    onBanner = onBanner
                )
            }

            selectedPlayerForModal?.let { player ->
                PlayerProfileModal(
                    player = player,
                    career = career,
                    userClub = userClub,
                    onDismiss = { selectedPlayerForModal = null },
                    onUpdateCareer = { updated ->
                        onUpdateCareer(updated)
                        selectedPlayerForModal = updated.players.firstOrNull { it.id == player.id }
                    },
                    onBanner = onBanner
                )
            }
        }
    }
}

@Composable
fun HomeDashboardTab(
    career: CareerSaveState,
    userClub: Club,
    onUpdateCareer: (CareerSaveState) -> Unit,
    onBanner: (String) -> Unit
) {
    val userDivision = career.divisions.firstOrNull { it.id == userClub.divisionId } ?: career.divisions.first()
    val standings = remember(career.fixtures, career.clubs) {
        CompetitionEngine.calculateStandings(userDivision, career.clubs, career.fixtures)
    }
    val rank = standings.indexOfFirst { it.clubId == userClub.id } + 1
    val nextFixture = career.fixtures.firstOrNull {
        !it.played && (it.homeClubId == userClub.id || it.awayClubId == userClub.id)
    }
    val lastFixture = career.fixtures.lastOrNull {
        it.played && (it.homeClubId == userClub.id || it.awayClubId == userClub.id)
    }
    val clubMap = remember(career.clubs) { career.clubs.associateBy { it.id } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = NavyCard)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(userDivision.name, color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("League Position: #${if (rank > 0) rank else "-"} of ${standings.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Stadium: ${userClub.stadiumName} (${userClub.stadiumCapacity} cap)", fontSize = 12.sp, color = SlateText)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Board: ${userClub.boardConfidence}%", color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Fans: ${userClub.supporterConfidence}%", color = AmberAccent, fontSize = 12.sp)
                            Text("Transfer Budget: €${userClub.transferBudget / 1_000}K", fontSize = 11.sp, color = SlateText)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (nextFixture != null) {
                                    val days = (nextFixture.dayOfYear - career.dayOfYear).coerceAtLeast(1)
                                    onUpdateCareer(CalendarEngine.advanceDays(career, days))
                                } else {
                                    onUpdateCareer(CalendarEngine.advanceSingleDay(career))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Text("SIMULATE TO NEXT MATCH", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val updatedClubs = career.clubs.map {
                                    if (it.id == userClub.id) it.copy(
                                        boardConfidence = (it.boardConfidence + 3).coerceAtMost(100),
                                        supporterConfidence = (it.supporterConfidence + 4).coerceAtMost(100)
                                    ) else it
                                }
                                val pressNews = NewsItem(
                                    id = "PRESS_${System.currentTimeMillis()}",
                                    dayOfYear = career.dayOfYear,
                                    seasonYear = career.seasonYear,
                                    category = "PRESS",
                                    headline = "${career.managerName} Backs Squad in Pre-Match Press Conference",
                                    body = "Speaking to reporters at ${userClub.stadiumName}, ${career.managerName} praised squad morale and tactical discipline.",
                                    relatedClubId = userClub.id
                                )
                                onUpdateCareer(career.copy(clubs = updatedClubs, news = listOf(pressNews) + career.news))
                                onBanner("Press conference held: Morale & Board confidence boosted!")
                            }
                        ) {
                            Text("Press Conf", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (nextFixture != null) {
            val home = clubMap[nextFixture.homeClubId]
            val away = clubMap[nextFixture.awayClubId]
            item {
                Card(colors = CardDefaults.cardColors(containerColor = NavyCard)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("NEXT FIXTURE • DAY ${nextFixture.dayOfYear} (${nextFixture.roundName})", color = AmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (home != null) ClubBadge2D(home, 32.dp)
                                Text(home?.name ?: nextFixture.homeClubId, fontWeight = FontWeight.Bold)
                            }
                            Text("VS", color = EmeraldAccent, fontWeight = FontWeight.ExtraBold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(away?.name ?: nextFixture.awayClubId, fontWeight = FontWeight.Bold)
                                if (away != null) ClubBadge2D(away, 32.dp)
                            }
                        }
                    }
                }
            }
        }

        if (lastFixture != null) {
            val home = clubMap[lastFixture.homeClubId]
            val away = clubMap[lastFixture.awayClubId]
            item {
                Card(colors = CardDefaults.cardColors(containerColor = NavySurface)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("LATEST MATCH REPORT & COMMENTARY", color = EmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${home?.name ?: lastFixture.homeClubId} ${lastFixture.homeGoals} - ${lastFixture.awayGoals} ${away?.name ?: lastFixture.awayClubId}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Possession: ${lastFixture.homePossession}% / ${100 - lastFixture.homePossession}% • Shots (On Target): ${lastFixture.homeShots}(${lastFixture.homeShotsOnTarget}) - ${lastFixture.awayShots}(${lastFixture.awayShotsOnTarget})",
                            fontSize = 11.sp,
                            color = SlateText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        lastFixture.events.takeLast(6).forEach { ev ->
                            Text("• ${ev.commentary}", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Text("INBOX & WORLD FOOTBALL NEWS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldAccent)
        }

        items(career.news.take(15), key = { it.id }) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("[${item.category}] Day ${item.dayOfYear}, ${item.seasonYear}", fontSize = 10.sp, color = AmberAccent)
                    Text(item.headline, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(item.body, fontSize = 12.sp, color = SlateText)
                }
            }
        }
    }
}

@Composable
fun SquadManagementTab(
    career: CareerSaveState,
    userClub: Club,
    onSelectPlayer: (Player) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("OVR") } // OVR, AGE, GOALS, VALUE

    val squad = remember(career.players, userClub.id, searchQuery, sortBy) {
        val raw = career.players.filter {
            it.clubId == userClub.id && (searchQuery.isBlank() || it.fullName.contains(searchQuery, ignoreCase = true))
        }
        when (sortBy) {
            "AGE" -> raw.sortedBy { it.age }
            "GOALS" -> raw.sortedByDescending { it.seasonGoals }
            "VALUE" -> raw.sortedByDescending { it.marketValue }
            else -> raw.sortedByDescending { it.overallRating() }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Squad (${squad.size} Players)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("OVR", "AGE", "GOALS", "VALUE").forEach { key ->
                FilterChip(
                    selected = sortBy == key,
                    onClick = { sortBy = key },
                    label = { Text("Sort: $key", fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(squad, key = { it.id }) { player ->
                val isStarter = player.id in userClub.tactic.startingElevenIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isStarter) NavyCard else NavySurface)
                        .clickable { onSelectPlayer(player) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = player.mainPosition.code,
                                color = EmeraldAccent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                            Text(player.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (isStarter) {
                                Text("XI", color = AmberAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Age ${player.age} • ${player.nationalityId} • Fit ${player.matchFitness}% • Goals ${player.seasonGoals} • €${player.marketValue / 1_000}K",
                            fontSize = 11.sp,
                            color = SlateText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${player.overallRating()}", color = EmeraldAccent, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun TacticsPitchTab(
    career: CareerSaveState,
    userClub: Club,
    onUpdateCareer: (CareerSaveState) -> Unit,
    onBanner: (String) -> Unit
) {
    val tactic = userClub.tactic
    val clubPlayers = career.players.filter { it.clubId == userClub.id }
    val playerById = clubPlayers.associateBy { it.id }
    val starters = tactic.startingElevenIds.mapNotNull { playerById[it] }.ifEmpty {
        clubPlayers.sortedByDescending { it.overallRating() }.take(11)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Tactical Board & Pitch Formation", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Formation.entries.forEach { form ->
                FilterChip(
                    selected = tactic.formation == form,
                    onClick = {
                        val updatedClubs = career.clubs.map {
                            if (it.id == userClub.id) it.copy(tactic = tactic.copy(formation = form)) else it
                        }
                        onUpdateCareer(career.copy(clubs = updatedClubs))
                    },
                    label = { Text(form.label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Mentality.entries.forEach { ment ->
                FilterChip(
                    selected = tactic.mentality == ment,
                    onClick = {
                        val updatedClubs = career.clubs.map {
                            if (it.id == userClub.id) it.copy(tactic = tactic.copy(mentality = ment)) else it
                        }
                        onUpdateCareer(career.copy(clubs = updatedClubs))
                    },
                    label = { Text(ment.label, fontSize = 11.sp) }
                )
            }
        }

        // 2D Tactical Pitch Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0E4D2B))
                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Halfway line & center circle
                drawLine(Color.White.copy(alpha = 0.4f), Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 2f)
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 42f, center = Offset(w / 2f, h / 2f), style = Stroke(2f))

                val coords = tactic.formation.coordinates
                coords.forEachIndexed { idx, (nx, ny) ->
                    val cx = nx * w
                    val cy = ny * h
                    drawCircle(color = EmeraldAccent, radius = 22f, center = Offset(cx, cy))
                    drawCircle(color = Color.White, radius = 22f, center = Offset(cx, cy), style = Stroke(3f))
                }
            }

            tactic.formation.coordinates.forEachIndexed { idx, (nx, ny) ->
                val p = starters.getOrNull(idx)
                if (p != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "${p.mainPosition.code} ${p.fullName.substringAfterLast(" ")} (${p.overallRating()})",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (nx * 280).dp.coerceIn(4.dp, 240.dp),
                                    y = (ny * 235).dp.coerceIn(4.dp, 240.dp)
                                )
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = NavyCard)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Team Instructions", fontWeight = FontWeight.Bold, color = EmeraldAccent)
                Text("Passing Directness: ${tactic.passingDirectness}/100", fontSize = 12.sp)
                Slider(
                    value = tactic.passingDirectness.toFloat(),
                    onValueChange = { v ->
                        val updated = career.clubs.map {
                            if (it.id == userClub.id) it.copy(tactic = tactic.copy(passingDirectness = v.toInt())) else it
                        }
                        onUpdateCareer(career.copy(clubs = updated))
                    },
                    valueRange = 1f..100f
                )

                Text("Pressing Intensity: ${tactic.pressingIntensity}/100", fontSize = 12.sp)
                Slider(
                    value = tactic.pressingIntensity.toFloat(),
                    onValueChange = { v ->
                        val updated = career.clubs.map {
                            if (it.id == userClub.id) it.copy(tactic = tactic.copy(pressingIntensity = v.toInt())) else it
                        }
                        onUpdateCareer(career.copy(clubs = updated))
                    },
                    valueRange = 1f..100f
                )

                Button(
                    onClick = {
                        val best11 = clubPlayers.filter { it.injuryDaysRemaining == 0 }
                            .sortedByDescending { it.overallRating() }
                            .take(11)
                            .map { it.id }
                        val updated = career.clubs.map {
                            if (it.id == userClub.id) it.copy(tactic = tactic.copy(startingElevenIds = best11)) else it
                        }
                        onUpdateCareer(career.copy(clubs = updated))
                        onBanner("Assistant Manager selected highest-rated fit Starting XI.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("AUTO-PICK BEST STARTING XI", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CompetitionsStandingsTab(
    career: CareerSaveState,
    userClub: Club
) {
    var selectedDivId by remember { mutableStateOf(userClub.divisionId) }
    val activeDivs = career.divisions.filter { it.id in career.activeDivisionIds }
    val currentDiv = activeDivs.firstOrNull { it.id == selectedDivId } ?: career.divisions.first()
    val standings = remember(currentDiv, career.clubs, career.fixtures) {
        CompetitionEngine.calculateStandings(currentDiv, career.clubs, career.fixtures)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            activeDivs.forEach { div ->
                FilterChip(
                    selected = div.id == currentDiv.id,
                    onClick = { selectedDivId = div.id },
                    label = { Text(div.name, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${currentDiv.name} (Tier ${currentDiv.tier}) • Promo: ${currentDiv.promotionSpots} • Releg: ${currentDiv.relegationSpots}",
            color = EmeraldAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#  Club", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SlateText)
            Text("P   W   D   L   GD  PTS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SlateText)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(standings.size) { idx ->
                val row = standings[idx]
                val pos = idx + 1
                val rowColor = when {
                    row.clubId == userClub.id -> EmeraldAccent.copy(alpha = 0.22f)
                    pos <= currentDiv.promotionSpots -> Color(0xFF064E3B).copy(alpha = 0.5f)
                    pos > standings.size - currentDiv.relegationSpots -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
                    else -> NavyCard
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(rowColor)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$pos. ${row.clubName}",
                        modifier = Modifier.weight(1f),
                        fontWeight = if (row.clubId == userClub.id) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${row.played}  ${row.won}  ${row.drawn}  ${row.lost}  ${row.goalDifference}  ${row.points}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DirectorHubTab(
    career: CareerSaveState,
    userClub: Club,
    onUpdateCareer: (CareerSaveState) -> Unit,
    onSelectPlayer: (Player) -> Unit,
    onBanner: (String) -> Unit
) {
    var section by remember { mutableStateOf(HubSection.TRANSFERS_SCOUTING) }
    var marketQuery by remember { mutableStateOf("") }
    var newEditorClubName by remember { mutableStateOf(userClub.name) }
    var newEditorPlayerName by remember { mutableStateOf("Wonderkid Touzani") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HubSection.entries.forEach { sec ->
                FilterChip(
                    selected = section == sec,
                    onClick = { section = sec },
                    label = { Text(sec.label, fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        when (section) {
            HubSection.TRANSFERS_SCOUTING -> {
                OutlinedTextField(
                    value = marketQuery,
                    onValueChange = { marketQuery = it },
                    label = { Text("Global Player Search & Scouting (${career.players.size} Players)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                val results = remember(career.players, marketQuery) {
                    career.players
                        .filter { it.clubId != userClub.id && (marketQuery.isBlank() || it.fullName.contains(marketQuery, ignoreCase = true)) }
                        .sortedByDescending { it.overallRating() }
                        .take(40)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(results, key = { it.id }) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyCard)
                                .clickable { onSelectPlayer(p) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${p.mainPosition.code} ${p.fullName} (OVR ${p.overallRating()})", fontWeight = FontWeight.Bold)
                                val pot = p.estimatedPotentialRange()
                                Text("Age ${p.age} • ${p.nationalityId} • Est Pot ${pot.first}-${pot.second} • Value €${p.marketValue / 1_000}K", fontSize = 11.sp, color = SlateText)
                            }
                            Button(onClick = { onSelectPlayer(p) }) {
                                Text("Bid / Scout", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            HubSection.FINANCES_INFRA -> {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Club Financial Statement", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                            Text("Cash Balance: €${userClub.balance / 1_000}K", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Transfer Budget: €${userClub.transferBudget / 1_000}K", fontSize = 13.sp)
                            Text("Wage Budget: €${userClub.wageBudgetWeekly / 1_000}K / week", fontSize = 13.sp)
                            Text("Annual Broadcast + Sponsorship: €${(userClub.annualBroadcastRevenue + userClub.annualSponsorship) / 1_000}K", fontSize = 12.sp, color = SlateText)
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Infrastructure & Stadium Upgrades", color = AmberAccent, fontWeight = FontWeight.Bold)
                            Text("Stadium: ${userClub.stadiumName} (${userClub.stadiumCapacity} seats) • Training Lv ${userClub.trainingFacilitiesLevel}/20 • Youth Lv ${userClub.youthFacilitiesLevel}/20", fontSize = 12.sp)
                            Button(
                                onClick = {
                                    val cost = 4_000_000L
                                    if (userClub.balance >= cost) {
                                        val proj = InfrastructureProject(
                                            id = "PROJ_${System.currentTimeMillis()}",
                                            clubId = userClub.id,
                                            type = "STADIUM_EXPANSION",
                                            cost = cost,
                                            daysRemaining = 14
                                        )
                                        val updatedClubs = career.clubs.map {
                                            if (it.id == userClub.id) it.copy(balance = it.balance - cost) else it
                                        }
                                        onUpdateCareer(career.copy(clubs = updatedClubs, projects = career.projects + proj))
                                        onBanner("Stadium expansion (+4,000 seats) commissioned!")
                                    } else {
                                        onBanner("Board rejected project: insufficient balance.")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Expand Stadium (+4,000 Seats — €4.0M)")
                            }
                            Button(
                                onClick = {
                                    val cost = 2_000_000L
                                    if (userClub.balance >= cost) {
                                        val proj = InfrastructureProject(
                                            id = "PROJ_${System.currentTimeMillis()}",
                                            clubId = userClub.id,
                                            type = "YOUTH_UPGRADE",
                                            cost = cost,
                                            daysRemaining = 10
                                        )
                                        val updatedClubs = career.clubs.map {
                                            if (it.id == userClub.id) it.copy(balance = it.balance - cost) else it
                                        }
                                        onUpdateCareer(career.copy(clubs = updatedClubs, projects = career.projects + proj))
                                        onBanner("Youth Academy upgrade commissioned!")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Upgrade Youth Academy Facilities (€2.0M)")
                            }
                        }
                    }
                }
            }

            HubSection.YOUTH_TRAINING -> {
                val staffList = career.staff.filter { it.clubId == userClub.id }
                val youthPlayers = career.players.filter { it.clubId == userClub.id && it.age <= 20 }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Technical & Medical Staff (${staffList.size})", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                    }
                    items(staffList, key = { it.id }) { st ->
                        Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${st.role.title}: ${st.fullName}", fontWeight = FontWeight.Bold)
                                Text("Coaching ${st.coachingSkill} • Judging Pot ${st.judgingPlayerPotential} • Physio ${st.physiotherapySkill}", fontSize = 11.sp, color = SlateText)
                            }
                        }
                    }
                    item {
                        Text("U19 Youth Academy Prospects (${youthPlayers.size})", color = AmberAccent, fontWeight = FontWeight.Bold)
                    }
                    items(youthPlayers, key = { it.id }) { yp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavyCard)
                                .clickable { onSelectPlayer(yp) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${yp.mainPosition.code} ${yp.fullName} (Age ${yp.age})", fontWeight = FontWeight.SemiBold)
                            Text("OVR ${yp.overallRating()} • Pot ${yp.estimatedPotentialRange().second}", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HubSection.WORLD_RECORDS -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("World Database Coverage: ${career.divisions.size} Divisions across ${career.countries.size} Countries", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                    }
                    if (career.awardsHistory.isEmpty()) {
                        item {
                            Text("Season historical awards and champions will be recorded here at the end of each campaign.", color = SlateText, fontSize = 12.sp)
                        }
                    } else {
                        items(career.awardsHistory) { rec ->
                            Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("${rec.seasonYear} — ${rec.divisionName}", fontWeight = FontWeight.Bold, color = AmberAccent)
                                    Text("Champion: ${rec.championClubName} (Runner-up: ${rec.runnerUpClubName})", fontSize = 12.sp)
                                    Text("Golden Boot: ${rec.topScorerName} (${rec.topScorerGoals} goals) • POTY: ${rec.playerOfTheSeasonName}", fontSize = 11.sp, color = SlateText)
                                }
                            }
                        }
                    }
                }
            }

            HubSection.IN_GAME_EDITOR -> {
                val issues = remember(career) { DatabaseValidator.validateCareerState(career) }
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("In-Game Club & Badge Editor (2D Flat Crest)", color = EmeraldAccent, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = newEditorClubName,
                                onValueChange = { newEditorClubName = it },
                                label = { Text("Club Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BadgePattern.entries.take(4).forEach { pat ->
                                    FilterChip(
                                        selected = userClub.badgePattern == pat,
                                        onClick = {
                                            val updated = career.clubs.map {
                                                if (it.id == userClub.id) it.copy(badgePattern = pat) else it
                                            }
                                            onUpdateCareer(career.copy(clubs = updated))
                                        },
                                        label = { Text(pat.name.take(6), fontSize = 10.sp) }
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val updated = career.clubs.map {
                                        if (it.id == userClub.id) it.copy(
                                            name = newEditorClubName,
                                            balance = it.balance + 15_000_000L,
                                            transferBudget = it.transferBudget + 15_000_000L,
                                            provenance = DataProvenance.USER_CUSTOM
                                        ) else it
                                    }
                                    onUpdateCareer(career.copy(clubs = updated))
                                    onBanner("Club updated & +€15M injected via In-Game Editor!")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                            ) {
                                Text("APPLY CLUB RENAME & +€15M BUDGET", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = NavyCard), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Create Custom Wonderkid Player", color = AmberAccent, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = newEditorPlayerName,
                                onValueChange = { newEditorPlayerName = it },
                                label = { Text("Custom Player Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val customPlayer = Player(
                                        id = "CUSTOM_PLY_${System.currentTimeMillis()}",
                                        fullName = newEditorPlayerName.ifBlank { "Custom Star" },
                                        birthYear = career.seasonYear - 18,
                                        age = 18,
                                        nationalityId = userClub.countryId,
                                        mainPosition = Position.ST,
                                        clubId = userClub.id,
                                        technical = TechnicalAttributes(88, 84, 80, 89, 88, 45, 45, 82, 85, 90, 80, 88),
                                        mental = MentalAttributes(85, 86, 88, 89, 84, 82, 85, 88, 78, 70, 92, 90),
                                        physical = PhysicalAttributes(92, 93, 88, 82, 86, 84, 90, 90),
                                        hiddenPotentialAbility = 96,
                                        scoutedKnowledgePercent = 100,
                                        marketValue = 45_000_000L,
                                        provenance = DataProvenance.USER_CUSTOM
                                    )
                                    onUpdateCareer(career.copy(players = listOf(customPlayer) + career.players))
                                    onBanner("Created ${customPlayer.fullName} (OVR ${customPlayer.overallRating()}) in your squad!")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CREATE PLAYER IN MY SQUAD")
                            }
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = NavySurface), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Database Integrity Validator", fontWeight = FontWeight.Bold, color = EmeraldAccent)
                            Text(
                                text = if (issues.isEmpty()) "0 Integrity Errors across ${career.clubs.size} clubs and ${career.players.size} players."
                                else "${issues.size} issues detected.",
                                fontSize = 12.sp,
                                color = SlateText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerProfileModal(
    player: Player,
    career: CareerSaveState,
    userClub: Club,
    onDismiss: () -> Unit,
    onUpdateCareer: (CareerSaveState) -> Unit,
    onBanner: (String) -> Unit
) {
    val isOwnPlayer = player.clubId == userClub.id
    var bidFeeText by remember(player.id) { mutableStateOf((player.marketValue).toString()) }
    var wageOfferText by remember(player.id) { mutableStateOf((player.wageWeekly * 12 / 10).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = {
            Column {
                Text("${player.mainPosition.code} • ${player.fullName} (OVR ${player.overallRating()})", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                val pot = player.estimatedPotentialRange()
                Text(
                    text = "Age ${player.age} • ${player.nationalityId} • Value €${player.marketValue / 1_000}K • Wage €${player.wageWeekly}/wk • Pot ${pot.first}-${pot.second}",
                    fontSize = 11.sp,
                    color = EmeraldAccent
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Season Stats: Apps ${player.seasonAppearances} • Goals ${player.seasonGoals} • Assists ${player.seasonAssists} • Avg Rating ${player.averageRating}",
                    fontSize = 12.sp,
                    color = AmberAccent
                )
                Text(
                    text = "Technical: Fin ${player.technical.finishing} | Pas ${player.technical.passing} | Drb ${player.technical.dribbling} | Tck ${player.technical.tackling} | Tec ${player.technical.technique}",
                    fontSize = 11.sp
                )
                Text(
                    text = "Mental: Dec ${player.mental.decisions} | Vis ${player.mental.vision} | Cmp ${player.mental.composure} | Pos ${player.mental.positioning} | Wrk ${player.mental.workRate}",
                    fontSize = 11.sp
                )
                Text(
                    text = "Physical: Pac ${player.physical.pace} | Acc ${player.physical.acceleration} | Sta ${player.physical.stamina} | Str ${player.physical.strength} | Agi ${player.physical.agility}",
                    fontSize = 11.sp
                )

                Divider(color = SlateText.copy(alpha = 0.3f))

                if (isOwnPlayer) {
                    Button(
                        onClick = {
                            val updatedPlayers = career.players.map {
                                if (it.id == player.id) it.copy(
                                    contractExpiryYear = it.contractExpiryYear + 2,
                                    wageWeekly = (it.wageWeekly * 1.15).toLong(),
                                    morale = (it.morale + 10).coerceAtMost(100)
                                ) else it
                            }
                            onUpdateCareer(career.copy(players = updatedPlayers))
                            onBanner("Extended ${player.fullName}'s contract by 2 years!")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                    ) {
                        Text("Extend Contract (+2 Years)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    if (career.inGameEditorEnabled) {
                        OutlinedButton(
                            onClick = {
                                val updatedPlayers = career.players.map {
                                    if (it.id == player.id) it.copy(
                                        technical = it.technical.copy(
                                            finishing = (it.technical.finishing + 5).coerceAtMost(99),
                                            passing = (it.technical.passing + 5).coerceAtMost(99),
                                            dribbling = (it.technical.dribbling + 5).coerceAtMost(99),
                                            tackling = (it.technical.tackling + 5).coerceAtMost(99)
                                        ),
                                        physical = it.physical.copy(
                                            pace = (it.physical.pace + 5).coerceAtMost(99),
                                            stamina = (it.physical.stamina + 5).coerceAtMost(99)
                                        ),
                                        matchFitness = 100,
                                        fatigue = 0,
                                        injuryDaysRemaining = 0,
                                        provenance = DataProvenance.USER_CUSTOM
                                    ) else it
                                }
                                onUpdateCareer(career.copy(players = updatedPlayers))
                                onBanner("In-Game Editor: Boosted ${player.fullName}'s attributes (+5)!")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("In-Game Editor: Boost Attributes (+5) & Heal")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = bidFeeText,
                        onValueChange = { bidFeeText = it },
                        label = { Text("Transfer Offer Fee (€)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wageOfferText,
                        onValueChange = { wageOfferText = it },
                        label = { Text("Weekly Wage Offer (€)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val fee = bidFeeText.toLongOrNull() ?: player.marketValue
                                val wage = wageOfferText.toLongOrNull() ?: player.wageWeekly
                                val sellingClub = career.clubs.firstOrNull { it.id == player.clubId } ?: userClub
                                val userDiv = career.divisions.firstOrNull { it.id == userClub.divisionId }
                                val userSquad = career.players.filter { it.clubId == userClub.id }

                                val rawOffer = TransferOffer(
                                    id = "OFFER_${System.currentTimeMillis()}",
                                    playerId = player.id,
                                    playerName = player.fullName,
                                    fromClubId = sellingClub.id,
                                    toClubId = userClub.id,
                                    feeOffered = fee,
                                    offeredWageWeekly = wage
                                )
                                val evaluated = TransferAndContractEngine.evaluateTransferOffer(
                                    offer = rawOffer,
                                    player = player,
                                    sellingClub = sellingClub,
                                    buyingClub = userClub,
                                    buyingClubPlayers = userSquad,
                                    buyingDivision = userDiv
                                )
                                if (evaluated.status == "ACCEPTED") {
                                    val nextState = TransferAndContractEngine.executeAcceptedTransfer(career, evaluated)
                                    onUpdateCareer(nextState)
                                    onBanner("TRANSFER COMPLETED: ${player.fullName} signed for €${fee / 1_000}K!")
                                    onDismiss()
                                } else {
                                    onBanner("${evaluated.status}: ${evaluated.reason}")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Text("Submit Bid", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val updatedPlayers = career.players.map {
                                    if (it.id == player.id) it.copy(scoutedKnowledgePercent = 100) else it
                                }
                                onUpdateCareer(career.copy(players = updatedPlayers))
                                onBanner("Chief Scout completed 100% full report on ${player.fullName}!")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Scout 100%", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
