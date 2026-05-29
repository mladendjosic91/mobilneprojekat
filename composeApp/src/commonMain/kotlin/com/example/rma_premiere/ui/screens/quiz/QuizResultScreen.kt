package com.example.rma_premiere.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuizResultScreen(
    score: Float,
    correctAnswers: Int,
    timeUsedSeconds: Int,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    val wrongAnswers = TOTAL_QUESTIONS - correctAnswers

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Quiz Complete!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        // Score circle
        Card(
            modifier = Modifier.size(160.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format("%.2f", score), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text("/ 100", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            StatItem("Correct", correctAnswers.toString())
            StatItem("Wrong", wrongAnswers.toString())
            StatItem("Time", "${timeUsedSeconds}s")
        }

        Spacer(Modifier.height(40.dp))

        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Home") }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
