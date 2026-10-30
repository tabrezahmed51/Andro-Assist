package com.giyhub.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private AiAgentRouter router;
    private AgentMemoryManager memoryManager;
    private TextToSpeech tts;
    private boolean isTtsEnabled = true;

    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private ImageButton btnAttachMedia;
    private ImageButton btnVoiceInput;
    private ImageButton btnTtsToggle;
    private ImageButton btnClearThread;
    private TextView tvAgentSubStatus;
    private TextView tvThreadMemoryCounter;
    private TextView tvUserTokenCounter;

    private RelativeLayout layoutAttachmentPreview;
    private TextView tvAttachmentName;
    private ImageButton btnClearAttachment;
    private Uri currentAttachmentUri = null;

    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<Intent> voiceRecognitionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        router = new AiAgentRouter(this);
        memoryManager = new AgentMemoryManager(this);

        initViews();
        setupLaunchers();
        setupTts();
        setupListeners();
        loadChatHistory();
    }

    private void initViews() {
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnAttachMedia = findViewById(R.id.btnAttachMedia);
        btnVoiceInput = findViewById(R.id.btnVoiceInput);
        btnTtsToggle = findViewById(R.id.btnTtsToggle);
        btnClearThread = findViewById(R.id.btnClearThread);
        tvAgentSubStatus = findViewById(R.id.tvAgentSubStatus);
        tvThreadMemoryCounter = findViewById(R.id.tvThreadMemoryCounter);
        tvUserTokenCounter = findViewById(R.id.tvUserTokenCounter);

        layoutAttachmentPreview = findViewById(R.id.layoutAttachmentPreview);
        tvAttachmentName = findViewById(R.id.tvAttachmentName);
        btnClearAttachment = findViewById(R.id.btnClearAttachment);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, new ChatAdapter.OnActionListener() {
            @Override
            public void onApprove(ChatMessage message, AiAgentTools.PendingAction action) {
                if (action != null && action.executeRunnable != null) {
                    action.executeRunnable.run();
                    message.pendingAction = null;
                    message.toolResult = "Approved & Executed: " + action.description;
                    chatAdapter.notifyDataSetChanged();
                    memoryManager.saveChatThread(messageList);
                    updateMemoryUsageCounter();
                    Toast.makeText(ChatActivity.this, "Action executed: " + action.toolName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeny(ChatMessage message) {
                message.pendingAction = null;
                message.toolResult = "Action dismissed by parent.";
                chatAdapter.notifyDataSetChanged();
                memoryManager.saveChatThread(messageList);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);
    }

    private void setupLaunchers() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        currentAttachmentUri = uri;
                        layoutAttachmentPreview.setVisibility(View.VISIBLE);
                        tvAttachmentName.setText("📎 Attached: " + (uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "Media"));
                    }
                }
        );

        voiceRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            etMessageInput.setText(matches.get(0));
                            sendMessage();
                        }
                    }
                }
        );
    }

    private void setupTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnOpenIntegrateTab).setOnClickListener(v ->
                startActivity(new Intent(this, AiIntegrateActivity.class))
        );

        btnSendMessage.setOnClickListener(v -> sendMessage());

        btnAttachMedia.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnClearAttachment.setOnClickListener(v -> {
            currentAttachmentUri = null;
            layoutAttachmentPreview.setVisibility(View.GONE);
        });

        btnVoiceInput.setOnClickListener(v -> startVoiceRecognition());

        btnTtsToggle.setOnClickListener(v -> {
            isTtsEnabled = !isTtsEnabled;
            btnTtsToggle.setAlpha(isTtsEnabled ? 1.0f : 0.4f);
            Toast.makeText(this, isTtsEnabled ? "Voice Output ON" : "Voice Output Muted", Toast.LENGTH_SHORT).show();
        });

        btnClearThread.setOnClickListener(v -> showClearThreadConfirmation());

        // Live User Token Counter & 100-Token Limit Enforcement
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int tokens = AgentMemoryManager.estimateTokens(s != null ? s.toString() : "");
                tvUserTokenCounter.setText(String.format(Locale.US, "%d / %d user tokens", tokens, AgentMemoryManager.MAX_USER_TOKENS));

                if (tokens > AgentMemoryManager.MAX_USER_TOKENS) {
                    tvUserTokenCounter.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.accent_crimson));
                    btnSendMessage.setEnabled(false);
                    btnSendMessage.setAlpha(0.4f);
                } else {
                    tvUserTokenCounter.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.accent_emerald));
                    btnSendMessage.setEnabled(true);
                    btnSendMessage.setAlpha(1.0f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderStatus();
        updateMemoryUsageCounter();
    }

    private void updateHeaderStatus() {
        String routerMode = router.getRouterMode().name();
        String autonomy = router.getAutonomyMode().name();
        String thinking = router.getThinkingMode().name();
        tvAgentSubStatus.setText(String.format("Router: %s • Autonomy: %s • %s", routerMode, autonomy, thinking));
    }

    private void updateMemoryUsageCounter() {
        int totalTokens = 0;
        for (ChatMessage msg : messageList) {
            totalTokens += AgentMemoryManager.estimateTokens(msg.text);
            if (msg.thinkingProcess != null) {
                totalTokens += AgentMemoryManager.estimateTokens(msg.thinkingProcess);
            }
        }
        int cappedDisplay = Math.min(AgentMemoryManager.MAX_THREAD_TOKENS, totalTokens);
        tvThreadMemoryCounter.setText(String.format(Locale.US, "Thread Memory: %d / %d tokens (Sliding Window)", cappedDisplay, AgentMemoryManager.MAX_THREAD_TOKENS));
    }

    private void loadChatHistory() {
        List<ChatMessage> saved = memoryManager.loadChatThread();
        if (!saved.isEmpty()) {
            messageList.addAll(saved);
            chatAdapter.notifyDataSetChanged();
            recyclerChat.scrollToPosition(messageList.size() - 1);
        } else {
            ChatMessage welcome = new ChatMessage(
                    "Hello! I am Andro-Assist's guardian AI Agent. I remember conversation history up to 3,000 tokens. User inputs are limited to 100 tokens max.",
                    false,
                    "Initialized 3,000-token sliding memory buffer & 100-token prompt guard.",
                    null
            );
            messageList.add(welcome);
            chatAdapter.notifyItemInserted(0);
        }
        updateMemoryUsageCounter();
    }

    private void showClearThreadConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Conversation Memory")
                .setMessage("Reset the 3,000-token thread memory and start fresh?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    messageList.clear();
                    memoryManager.clearMemory();
                    chatAdapter.notifyDataSetChanged();
                    loadChatHistory();
                    Toast.makeText(this, "Agent memory reset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startVoiceRecognition() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Andro-Assist AI Agent (max 100 tokens)...");
            voiceRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty() && currentAttachmentUri == null) return;

        // Strict 100-token limit guard
        if (!AgentMemoryManager.isUserPromptWithinLimit(text)) {
            Toast.makeText(this, "Prompt exceeds 100 tokens maximum! Please shorten your prompt.", Toast.LENGTH_LONG).show();
            return;
        }

        // Add User Message
        ChatMessage userMsg = new ChatMessage(text, true, null, currentAttachmentUri);
        messageList.add(userMsg);
        int userPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(userPos);
        recyclerChat.smoothScrollToPosition(userPos);

        etMessageInput.setText("");
        currentAttachmentUri = null;
        layoutAttachmentPreview.setVisibility(View.GONE);
        updateMemoryUsageCounter();

        // Staged Agent Placeholder
        ChatMessage agentPlaceholder = new ChatMessage("...", false, "Reading 3000-token thread context & consulting guardian tools...", null);
        messageList.add(agentPlaceholder);
        int agentPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(agentPos);
        recyclerChat.smoothScrollToPosition(agentPos);

        // Process with bounded 3000-token context history
        router.processQuery(text, messageList, new AiAgentRouter.RouterCallback() {
            @Override
            public void onResponse(AiAgentRouter.AgentResponse response) {
                agentPlaceholder.text = response.responseText;
                agentPlaceholder.thinkingProcess = response.thinkingProcess;
                agentPlaceholder.toolResult = response.toolExecuted;
                agentPlaceholder.pendingAction = response.pendingAction;
                chatAdapter.notifyItemChanged(agentPos);
                recyclerChat.smoothScrollToPosition(agentPos);

                // Persist memory & update token counts
                memoryManager.saveChatThread(messageList);
                updateMemoryUsageCounter();

                if (isTtsEnabled && tts != null && response.responseText != null) {
                    tts.speak(response.responseText, TextToSpeech.QUEUE_FLUSH, null, "AgentResponse");
                }
            }

            @Override
            public void onError(String error) {
                agentPlaceholder.text = "Error processing request: " + error;
                chatAdapter.notifyItemChanged(agentPos);
                memoryManager.saveChatThread(messageList);
                updateMemoryUsageCounter();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
