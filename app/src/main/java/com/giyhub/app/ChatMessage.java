package com.giyhub.app;

import android.net.Uri;

public class ChatMessage {

    public String text;
    public boolean isUser;
    public String thinkingProcess;
    public long timestamp;
    public Uri attachmentUri;
    public String toolResult;
    public AiAgentTools.PendingAction pendingAction;
    public boolean isThinkingExpanded = false;

    public ChatMessage(String text, boolean isUser, String thinkingProcess, Uri attachmentUri) {
        this.text = text;
        this.isUser = isUser;
        this.thinkingProcess = thinkingProcess;
        this.timestamp = System.currentTimeMillis();
        this.attachmentUri = attachmentUri;
    }
}
