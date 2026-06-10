package com.futureme.financialai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.futureme.shared.models.RiskScore
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioResult
import com.futureme.financialai.ui.theme.Forest
import com.futureme.financialai.ui.theme.Mint
import com.futureme.financialai.ui.theme.Positive
import com.futureme.financialai.ui.theme.Warning

@Composable
fun SectionTitle(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Eyebrow(eyebrow)
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun Eyebrow(text: String, light: Boolean = false) {
    Text(
        text = text.uppercase(),
        color = if (light) Color(0xFF92B6A9) else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
fun HealthScoreCard(
    score: Int,
    label: String,
    summary: String,
    monthlySurplus: String,
    runway: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Forest),
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow("Financial health", light = true)
                    Text(
                        label,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        summary,
                        color = Color(0xFFACD9C8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text(
                    "Updated today",
                    color = Color(0xFFACD9C8),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(Color(0xFF214A3E), RoundedCornerShape(99.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            Row(
                modifier = Modifier.padding(top = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScoreRing(score)
                Column(
                    modifier = Modifier.padding(start = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    MiniStat("MONTHLY CUSHION", monthlySurplus)
                    MiniStat("CASH RUNWAY", runway)
                }
            }
        }
    }
}

@Composable
private fun ScoreRing(score: Int) {
    Box(
        modifier = Modifier
            .size(116.dp)
            .semantics {
                contentDescription = "Financial health score $score out of 100"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color(0xFF315047),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = Mint,
                startAngle = -90f,
                sweepAngle = score * 3.6f,
                useCenter = false,
                style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                score.toString(),
                color = Color.White,
                fontSize = 31.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("out of 100", color = Color(0xFF8EA99F), fontSize = 9.sp)
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF809C91), fontSize = 8.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun MetricCard(
    badge: String,
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f),
                        RoundedCornerShape(11.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    badge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            }
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(
                hint,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
fun ProjectionCard(
    result: ScenarioResult,
    headline: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow(headline)
                    Text(value, style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "${if (result.netWorthDelta5Years >= 0) "+" else ""}" +
                        result.netWorthDelta5Years.toLong(),
                    color = if (result.netWorthDelta5Years >= 0) Positive else Warning,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(99.dp),
                        )
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                )
            }
            FinancialLineChart(
                primary = result.projections.map { it.scenarioNetWorth },
                secondary = result.projections.map { it.baselineNetWorth },
                description = "Five-year projected net worth for ${result.scenario.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 14.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Legend("Selected scenario", Positive)
                Spacer(Modifier.width(14.dp))
                Legend("Current plan", Color(0xFFA5B2AC))
            }
        }
    }
}

@Composable
fun ComparisonChart(
    left: ScenarioResult,
    right: ScenarioResult,
    modifier: Modifier = Modifier,
) {
    FinancialLineChart(
        primary = left.projections.map { it.scenarioNetWorth },
        secondary = right.projections.map { it.scenarioNetWorth },
        primaryColor = Positive,
        secondaryColor = Color(0xFF7C70BD),
        description = "Five-year comparison of ${left.scenario.title} and ${right.scenario.title}",
        modifier = modifier,
    )
}

@Composable
private fun FinancialLineChart(
    primary: List<Double>,
    secondary: List<Double>,
    description: String,
    modifier: Modifier,
    primaryColor: Color = Positive,
    secondaryColor: Color = Color(0xFFA5B2AC),
) {
    Canvas(
        modifier = modifier.semantics { contentDescription = description },
    ) {
        val values = primary + secondary
        val minimum = values.minOrNull() ?: 0.0
        val maximum = values.maxOrNull() ?: 1.0
        repeat(4) { index ->
            val y = size.height * (index + 1) / 5f
            drawLine(
                color = Color(0xFF718078).copy(alpha = 0.25f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        fun pathFor(series: List<Double>): Path {
            val path = Path()
            series.forEachIndexed { index, value ->
                val x = index.toFloat() / (series.size - 1).coerceAtLeast(1) * size.width
                val normalized =
                    ((value - minimum) / (maximum - minimum).coerceAtLeast(1.0)).toFloat()
                val y = size.height - normalized * size.height * 0.82f - size.height * 0.09f
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
        }

        drawPath(
            path = pathFor(secondary),
            color = secondaryColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawPath(
            path = pathFor(primary),
            color = primaryColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(14.dp)
                .height(2.dp)
                .background(color),
        )
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}

@Composable
fun ScenarioCard(
    scenario: Scenario,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .semantics {
                role = Role.Button
                contentDescription = "Simulate ${scenario.title}. ${scenario.subtitle}"
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f),
                        RoundedCornerShape(11.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    scenario.type.name.take(4),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                scenario.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 13.dp),
            )
            Text(
                scenario.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 5.dp),
            )
            Text(
                "SIMULATE  >",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}

@Composable
fun InsightCard(
    result: ScenarioResult,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("FutureMe insight")
            Text(
                result.scenario.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                result.recommendation,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 7.dp),
            )
            result.tradeoffs.take(3).forEach { tradeoff ->
                Row(modifier = Modifier.padding(top = 9.dp)) {
                    Text("•", color = MaterialTheme.colorScheme.secondary)
                    Text(
                        tradeoff,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun RiskExplanationCard(
    assessment: RiskScore,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "${assessment.level.name.lowercase()} risk, " +
                        "${assessment.value} out of 100. ${assessment.summary}"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(
                            if (assessment.value >= 50) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                            },
                            RoundedCornerShape(18.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            assessment.value.toString(),
                            color = if (assessment.value >= 50) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("/100", fontSize = 8.sp)
                    }
                }
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Eyebrow("Transparent risk model")
                    Text(
                        "${assessment.level.name.lowercase().replaceFirstChar { it.uppercase() }} risk",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        assessment.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(14.dp),
                    ),
            ) {
                assessment.factors.forEach { factor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                factor.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                factor.explanation,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                            )
                        }
                        Text(
                            "${if (factor.points > 0) "+" else ""}${factor.points}",
                            color = if (factor.points < 0) Positive else Warning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImpactTile(
    label: String,
    value: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
        )
        Text(
            value,
            color = if (positive) Positive else Warning,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
