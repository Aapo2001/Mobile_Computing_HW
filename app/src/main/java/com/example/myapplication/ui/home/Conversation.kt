package com.example.myapplication.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.File

data class Message(val author: String, val body: String, val imagePath: String? = null)


@Composable
fun MessageCard(msg: Message, userImagePath: String? = null) {
    val isGemma = msg.author == "Gemma"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isGemma) Arrangement.Start else Arrangement.End
    ) {
        if (isGemma) {
            // Avatar for Gemma (on the left)
            val imageModifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)

            Image(
                painter = painterResource(R.drawable.profile_picture),
                contentDescription = "Gemma",
                modifier = imageModifier
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // We keep track if the message is expanded or not
        var isExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable { isExpanded = !isExpanded },
            horizontalAlignment = if (isGemma) Alignment.Start else Alignment.End
        ) {
            Text(
                text = msg.author,
                color = if (isGemma) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // M3 Card for message bubble
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isGemma)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.animateContentSize()
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGemma)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (!isGemma) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar for user (on the right)
            val imageModifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.tertiaryContainer, CircleShape)

            val imagePath = msg.imagePath ?: userImagePath
            if (imagePath != null && File(imagePath).exists()) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = "Profile picture",
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.profile_picture),
                    contentDescription = null,
                    modifier = imageModifier
                )
            }
        }
    }
}
@Composable
fun Conversation(messages: List<Message>, userImagePath: String? = null) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(messages) { message ->
            MessageCard(message, userImagePath)
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    MyApplicationTheme{
        Conversation(SampleData.conversationSample)
    }
}
