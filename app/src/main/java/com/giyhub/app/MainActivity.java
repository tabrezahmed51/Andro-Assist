package com.giyhub.app;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RoleManager roleManager;
    private PairingManager pairingManager;
    private RewardManager rewardManager;
    private BedtimeManager bedtimeManager;
    private SentinelManager sentinelManager;
    private AppHealthManager healthManager;

    private TextView tvRoleBadge;
    private TextView tvHealthDiagnostics;
    private CardView cardRoleSetup;
    private CardView cardPermissionWarning;
    private CardView cardParentControls;
    private Button btnSelectParent;
    private Button btnSelectChild;
    private Button btnGrantPermission;
    private Button btnInstantPause;
    private Button btnChangeLimit;
    private Button btnSwitchRole;
    private Button btnPairing;
    private Button btnRewards;
    private Button btnLaunchAiChat;
    private Button btnLaunchAiIntegrate;

    private TextView tvSentinelStatus;
    private TextView tvSentinelDns;
    private Button btnToggleSentinel;

    private TextView tvTotalScreenTime;
    private ProgressBar progressDailyLimit;
    private TextView tvDailyLimitText;
    private TextView tvRemainingTime;
    private LinearLayout layoutTopAppsList;
    private TextView tvEmptyAppsState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roleManager = new RoleManager(this);
        pairingManager = new PairingManager(this);
        rewardManager = new RewardManager(this);
        bedtimeManager = new BedtimeManager();
        sentinelManager = new SentinelManager(this);
        healthManager = AppHealthManager.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        tvHealthDiagnostics = findViewById(R.id.tvHealthDiagnostics);
        cardRoleSetup = findViewById(R.id.cardRoleSetup);
        cardPermissionWarning = findViewById(R.id.cardPermissionWarning);
        cardParentControls = findViewById(R.id.cardParentControls);

        btnSelectParent = findViewById(R.id.btnSelectParent);
        btnSelectChild = findViewById(R.id.btnSelectChild);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        btnInstantPause = findViewById(R.id.btnInstantPause);
        btnChangeLimit = findViewById(R.id.btnChangeLimit);
        btnSwitchRole = findViewById(R.id.btnSwitchRole);
        btnPairing = findViewById(R.id.btnPairing);
        btnRewards = findViewById(R.id.btnRewards);
        btnLaunchAiChat = findViewById(R.id.btnLaunchAiChat);
        btnLaunchAiIntegrate = findViewById(R.id.btnLaunchAiIntegrate);

        tvSentinelStatus = findViewById(R.id.tvSentinelStatus);
        tvSentinelDns = findViewById(R.id.tvSentinelDns);
        btnToggleSentinel = findViewById(R.id.btnToggleSentinel);

        tvTotalScreenTime = findViewById(R.id.tvTotalScreenTime);
        progressDailyLimit = findViewById(R.id.progressDailyLimit);
        tvDailyLimitText = findViewById(R.id.tvDailyLimitText);
        tvRemainingTime = findViewById(R.id.tvRemainingTime);
        layoutTopAppsList = findViewById(R.id.layoutTopAppsList);
        tvEmptyAppsState = findViewById(R.id.tvEmptyAppsState);
    }

    private void setupListeners() {
        btnSelectParent.setOnClickListener(v -> {
            roleManager.setRole(RoleManager.ROLE_PARENT);
            applyRoleState();
        });

        btnSelectChild.setOnClickListener(v -> {
            roleManager.setRole(RoleManager.ROLE_CHILD);
            applyRoleState();
        });

        btnGrantPermission.setOnClickListener(v -> showProminentDisclosure());

        btnInstantPause.setOnClickListener(v -> toggleInstantPause());

        btnChangeLimit.setOnClickListener(v -> showChangeLimitDialog());

        btnSwitchRole.setOnClickListener(v -> promptPinForRoleSwitch());

        btnPairing.setOnClickListener(v -> showPairingDialog());

        btnRewards.setOnClickListener(v -> showRewardsDialog());

        btnLaunchAiChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class))
        );

        btnLaunchAiIntegrate.setOnClickListener(v ->
                startActivity(new Intent(this, AiIntegrateActivity.class))
        );

        btnToggleSentinel.setOnClickListener(v -> {
            if (roleManager.isChild()) {
                promptPinForSentinelToggle();
            } else {
                toggleSentinelFilter();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyRoleState();
        refreshScreenTime();
        refreshSentinelUI();
        tvHealthDiagnostics.setText(healthManager.getHealthDiagnostics());
        checkEnforcementRules();
    }

    private void refreshSentinelUI() {
        boolean active = sentinelManager.isFilterEnabled();
        if (active) {
            tvSentinelStatus.setText(R.string.sentinel_desc_active);
            btnToggleSentinel.setText(R.string.btn_toggle_sentinel_on);
            tvSentinelDns.setText(getString(R.string.sentinel_dns_format, sentinelManager.getActiveDnsProfile() + " (Family Safe)"));
        } else {
            tvSentinelStatus.setText(R.string.sentinel_desc_inactive);
            btnToggleSentinel.setText(R.string.btn_toggle_sentinel_off);
            tvSentinelDns.setText(getString(R.string.sentinel_dns_format, sentinelManager.getActiveDnsProfile()));
        }
    }

    private void toggleSentinelFilter() {
        boolean newState = !sentinelManager.isFilterEnabled();
        sentinelManager.setFilterEnabled(newState);
        refreshSentinelUI();
        Toast.makeText(this, newState ? "Sentinel 18+ Web Shield Enabled" : "Sentinel Filter Disabled (All Gates Open)", Toast.LENGTH_SHORT).show();
    }

    private void promptPinForSentinelToggle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pin_dialog_title);
        builder.setMessage(R.string.pin_dialog_prompt);

        final EditText pinInput = new EditText(this);
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(pinInput);

        builder.setPositiveButton(R.string.pin_confirm, (dialog, which) -> {
            String enteredPin = pinInput.getText().toString().trim();
            if (roleManager.verifyPin(enteredPin)) {
                toggleSentinelFilter();
            } else {
                Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.pin_cancel, null);
        builder.show();
    }

    private void applyRoleState() {
        String role = roleManager.getRole();

        if (RoleManager.ROLE_UNSET.equals(role)) {
            cardRoleSetup.setVisibility(View.VISIBLE);
            cardParentControls.setVisibility(View.GONE);
            tvRoleBadge.setText("Setup Required");
            stopChildService();
            return;
        }

        cardRoleSetup.setVisibility(View.GONE);

        if (RoleManager.ROLE_PARENT.equals(role)) {
            tvRoleBadge.setText("Parent Mode");
            cardParentControls.setVisibility(View.VISIBLE);
            updatePauseButtonState();
            stopChildService();
        } else {
            tvRoleBadge.setText("Child Mode");
            cardParentControls.setVisibility(View.GONE);
            startChildService();
        }
    }

    private void startChildService() {
        Intent serviceIntent = new Intent(this, ChildMonitorService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopChildService() {
        Intent serviceIntent = new Intent(this, ChildMonitorService.class);
        serviceIntent.setAction(ChildMonitorService.ACTION_STOP);
        startService(serviceIntent);
    }

    private void updatePauseButtonState() {
        if (roleManager.isInstantPauseActive()) {
            btnInstantPause.setText(R.string.btn_instant_pause_off);
        } else {
            btnInstantPause.setText(R.string.btn_instant_pause_on);
        }
    }

    private void toggleInstantPause() {
        boolean newState = !roleManager.isInstantPauseActive();
        roleManager.setInstantPause(newState);
        updatePauseButtonState();
        Toast.makeText(this, newState ? "All child devices paused" : "Child devices resumed", Toast.LENGTH_SHORT).show();
    }

    private void showChangeLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.btn_change_limit);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Minutes (e.g., 90, 120, 180)");
        input.setText(String.valueOf(roleManager.getDailyLimitMinutes()));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String val = input.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    int mins = Integer.parseInt(val);
                    roleManager.setDailyLimitMinutes(mins);
                    refreshScreenTime();
                    Toast.makeText(this, "Daily limit updated", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException ignored) {}
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPairingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.btn_pairing);
        String code = pairingManager.getActivePairingCode();
        String family = pairingManager.getFamilyId();

        builder.setMessage(String.format("Family ID: %s\nDevice ID: %s\n\nPairing Code:\n%s\n\nEnter this code on child devices to link.", family, pairingManager.getDeviceId(), code));
        builder.setPositiveButton("Done", null);
        builder.show();
    }

    private void showRewardsDialog() {
        List<RewardManager.ChoreTask> chores = rewardManager.getDefaultTasks();
        String[] titles = new String[chores.size()];
        for (int i = 0; i < chores.size(); i++) {
            RewardManager.ChoreTask task = chores.get(i);
            titles[i] = String.format("%s (+%dm)", task.title, task.rewardMinutes);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chore & Bonus Time Board");
        builder.setItems(titles, (dialog, which) -> {
            RewardManager.ChoreTask selected = chores.get(which);
            rewardManager.addBonusMinutes(selected.rewardMinutes);
            Toast.makeText(this, String.format("Approved: +%dm bonus screen time awarded!", selected.rewardMinutes), Toast.LENGTH_SHORT).show();
            refreshScreenTime();
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showProminentDisclosure() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.prominent_disclosure_title);
        builder.setMessage(R.string.prominent_disclosure_body);
        builder.setPositiveButton("Accept & Continue", (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void promptPinForRoleSwitch() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pin_dialog_title);
        builder.setMessage(R.string.pin_dialog_prompt);

        final EditText pinInput = new EditText(this);
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(pinInput);

        builder.setPositiveButton(R.string.pin_confirm, (dialog, which) -> {
            String enteredPin = pinInput.getText().toString().trim();
            if (roleManager.verifyPin(enteredPin)) {
                roleManager.resetRole();
                applyRoleState();
            } else {
                Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.pin_cancel, null);
        builder.show();
    }

    private void checkEnforcementRules() {
        if (!roleManager.isChild()) return;

        // Check if Bedtime is currently active
        if (bedtimeManager.isBedtimeActiveNow()) {
            Intent lockIntent = new Intent(this, LockOverlayActivity.class);
            lockIntent.putExtra(LockOverlayActivity.EXTRA_REASON, "BEDTIME");
            startActivity(lockIntent);
            return;
        }

        // Check if Instant Pause is active
        if (roleManager.isInstantPauseActive()) {
            Intent lockIntent = new Intent(this, LockOverlayActivity.class);
            lockIntent.putExtra(LockOverlayActivity.EXTRA_REASON, "INSTANT_PAUSE");
            startActivity(lockIntent);
        }
    }

    private void refreshScreenTime() {
        if (!hasUsageStatsPermission()) {
            cardPermissionWarning.setVisibility(View.VISIBLE);
            tvTotalScreenTime.setText("0h 00m");
            progressDailyLimit.setProgress(0);
            return;
        }

        cardPermissionWarning.setVisibility(View.GONE);
        loadUsageData();
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;

        int mode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    getPackageName()
            );
        } else {
            mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    getPackageName()
            );
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void loadUsageData() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        int baseLimit = roleManager.getDailyLimitMinutes();
        int bonus = rewardManager.getBonusMinutes();
        int totalLimit = baseLimit + bonus;

        tvDailyLimitText.setText(getString(R.string.daily_limit_format, ScreenTimeHelper.formatDuration(totalLimit)));

        if (stats == null || stats.isEmpty()) {
            tvTotalScreenTime.setText("0h 00m");
            progressDailyLimit.setProgress(0);
            tvRemainingTime.setText(getString(R.string.remaining_time_format, ScreenTimeHelper.formatDuration(totalLimit)));
            tvEmptyAppsState.setVisibility(View.VISIBLE);
            return;
        }

        Map<String, Long> usageMap = new HashMap<>();
        long totalMillis = 0;

        for (UsageStats stat : stats) {
            long foregroundTime = stat.getTotalTimeInForeground();
            if (foregroundTime > 60_000L) {
                usageMap.put(stat.getPackageName(), foregroundTime);
                totalMillis += foregroundTime;
            }
        }

        int totalMinutes = (int) (totalMillis / (1000 * 60));
        tvTotalScreenTime.setText(ScreenTimeHelper.formatDuration(totalMinutes));

        int progress = ScreenTimeHelper.calculateProgress(totalMinutes, totalLimit);
        progressDailyLimit.setProgress(progress);

        int remainingMinutes = ScreenTimeHelper.calculateRemaining(totalMinutes, totalLimit);
        tvRemainingTime.setText(getString(
                R.string.remaining_time_format,
                ScreenTimeHelper.formatDuration(remainingMinutes)
        ));

        // Trigger lock overlay if Child and time is completely up
        if (roleManager.isChild() && remainingMinutes <= 0) {
            Intent lockIntent = new Intent(this, LockOverlayActivity.class);
            lockIntent.putExtra(LockOverlayActivity.EXTRA_REASON, "LIMIT_EXCEEDED");
            startActivity(lockIntent);
        }

        displayTopApps(usageMap);
    }

    private void displayTopApps(Map<String, Long> usageMap) {
        layoutTopAppsList.removeAllViews();

        if (usageMap.isEmpty()) {
            layoutTopAppsList.addView(tvEmptyAppsState);
            tvEmptyAppsState.setVisibility(View.VISIBLE);
            return;
        }

        PackageManager pm = getPackageManager();
        List<Map.Entry<String, Long>> sortedList = new ArrayList<>(usageMap.entrySet());
        Collections.sort(sortedList, (a, b) -> Long.compare(b.getValue(), a.getValue()));

        int count = 0;
        for (Map.Entry<String, Long> entry : sortedList) {
            if (count++ >= 5) break;

            String packageName = entry.getKey();
            String appName;
            try {
                appName = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                appName = packageName.contains(".")
                        ? packageName.substring(packageName.lastIndexOf('.') + 1)
                        : packageName;
            }

            int mins = (int) (entry.getValue() / (1000 * 60));
            String formattedTime = ScreenTimeHelper.formatDuration(mins);

            TextView row = new TextView(this);
            row.setText(String.format("%s — %s", appName, formattedTime));
            row.setTextSize(14f);
            row.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            row.setPadding(0, 8, 0, 8);

            layoutTopAppsList.addView(row);
        }
    }
}
