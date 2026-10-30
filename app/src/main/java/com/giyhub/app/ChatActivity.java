package com.giyhub.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private AiAgentRouter router;
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
    private TextView tvAgentSubStatus;

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
        initViews();
        setupLaunchers();
        setupTts();
        setupListeners();
        loadInitialGreeting();
    }

    private void initViews() {
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnAttachMedia = findViewById(R.id.btnAttachMedia);
        btnVoiceInput = findViewById(R.id.btnVoiceInput);
        btnTtsToggle = findViewById(R.id.btnTtsToggle);
        tvAgentSubStatus = findViewById(R.id.tvAgentSubStatus);

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
                    Toast.makeText(ChatActivity.this, "Action executed: " + action.toolName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeny(ChatMessage message) {
                message.pendingAction = null;
                message.toolResult = "Action dismissed by parent.";
                chatAdapter.notifyDataSetChanged();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);
    }

    private void setupLaunchers() {
        // File & Media sharing picker
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

        // Speech-to-Text Voice Recognition
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderStatus();
    }

    private void updateHeaderStatus() {
        String routerMode = router.getRouterMode().name();
        String autonomy = router.getAutonomyMode().name();
        String thinking = router.getThinkingMode().name();
        tvAgentSubStatus.setText(String.format("Router: %s • Autonomy: %s • %s", routerMode, autonomy, thinking));
    }

    private void loadInitialGreeting() {
        if (messageList.isEmpty()) {
            ChatMessage welcome = new ChatMessage(
                    "Hello! I am Andro-Assist's guardian AI Agent. I can manage device quotas, enforce instant pause, control Sentinel web safety, and assist with digital wellbeing.",
                    false,
                    "Loaded local guardian tools & agent capabilities.",
                    null
            );
            messageList.add(welcome);
            chatAdapter.notifyItemInserted(0);
        }
    }

    private void startVoiceRecognition() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Andro-Assist AI Agent...");
            voiceRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Voice recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty() && currentAttachmentUri == null) return;

        // User Message
        ChatMessage userMsg = new ChatMessage(text, true, null, currentAttachmentUri);
        messageList.add(userMsg);
        int userPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(userPos);
        recyclerChat.smoothScrollToPosition(userPos);

        etMessageInput.setText("");
        currentAttachmentUri = null;
        layoutAttachmentPreview.setVisibility(View.GONE);

        // Placeholder for Agent thinking
        ChatMessage agentPlaceholder = new ChatMessage("...", false, "Analyzing query & consulting guardian tools...", null);
        messageList.add(agentPlaceholder);
        int agentPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(agentPos);
        recyclerChat.smoothScrollToPosition(agentPos);

        router.processQuery(text, new AiAgentRouter.RouterCallback() {
            @Override
            public void onResponse(AiAgentRouter.AgentResponse response) {
                agentPlaceholder.text = response.responseText;
                agentPlaceholder.thinkingProcess = response.thinkingProcess;
                agentPlaceholder.toolResult = response.toolExecuted;
                agentPlaceholder.pendingAction = response.pendingAction;
                chatAdapter.notifyItemChanged(agentPos);
                recyclerChat.smoothScrollToPosition(agentPos);

                if (isTtsEnabled && tts != null && response.responseText != null) {
                    tts.speak(response.responseText, TextToSpeech.QUEUE_FLUSH, null, "AgentResponse");
                }
            }

            @Override
            public void onError(String error) {
                agentPlaceholder.text = "Error processing request: " + error;
                chatAdapter.notifyItemChanged(agentPos);
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
