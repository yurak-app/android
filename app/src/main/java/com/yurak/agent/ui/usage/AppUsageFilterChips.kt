package com.yurak.agent.ui.usage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yurak.agent.ui.YurakCardBg
import com.yurak.agent.ui.YurakSlate
import com.yurak.agent.usage.AppFilter
import com.yurak.agent.usage.RealAppUsage

@Composable
fun AppUsageFilterChips(
    selectedFilter: AppFilter,
    onFilterSelected: (AppFilter) -> Unit,
    allApps: List<RealAppUsage>,
    modifier: Modifier = Modifier,
) {
    val totalCount = allApps.size
    val restrictedCount = allApps.count { it.isRestricted || it.isNearLimit }
    val unrestrictedCount = allApps.count { !it.isRestricted && !it.isNearLimit }

    val filters = listOf(
        Pair(AppFilter.BARCHASI, "Barchasi ($totalCount)"),
        Pair(AppFilter.CHEKLANGAN, "Cheklangan ($restrictedCount)"),
        Pair(AppFilter.CHEKLANMAGAN, "Cheklanmagan ($unrestrictedCount)"),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selectedFilter == filter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFFCCFBF1) else YurakCardBg,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) Color(0xFF2DD4BF) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.clickable { onFilterSelected(filter) },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F766E) else YurakSlate,
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
    }
}

