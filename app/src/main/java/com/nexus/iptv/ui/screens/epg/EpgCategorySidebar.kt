package com.nexus.iptv.ui.screens.epg

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nexus.iptv.domain.model.Category
import com.nexus.iptv.ui.design.AppColors
import com.nexus.iptv.ui.interaction.TvClickableSurface

@Composable
internal fun GuideCategorySidebar(
    categories: List<Category>,
    selectedCategoryId: Long,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer top padding (vs. contentPadding) so the LazyColumn's frame itself starts below
    // the EpgGrid's timeline header — without this, categories scroll up *into* the header
    // area instead of clipping at the top of the sidebar.
    LazyColumn(
        modifier = modifier
            .width(170.dp)
            .padding(top = 46.dp)
            .fillMaxHeight(),
        contentPadding = PaddingValues(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            GuideCategoryRow(
                category = category,
                selected = category.id == selectedCategoryId,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun GuideCategoryRow(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    TvClickableSurface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) AppColors.Brand.copy(alpha = 0.22f) else Color.Transparent,
            focusedContainerColor = AppColors.Brand.copy(alpha = 0.42f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = if (selected) {
                Border(
                    border = BorderStroke(1.dp, AppColors.Brand.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Border.None
            },
            focusedBorder = Border(
                border = BorderStroke(2.dp, AppColors.Focus),
                shape = RoundedCornerShape(8.dp)
            )
        )
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) AppColors.TextPrimary else AppColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}
