package com.giyhub.app;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RoleManager roleManager;
    private PairingManager pairingManager;
    private RewardManager rewardManager;
    private BedtimeManager bedtimeManager;
    private SentinelManager sentinelManager;
    private AppHealthManager healthManager;
    private FinancialProtectionManager financialManager;
    private HardwareUsageReporter usageReporter;
    private AppRecommendationManager appRecManager;

    private TextView tvRoleBadge;
    private TextView tvLivePulseRadar;
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

    // Sentinel UI
    private TextView tvSentinelGateBadge;
    private TextView tvSentinelStatus;
    private TextView tvSentinelDns;
    private Button btnToggleSentinel;

    // Hero Live Screen Time
    private TextView tvLiveClockSeconds;
    private TextView tvTotalScreenTime;
    private ProgressBar progressDailyLimit;
    private TextView tvDailyLimitText;
    private TextView tvRemainingTime;
    private LineChart lineChartScreenTime;

    // 4D Hardware Reports
    private TextView tvReportDateTitle;
    private Button btnDateToday;
    private Button btnDateYesterday;
    private Button btnDateWeekly;
    private Button btnMetricTime;
    private Button btnMetricStorage;
    private Button btnMetricRam;
    private Button btnMetricBattery;
    private PieChart pieChartCategoryDistribution;
    private LinearLayout layoutHardwareMetricsList;
    private TextView tvEmptyMetricsState;

    private HardwareUsageReporter.DateRange currentDateRange = HardwareUsageReporter.DateRange.TODAY;
    private enum MetricView { TIME, STORAGE, RAM, BATTERY }
    private MetricView currentMetricView = MetricView.TIME;

    // Financial Protection & Anti-Bill Shock
    private TextView tvFinancialLevelBadge;
    private TextView tvFinancialDesc;
    private Button btnConfigureFinancial;

    // Kids App Guide & Risk Scanner
    private Button btnViewCuratedApps;
    private Button btnScanAppRisks;
    private LinearLayout layoutAppGuideContainer;

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
        financialManager = new FinancialProtectionManager(this);
        usageReporter = new HardwareUsageReporter(this);
        appRecManager = new AppRecommendationManager();

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvRoleBadge = findViewById(R.id.tvRoleBadge);
        tvLivePulseRadar = findViewById(R.id.tvLivePulseRadar);
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

        tvSentinelGateBadge = findViewById(R.id.tvSentinelGateBadge);
        tvSentinelStatus = findViewById(R.id.tvSentinelStatus);
        tvSentinelDns = findViewById(R.id.tvSentinelDns);
        btnToggleSentinel = findViewById(R.id.btnToggleSentinel);

        tvLiveClockSeconds = findViewById(R.id.tvLiveClockSeconds);
        tvTotalScreenTime = findViewById(R.id.tvTotalScreenTime);
        progressDailyLimit = findViewById(R.id.progressDailyLimit);
        tvDailyLimitText = findViewById(R.id.tvDailyLimitText);
        tvRemainingTime = findViewById(R.id.tvRemainingTime);
        lineChartScreenTime = findViewById(R.id.lineChartScreenTime);

        tvReportDateTitle = findViewById(R.id.tvReportDateTitle);
        btnDateToday = findViewById(R.id.btnDateToday);
        btnDateYesterday = findViewById(R.id.btnDateYesterday);
        btnDateWeekly = findViewById(R.id.btnDateWeekly);
        btnMetricTime = findViewById(R.id.btnMetricTime);
        btnMetricStorage = findViewById(R.id.btnMetricStorage);
        btnMetricRam = findViewById(R.id.btnMetricRam);
        btnMetricBattery = findViewById(R.id.btnMetricBattery);
        pieChartCategoryDistribution = findViewById(R.id.pieChartCategoryDistribution);
        layoutHardwareMetricsList = findViewById(R.id.layoutHardwareMetricsList);
        tvEmptyMetricsState = findViewById(R.id.tvEmptyMetricsState);

        CyberChartHelper.setupNeonLineChart(lineChartScreenTime);
        CyberChartHelper.setupNeonDonutChart(pieChartCategoryDistribution, "Usage Split\nToday");

        tvFinancialLevelBadge = findViewById(R.id.tvFinancialLevelBadge);
        tvFinancialDesc = findViewById(R.id.tvFinancialDesc);
        btnConfigureFinancial = findViewById(R.id.btnConfigureFinancial);

        btnViewCuratedApps = findViewById(R.id.btnViewCuratedApps);
        btnScanAppRisks = findViewById(R.id.btnScanAppRisks);
        layoutAppGuideContainer = findViewById(R.id.layoutAppGuideContainer);
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

        btnConfigureFinancial.setOnClickListener(v -> {
            if (roleManager.isChild()) {
                promptPinForFinancialConfig();
            } else {
                showFinancialLevelDialog();
            }
        });

        // Date Selectors
        btnDateToday.setOnClickListener(v -> {
            currentDateRange = HardwareUsageReporter.DateRange.TODAY;
            highlightDateButton(btnDateToday);
            refreshHardwareReports();
        });

        btnDateYesterday.setOnClickListener(v -> {
            currentDateRange = HardwareUsageReporter.DateRange.YESTERDAY;
            highlightDateButton(btnDateYesterday);
            refreshHardwareReports();
        });

        btnDateWeekly.setOnClickListener(v -> {
            currentDateRange = HardwareUsageReporter.DateRange.WEEKLY;
            highlightDateButton(btnDateWeekly);
            refreshHardwareReports();
        });

        // 4D Metric Selectors
        btnMetricTime.setOnClickListener(v -> {
            currentMetricView = MetricView.TIME;
            highlightMetricButton(btnMetricTime);
            refreshHardwareReports();
        });

        btnMetricStorage.setOnClickListener(v -> {
            currentMetricView = MetricView.STORAGE;
            highlightMetricButton(btnMetricStorage);
            refreshHardwareReports();
        });

        btnMetricRam.setOnClickListener(v -> {
            currentMetricView = MetricView.RAM;
            highlightMetricButton(btnMetricRam);
            refreshHardwareReports();
        });

        btnMetricBattery.setOnClickListener(v -> {
            currentMetricView = MetricView.BATTERY;
            highlightMetricButton(btnMetricBattery);
            refreshHardwareReports();
        });

        btnViewCuratedApps.setOnClickListener(v -> displayCuratedKidsApps());
        btnScanAppRisks.setOnClickListener(v -> runInstalledAppsRiskScan());
    }

    private void highlightDateButton(Button active) {
        int normalColor = ContextCompat.getColor(this, R.color.surface_card_stroke);
        int activeColor = ContextCompat.getColor(this, R.color.accent_blue);

        btnDateToday.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnDateToday == active ? activeColor : normalColor));
        btnDateYesterday.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnDateYesterday == active ? activeColor : normalColor));
        btnDateWeekly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnDateWeekly == active ? activeColor : normalColor));

        btnDateToday.setTextColor(btnDateToday == active ? 0xFFFFFFFF : ContextCompat.getColor(this, R.color.text_primary));
        btnDateYesterday.setTextColor(btnDateYesterday == active ? 0xFFFFFFFF : ContextCompat.getColor(this, R.color.text_primary));
        btnDateWeekly.setTextColor(btnDateWeekly == active ? 0xFFFFFFFF : ContextCompat.getColor(this, R.color.text_primary));
    }

    private void highlightMetricButton(Button active) {
        int normalColor = ContextCompat.getColor(this, R.color.surface_card_stroke);
        int cyanColor = ContextCompat.getColor(this, R.color.neon_cyan);

        btnMetricTime.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnMetricTime == active ? cyanColor : normalColor));
        btnMetricStorage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnMetricStorage == active ? cyanColor : normalColor));
        btnMetricRam.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnMetricRam == active ? cyanColor : normalColor));
        btnMetricBattery.setBackgroundTintList(android.content.res.ColorStateList.valueOf(btnMetricBattery == active ? cyanColor : normalColor));

        btnMetricTime.setTextColor(btnMetricTime == active ? 0xFF000000 : ContextCompat.getColor(this, R.color.text_primary));
        btnMetricStorage.setTextColor(btnMetricStorage == active ? 0xFF000000 : ContextCompat.getColor(this, R.color.text_primary));
        btnMetricRam.setTextColor(btnMetricRam == active ? 0xFF000000 : ContextCompat.getColor(this, R.color.text_primary));
        btnMetricBattery.setTextColor(btnMetricBattery == active ? 0xFF000000 : ContextCompat.getColor(this, R.color.text_primary));
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyRoleState();
        refreshSentinelUI();
        refreshFinancialUI();
        refreshHardwareReports();
        tvHealthDiagnostics.setText(healthManager.getHealthDiagnostics());
        checkEnforcementRules();
    }

    private void refreshSentinelUI() {
        boolean active = sentinelManager.isFilterEnabled();
        if (active) {
            tvSentinelGateBadge.setText("PG-13 GUARD");
            tvSentinelGateBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_emerald));
            tvSentinelStatus.setText(R.string.sentinel_desc_active);
            btnToggleSentinel.setText(R.string.btn_toggle_sentinel_on);
            tvSentinelDns.setText(getString(R.string.sentinel_dns_format, sentinelManager.getActiveDnsProfile() + " (Family Safe)"));
        } else {
            tvSentinelGateBadge.setText("18+ OPEN");
            tvSentinelGateBadge.setTextColor(ContextCompat.getColor(this, R.color.neon_magenta));
            tvSentinelStatus.setText(R.string.sentinel_desc_inactive);
            btnToggleSentinel.setText(R.string.btn_toggle_sentinel_off);
            tvSentinelDns.setText(getString(R.string.sentinel_dns_format, sentinelManager.getActiveDnsProfile()));
        }
    }

    private void toggleSentinelFilter() {
        boolean newState = !sentinelManager.isFilterEnabled();
        sentinelManager.setFilterEnabled(newState);
        refreshSentinelUI();
        Toast.makeText(this, newState ? "Sentinel PG-13 Safe Filter Activated" : "18+ Gates Open (Content Unlocked)", Toast.LENGTH_SHORT).show();
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

    private void refreshFinancialUI() {
        FinancialProtectionManager.ProtectionLevel level = financialManager.getProtectionLevel();
        tvFinancialLevelBadge.setText(level.title);
        tvFinancialDesc.setText(level.description + " Blocked attempts: " + financialManager.getBlockedAttemptsCount());
    }

    private void promptPinForFinancialConfig() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pin_dialog_title);
        builder.setMessage(R.string.pin_dialog_prompt);

        final EditText pinInput = new EditText(this);
        pinInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(pinInput);

        builder.setPositiveButton(R.string.pin_confirm, (dialog, which) -> {
            String enteredPin = pinInput.getText().toString().trim();
            if (roleManager.verifyPin(enteredPin)) {
                showFinancialLevelDialog();
            } else {
                Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.pin_cancel, null);
        builder.show();
    }

    private void showFinancialLevelDialog() {
        FinancialProtectionManager.ProtectionLevel[] levels = FinancialProtectionManager.ProtectionLevel.values();
        String[] titles = new String[levels.length];
        for (int i = 0; i < levels.length; i++) {
            titles[i] = levels[i].title + "\n" + levels[i].description;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Financial Protection & Anti-Bill Shock");
        builder.setItems(titles, (dialog, which) -> {
            financialManager.setProtectionLevel(levels[which]);
            refreshFinancialUI();
            Toast.makeText(this, "Protection Level Updated: " + levels[which].title, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
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
                    refreshHardwareReports();
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
            refreshHardwareReports();
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

        if (bedtimeManager.isBedtimeActiveNow()) {
            Intent lockIntent = new Intent(this, LockOverlayActivity.class);
            lockIntent.putExtra(LockOverlayActivity.EXTRA_REASON, "BEDTIME");
            startActivity(lockIntent);
            return;
        }

        if (roleManager.isInstantPauseActive()) {
            Intent lockIntent = new Intent(this, LockOverlayActivity.class);
            lockIntent.putExtra(LockOverlayActivity.EXTRA_REASON, "INSTANT_PAUSE");
            startActivity(lockIntent);
        }
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

    private void refreshHardwareReports() {
        if (!hasUsageStatsPermission()) {
            cardPermissionWarning.setVisibility(View.VISIBLE);
            tvTotalScreenTime.setText("00h 00m 00s");
            progressDailyLimit.setProgress(0);
            return;
        }

        cardPermissionWarning.setVisibility(View.GONE);

        HardwareUsageReporter.AggregatedReport report = usageReporter.generateReport(currentDateRange);
        tvReportDateTitle.setText(report.formattedDate);
        tvTotalScreenTime.setText(report.totalHms);
        tvLiveClockSeconds.setText("Live Sync");

        int baseLimit = roleManager.getDailyLimitMinutes();
        int bonus = rewardManager.getBonusMinutes();
        int totalLimitMins = baseLimit + bonus;
        int totalUsedMins = (int) (report.totalForegroundMs / (1000 * 60));

        tvDailyLimitText.setText(getString(R.string.daily_limit_format, ScreenTimeHelper.formatDuration(totalLimitMins)));
        int progress = ScreenTimeHelper.calculateProgress(totalUsedMins, totalLimitMins);
        progressDailyLimit.setProgress(progress);

        int remainingMins = ScreenTimeHelper.calculateRemaining(totalUsedMins, totalLimitMins);
        tvRemainingTime.setText(getString(R.string.remaining_time_format, ScreenTimeHelper.formatDuration(remainingMins)));

        if (roleManager.isChild() && remainingMins <= 0 && totalLimitMins > 0) {
            Intent lockIntent = new Intent(this, LockOverlayActivity.class);
            lockIntent.putExtra(LockOverlayActivity.EXTRA_REASON, "LIMIT_EXCEEDED");
            startActivity(lockIntent);
        }

        // Update Cyberpunk Line & Donut Charts
        HardwareUsageReporter.TimelineChartData chartData = usageReporter.getTimelineChartData(currentDateRange, report.totalForegroundMs);
        CyberChartHelper.updateLineChartData(lineChartScreenTime, chartData.values, chartData.labels);

        pieChartCategoryDistribution.setCenterText("Usage Split\n" + currentDateRange.label);
        java.util.Map<String, Float> categoryMap = HardwareUsageReporter.getCategoryDistribution(report.topApps);
        CyberChartHelper.updateDonutChartData(pieChartCategoryDistribution, categoryMap);

        renderMetricsList(report.topApps);
    }

    private void renderMetricsList(List<HardwareUsageReporter.AppMetricItem> apps) {
        layoutHardwareMetricsList.removeAllViews();

        if (apps == null || apps.isEmpty()) {
            layoutHardwareMetricsList.addView(tvEmptyMetricsState);
            tvEmptyMetricsState.setVisibility(View.VISIBLE);
            return;
        }

        for (HardwareUsageReporter.AppMetricItem item : apps) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 10, 0, 10);

            TextView nameView = new TextView(this);
            nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f));
            nameView.setText(item.appName);
            nameView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            nameView.setTextSize(13f);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView valueView = new TextView(this);
            valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            valueView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            valueView.setTextSize(12f);

            if (currentMetricView == MetricView.TIME) {
                valueView.setText(item.formattedTimeHms);
                valueView.setTextColor(ContextCompat.getColor(this, R.color.neon_cyan));
            } else if (currentMetricView == MetricView.STORAGE) {
                valueView.setText(String.format(java.util.Locale.US, "%.1f MB", item.storageMb));
                valueView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            } else if (currentMetricView == MetricView.RAM) {
                valueView.setText(String.format(java.util.Locale.US, "%.0f MB RAM", item.ramMb));
                valueView.setTextColor(ContextCompat.getColor(this, R.color.neon_emerald));
            } else if (currentMetricView == MetricView.BATTERY) {
                valueView.setText(String.format(java.util.Locale.US, "%.1f%% battery", item.batteryPercent));
                valueView.setTextColor(ContextCompat.getColor(this, R.color.accent_warning));
            }

            row.addView(nameView);
            row.addView(valueView);
            layoutHardwareMetricsList.addView(row);
        }
    }

    private void displayCuratedKidsApps() {
        layoutAppGuideContainer.removeAllViews();
        List<AppRecommendationManager.RecommendedApp> curated = appRecManager.getCuratedApps();

        for (AppRecommendationManager.RecommendedApp app : curated) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(0, 8, 0, 8);

            TextView title = new TextView(this);
            title.setText("⭐ " + app.name + " (" + app.ageSuitability + ")");
            title.setTextColor(ContextCompat.getColor(this, R.color.neon_emerald));
            title.setTextSize(13f);
            title.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView desc = new TextView(this);
            desc.setText(app.category + " — " + app.benefits);
            desc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            desc.setTextSize(11f);

            itemLayout.addView(title);
            itemLayout.addView(desc);
            layoutAppGuideContainer.addView(itemLayout);
        }
    }

    private void runInstalledAppsRiskScan() {
        layoutAppGuideContainer.removeAllViews();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<AppRecommendationManager.AppRiskAssessment> flagged = new ArrayList<>();
        for (ApplicationInfo info : installed) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue; // skip system apps
            String label = pm.getApplicationLabel(info).toString();
            AppRecommendationManager.AppRiskAssessment assessment = appRecManager.assessAppRisk(info.packageName, label);
            if (assessment.riskLevel != AppRecommendationManager.RiskLevel.SAFE) {
                flagged.add(assessment);
            }
        }

        if (flagged.isEmpty()) {
            TextView clean = new TextView(this);
            clean.setText("🛡️ Zero predatory loot-box or high-risk apps detected on this device!");
            clean.setTextColor(ContextCompat.getColor(this, R.color.neon_emerald));
            clean.setTextSize(12f);
            clean.setPadding(0, 10, 0, 10);
            layoutAppGuideContainer.addView(clean);
            return;
        }

        for (AppRecommendationManager.AppRiskAssessment item : flagged) {
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(0, 8, 0, 8);

            TextView title = new TextView(this);
            title.setText("⚠️ " + item.appName + " — " + item.riskLevel.label);
            title.setTextColor(item.riskLevel.colorHex);
            title.setTextSize(13f);
            title.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView reason = new TextView(this);
            reason.setText(item.reason);
            reason.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            reason.setTextSize(11f);

            itemLayout.addView(title);
            itemLayout.addView(reason);
            layoutAppGuideContainer.addView(itemLayout);
        }
    }
}
