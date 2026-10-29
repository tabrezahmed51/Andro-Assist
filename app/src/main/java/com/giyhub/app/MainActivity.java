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

    private TextView tvRoleBadge;
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

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyRoleState();
        refreshScreenTime();
        checkEnforcementRules();
    }

    private void applyRoleState() {
        String role = roleManager.getRole();

        if (RoleManager.ROLE_UNSET.equals(role)) {
            cardRoleSetup.setVisibility(View.VISIBLE);
            cardParentControls.setVisibility(View.GONE);
            tvRoleBadge.setText("Setup Required");
            tvRoleBadge.setBackgroundColor(0xFFE2E8F0);
            stopChildService();
            return;
        }

        cardRoleSetup.setVisibility(View.GONE);

        if (RoleManager.ROLE_PARENT.equals(role)) {
            tvRoleBadge.setText("Parent Mode");
            tvRoleBadge.setBackgroundColor(0xFFDBEAFE);
            tvRoleBadge.setTextColor(0xFF1D4ED8);
            cardParentControls.setVisibility(View.VISIBLE);
            updatePauseButtonState();
            stopChildService();
        } else {
            tvRoleBadge.setText("Child Mode");
            tvRoleBadge.setBackgroundColor(0xFFDCFCE7);
            tvRoleBadge.setTextColor(0xFF15803D);
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
            btnInstantPause.setBackgroundColor(0xFF16A34A);
        } else {
            btnInstantPause.setText(R.string.btn_instant_pause_on);
            btnInstantPause.setBackgroundColor(0xFFEF4444);
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
            row.setTextColor(0xFF334155);
            row.setPadding(0, 8, 0, 8);

            layoutTopAppsList.addView(row);
        }
    }
}
