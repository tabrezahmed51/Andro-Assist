package com.giyhub.app;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgentMemoryManagerTest {

    @Test
    public void testTokenEstimation() {
        assertEquals(0, AgentMemoryManager.estimateTokens(""));
        assertEquals(0, AgentMemoryManager.estimateTokens(null));

        String shortText = "Hello world! How are you?";
        int tokens = AgentMemoryManager.estimateTokens(shortText);
        assertTrue(tokens >= 5 && tokens <= 12);
    }

    @Test
    public void testUserTokenLimit() {
        String shortPrompt = "Pause all devices for dinner.";
        assertTrue(AgentMemoryManager.isUserPromptWithinLimit(shortPrompt));

        // Create prompt with 120 words (well over 100 tokens)
        StringBuilder longPrompt = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            longPrompt.append("word").append(i).append(" ");
        }

        assertFalse(AgentMemoryManager.isUserPromptWithinLimit(longPrompt.toString()));

        // Truncate should bring it within limit
        String truncated = AgentMemoryManager.truncateToMaxUserTokens(longPrompt.toString());
        assertTrue(AgentMemoryManager.isUserPromptWithinLimit(truncated));
        assertTrue(AgentMemoryManager.estimateTokens(truncated) <= AgentMemoryManager.MAX_USER_TOKENS);
    }

    @Test
    public void testBoundedThreadSlidingWindow() {
        List<ChatMessage> history = new ArrayList<>();

        // Add 50 messages of 100 tokens each (total 5,000 tokens, exceeds 3,000 limit)
        for (int i = 0; i < 50; i++) {
            StringBuilder text = new StringBuilder("Turn " + i + ": ");
            for (int w = 0; w < 60; w++) {
                text.append("token").append(w).append(" ");
            }
            history.add(new ChatMessage(text.toString(), i % 2 == 0, null, null));
        }

        List<ChatMessage> bounded = AgentMemoryManager.buildBoundedThread(history);

        assertNotNull(bounded);
        assertTrue(bounded.size() < 50); // Older turns must be dropped

        // Total tokens in bounded list must not exceed 3,000
        int totalTokens = 0;
        for (ChatMessage msg : bounded) {
            totalTokens += AgentMemoryManager.estimateTokens(msg.text);
        }
        assertTrue(totalTokens <= AgentMemoryManager.MAX_THREAD_TOKENS);

        // Most recent message (Turn 49) must be preserved
        ChatMessage last = bounded.get(bounded.size() - 1);
        assertTrue(last.text.startsWith("Turn 49:"));
    }

    @Test
    public void testFormatConversationTranscript() {
        List<ChatMessage> list = new ArrayList<>();
        list.add(new ChatMessage("Set quota to 90m", true, null, null));
        list.add(new ChatMessage("Daily quota set to 90 minutes.", false, null, null));

        String transcript = AgentMemoryManager.formatConversationTranscript(list);
        assertTrue(transcript.contains("User: Set quota to 90m"));
        assertTrue(transcript.contains("Agent: Daily quota set to 90 minutes."));
    }
}
