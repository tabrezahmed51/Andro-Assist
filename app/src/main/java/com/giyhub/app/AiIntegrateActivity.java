package com.giyhub.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AiIntegrateActivity extends AppCompatActivity {

    private DeviceHardwareProfiler.HardwareProfile currentProfile;
    private ModelCatalog modelCatalog;
    private AiAgentRouter router;
    private AiCloudProviderManager cloudManager;

    private TextView tvHardwareProfile;
    private Button btnScanHardware;

    private TextView tvAutonomyDesc;
    private Button btnAutonomyYolo;
    private Button btnAutonomyAlwaysAsk;
    private Button btnAutonomyManual;

    private TextView tvRouterStatus;
    private Button btnRouterAuto;
    private Button btnRouterManual;

    private TextView tvThinkingStatus;
    private Button btnCycleThinking;

    private LinearLayout layoutModelsList;
    private Button btnConfigPollinations;
    private Button btnConfigPuter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_integrate);

        modelCatalog = new ModelCatalog(this);
        router = new AiAgentRouter(this);
        cloudManager = new AiCloudProviderManager(this);

        initViews();
        setupListeners();
        refreshAllUI();
    }

    private void initViews() {
        tvHardwareProfile = findViewById(R.id.tvHardwareProfile);
        btnScanHardware = findViewById(R.id.btnScanHardware);

        tvAutonomyDesc = findViewById(R.id.tvAutonomyDesc);
        btnAutonomyYolo = findViewById(R.id.btnAutonomyYolo);
        btnAutonomyAlwaysAsk = findViewById(R.id.btnAutonomyAlwaysAsk);
        btnAutonomyManual = findViewById(R.id.btnAutonomyManual);

        tvRouterStatus = findViewById(R.id.tvRouterStatus);
        btnRouterAuto = findViewById(R.id.btnRouterAuto);
        btnRouterManual = findViewById(R.id.btnRouterManual);

        tvThinkingStatus = findViewById(R.id.tvThinkingStatus);
        btnCycleThinking = findViewById(R.id.btnCycleThinking);

        layoutModelsList = findViewById(R.id.layoutModelsList);
        btnConfigPollinations = findViewById(R.id.btnConfigPollinations);
        btnConfigPuter = findViewById(R.id.btnConfigPuter);
    }

    private void setupListeners() {
        findViewById(R.id.btnBackIntegrate).setOnClickListener(v -> finish());

        btnScanHardware.setOnClickListener(v -> {
            currentProfile = DeviceHardwareProfiler.scanDevice(this);
            tvHardwareProfile.setText(currentProfile.getSummary());
            Toast.makeText(this, "Hardware analysis complete! Recommended: " + currentProfile.tier.recommendedModel, Toast.LENGTH_SHORT).show();
        });

        btnAutonomyYolo.setOnClickListener(v -> {
            router.setAutonomyMode(AiAgentRouter.AutonomyMode.YOLO);
            refreshAutonomyUI();
        });

        btnAutonomyAlwaysAsk.setOnClickListener(v -> {
            router.setAutonomyMode(AiAgentRouter.AutonomyMode.ALWAYS_ASK);
            refreshAutonomyUI();
        });

        btnAutonomyManual.setOnClickListener(v -> {
            router.setAutonomyMode(AiAgentRouter.AutonomyMode.MANUAL);
            refreshAutonomyUI();
        });

        btnRouterAuto.setOnClickListener(v -> {
            router.setRouterMode(AiAgentRouter.RouterMode.AUTO);
            refreshRouterUI();
        });

        btnRouterManual.setOnClickListener(v -> {
            router.setRouterMode(AiAgentRouter.RouterMode.MANUAL);
            refreshRouterUI();
        });

        btnCycleThinking.setOnClickListener(v -> cycleThinkingMode());

        btnConfigPollinations.setOnClickListener(v -> showPollinationsDialog());

        btnConfigPuter.setOnClickListener(v -> showPuterDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAllUI();
    }

    private void refreshAllUI() {
        refreshAutonomyUI();
        refreshRouterUI();
        refreshThinkingUI();
        refreshModelsList();
    }

    private void refreshAutonomyUI() {
        AiAgentRouter.AutonomyMode mode = router.getAutonomyMode();
        tvAutonomyDesc.setText(mode.title + "\n" + mode.description);
    }

    private void refreshRouterUI() {
        AiAgentRouter.RouterMode mode = router.getRouterMode();
        tvRouterStatus.setText(mode.title + ": " + mode.description);
    }

    private void refreshThinkingUI() {
        AiAgentRouter.ThinkingMode mode = router.getThinkingMode();
        tvThinkingStatus.setText("Thinking Budget: " + mode.title + " (" + mode.description + ")");
    }

    private void cycleThinkingMode() {
        AiAgentRouter.ThinkingMode current = router.getThinkingMode();
        AiAgentRouter.ThinkingMode[] modes = AiAgentRouter.ThinkingMode.values();
        int nextIndex = (current.ordinal() + 1) % modes.length;
        router.setThinkingMode(modes[nextIndex]);
        refreshThinkingUI();
        Toast.makeText(this, "Switched to: " + modes[nextIndex].title, Toast.LENGTH_SHORT).show();
    }

    private void refreshModelsList() {
        layoutModelsList.removeAllViews();
        List<ModelCatalog.ModelItem> models = ModelCatalog.getAvailableModels();

        for (ModelCatalog.ModelItem item : models) {
            ModelCatalog.DownloadStatus status = modelCatalog.getStatus(item);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(0, 10, 0, 16);

            TextView title = new TextView(this);
            title.setText(item.name + " (" + item.sizeMb + " MB)");
            title.setTextSize(14f);
            title.setTextColor(0xFF0F172A);
            title.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView desc = new TextView(this);
            desc.setText(item.description);
            desc.setTextSize(12f);
            desc.setTextColor(0xFF64748B);
            desc.setPadding(0, 2, 0, 6);

            TextView statusText = new TextView(this);
            statusText.setText("Status: " + status.name());
            statusText.setTextSize(12f);
            statusText.setTypeface(null, android.graphics.Typeface.BOLD);
            statusText.setTextColor(status == ModelCatalog.DownloadStatus.LOADED ? 0xFF10B981 : 0xFF2563EB);

            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonRow.setPadding(0, 6, 0, 0);

            if (status == ModelCatalog.DownloadStatus.NOT_DOWNLOADED) {
                Button btnDownload = new Button(this);
                btnDownload.setText("Download GGUF");
                btnDownload.setTextSize(12f);
                btnDownload.setOnClickListener(v -> {
                    modelCatalog.startDownload(item);
                    Toast.makeText(this, "Enqueued download from Hugging Face for " + item.name, Toast.LENGTH_SHORT).show();
                    refreshModelsList();
                });
                buttonRow.addView(btnDownload);
            } else if (status == ModelCatalog.DownloadStatus.DOWNLOADING) {
                Button btnDownloading = new Button(this);
                btnDownloading.setText("Downloading... (Pause)");
                btnDownloading.setTextSize(12f);
                buttonRow.addView(btnDownloading);
            } else if (status == ModelCatalog.DownloadStatus.DOWNLOADED) {
                Button btnLoad = new Button(this);
                btnLoad.setText("Load into Chat");
                btnLoad.setTextSize(12f);
                btnLoad.setOnClickListener(v -> {
                    modelCatalog.loadModel(item);
                    Toast.makeText(this, "Loaded " + item.name + " into Chat Agent memory!", Toast.LENGTH_SHORT).show();
                    refreshModelsList();
                });
                buttonRow.addView(btnLoad);

                Button btnDelete = new Button(this);
                btnDelete.setText("Remove");
                btnDelete.setTextSize(12f);
                btnDelete.setOnClickListener(v -> {
                    modelCatalog.removeModel(item);
                    Toast.makeText(this, "Deleted " + item.name + " to free up space.", Toast.LENGTH_SHORT).show();
                    refreshModelsList();
                });
                buttonRow.addView(btnDelete);
            } else if (status == ModelCatalog.DownloadStatus.LOADED) {
                Button btnUnload = new Button(this);
                btnUnload.setText("Unload");
                btnUnload.setTextSize(12f);
                btnUnload.setOnClickListener(v -> {
                    modelCatalog.unloadModel();
                    Toast.makeText(this, "Unloaded model from Chat memory.", Toast.LENGTH_SHORT).show();
                    refreshModelsList();
                });
                buttonRow.addView(btnUnload);

                Button btnDelete = new Button(this);
                btnDelete.setText("Remove");
                btnDelete.setTextSize(12f);
                btnDelete.setOnClickListener(v -> {
                    modelCatalog.removeModel(item);
                    Toast.makeText(this, "Deleted " + item.name, Toast.LENGTH_SHORT).show();
                    refreshModelsList();
                });
                buttonRow.addView(btnDelete);
            }

            card.addView(title);
            card.addView(desc);
            card.addView(statusText);
            card.addView(buttonRow);

            layoutModelsList.addView(card);
        }
    }

    private void showPollinationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pollinations AI Configuration");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etUser = new EditText(this);
        etUser.setHint("Pollinations Username / Account ID");
        etUser.setText(cloudManager.getPollinationsUser());
        layout.addView(etUser);

        final EditText etKey = new EditText(this);
        etKey.setHint("API Key (Optional for free tier)");
        etKey.setText(cloudManager.getPollinationsKey());
        layout.addView(etKey);

        builder.setView(layout);

        builder.setPositiveButton("Save / Sign-in", (dialog, which) -> {
            cloudManager.savePollinationsAccount(etUser.getText().toString().trim(), etKey.getText().toString().trim());
            Toast.makeText(this, "Pollinations credentials saved", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Reset Account", (dialog, which) -> {
            cloudManager.resetPollinations();
            Toast.makeText(this, "Pollinations reset", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPuterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Puter.js AI Configuration");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etUser = new EditText(this);
        etUser.setHint("Puter Username / Client ID");
        etUser.setText(cloudManager.getPuterUser());
        layout.addView(etUser);

        final EditText etToken = new EditText(this);
        etToken.setHint("Puter Auth Token / API Secret");
        etToken.setText(cloudManager.getPuterToken());
        layout.addView(etToken);

        builder.setView(layout);

        builder.setPositiveButton("Save / Sign-in", (dialog, which) -> {
            cloudManager.savePuterAccount(etUser.getText().toString().trim(), etToken.getText().toString().trim());
            Toast.makeText(this, "Puter credentials saved", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Reset Account", (dialog, which) -> {
            cloudManager.resetPuter();
            Toast.makeText(this, "Puter account reset", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
