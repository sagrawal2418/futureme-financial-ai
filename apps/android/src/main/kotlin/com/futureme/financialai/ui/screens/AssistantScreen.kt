package com.futureme.financialai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.ChatMessage
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.shared.models.AiEvaluationDashboard
import com.futureme.shared.models.SuggestedQuestion

@Composable
fun AssistantScreen(
    messages: List<ChatMessage>,
    suggestions: List<SuggestedQuestion>,
    evaluation: AiEvaluationDashboard,
    onAsk: (String) -> Unit,
) {
    var question by rememberSaveable { mutableStateOf("") }

    Column {
        SectionTitle(
            eyebrow = "FutureMe AI coach",
            title = "Your financial strategist",
        )
        Text(
            "Grounded in shared readiness, risk, and scenario results. AI explains; the engines calculate.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { suggestion ->
                OutlinedButton(
                    onClick = { onAsk(suggestion.prompt) },
                    modifier = Modifier.semantics {
                        contentDescription = "Ask ${suggestion.title}"
                    },
                ) {
                    Text(suggestion.title)
                }
            }
        }

        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            messages.forEach { message ->
                ChatBubble(message)
            }
        }

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Ask what is blocking your next life decision") },
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .semantics {
                    contentDescription = "Financial strategist question"
                },
        )
        Button(
            onClick = {
                onAsk(question)
                question = ""
            },
            enabled = question.isNotBlank(),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 10.dp)
                .semantics {
                    contentDescription = "Ask FutureMe AI coach"
                },
        ) {
            Text("Ask my coach")
        }

        SectionTitle(
            eyebrow = "AI EVALUATION DASHBOARD",
            title = "Can we trust the explanation quality?",
            modifier = Modifier.padding(top = 28.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    evaluation.statusLabel,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    evaluation.methodologyNote,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
                evaluation.categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(category.label)
                        Text(
                            "${category.promptCount} prompts",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
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
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .semantics {
                    contentDescription =
                        "${if (message.isUser) "Your question" else "FutureMe response"}: " +
                            message.text
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
        ) {
            Text(
                message.text,
                color = if (message.isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .background(androidx.compose.ui.graphics.Color.Transparent)
                    .padding(16.dp),
            )
        }
    }
}
