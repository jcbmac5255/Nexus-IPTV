package com.nexus.iptv.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.nexus.iptv.R
import com.nexus.iptv.device.rememberIsTelevisionDevice
import com.nexus.iptv.ui.interaction.mouseClickable
import com.nexus.iptv.ui.theme.OnSurface
import com.nexus.iptv.ui.theme.OnSurfaceDim
import com.nexus.iptv.ui.theme.Primary
import com.nexus.iptv.ui.theme.SurfaceElevated
import com.nexus.iptv.update.ParsedReleaseNotes

@Composable
fun ReleaseNotesDialog(
    versionName: String,
    parsedNotes: ParsedReleaseNotes,
    onDismissRequest: () -> Unit
) {
    val isTelevisionDevice = rememberIsTelevisionDevice()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogContent: @Composable (Modifier) -> Unit = { resolvedModifier ->
            Surface(
                modifier = resolvedModifier,
                shape = RoundedCornerShape(24.dp),
                colors = SurfaceDefaults.colors(containerColor = SurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Primary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.release_notes_dialog_title, versionName),
                        style = MaterialTheme.typography.headlineMedium,
                        color = OnSurface
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (parsedNotes.isEmpty) {
                            Text(
                                text = stringResource(R.string.release_notes_dialog_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceDim
                            )
                        } else {
                            ReleaseSection(
                                title = stringResource(R.string.release_notes_dialog_section_features),
                                items = parsedNotes.features
                            )
                            ReleaseSection(
                                title = stringResource(R.string.release_notes_dialog_section_changes),
                                items = parsedNotes.changes
                            )
                            ReleaseSection(
                                title = stringResource(R.string.release_notes_dialog_section_fixes),
                                items = parsedNotes.fixes
                            )
                            if (parsedNotes.other.isNotBlank()) {
                                ReleaseSection(
                                    title = stringResource(R.string.release_notes_dialog_section_other),
                                    items = parsedNotes.other.lines().filter { it.isNotBlank() }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismissRequest,
                            modifier = Modifier.mouseClickable(onClick = onDismissRequest),
                            colors = ButtonDefaults.colors(
                                containerColor = Primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(R.string.release_notes_dialog_close))
                        }
                    }
                }
            }
        }

        if (isTelevisionDevice) {
            dialogContent(Modifier.fillMaxWidth(0.55f))
        } else {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val dialogModifier = when {
                    maxWidth < 700.dp -> Modifier.fillMaxWidth(0.9f)
                    maxWidth < 1280.dp -> Modifier.fillMaxWidth(0.7f)
                    else -> Modifier.fillMaxWidth(0.55f)
                }
                dialogContent(dialogModifier)
            }
        }
    }
}

@Composable
private fun ReleaseSection(title: String, items: List<String>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface
        )
        items.forEach { item ->
            Text(
                text = "•  $item",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDim
            )
        }
    }
}
