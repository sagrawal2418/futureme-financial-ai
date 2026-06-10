package com.futureme.financialai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futureme.financialai.model.ScenarioComparison
import com.futureme.financialai.model.ScenarioResult
import com.futureme.financialai.ui.components.ComparisonChart
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.financialai.util.oneDecimal
import com.futureme.financialai.util.signedMoney

@Composable
fun ComparisonScreen(comparison: ScenarioComparison) {
    Column {
        SectionTitle(
            eyebrow = "Side by side",
            title = "Compare two paths",
        )
        Text(
            "Risk-adjusted comparison using the same household profile and projection policy.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ComparisonChart(
                    left = comparison.left,
                    right = comparison.right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        comparison.left.scenario.title,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                    Text(
                        comparison.right.scenario.title,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                }
                ComparisonOption(
                    label = "OPTION A",
                    result = comparison.left,
                    preferred = comparison.preferredScenarioId == comparison.left.scenario.id,
                    modifier = Modifier.padding(top = 20.dp),
                )
                ComparisonOption(
                    label = "OPTION B",
                    result = comparison.right,
                    preferred = comparison.preferredScenarioId == comparison.right.scenario.id,
                    modifier = Modifier.padding(top = 20.dp),
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                comparison.summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        Text(
            "Educational simulation only, not financial advice.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 22.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ComparisonOption(
    label: String,
    result: ScenarioResult,
    preferred: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
    ) {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Eyebrow(label)
                Text(result.scenario.title, style = MaterialTheme.typography.titleMedium)
            }
            if (preferred) {
                Text(
                    "RECOMMENDED",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Text(
            compactMoney(result.projectedNetWorth5Years),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "Projected five-year net worth",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Monthly surplus", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(money(result.projectedMonthlySurplus))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Emergency runway", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${oneDecimal(result.emergencyFundMonths)} months")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Risk score", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${result.risk.score}/100")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Five-year change", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(signedMoney(result.netWorthDelta5Years))
        }
    }
}
