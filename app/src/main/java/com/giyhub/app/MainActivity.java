package com.giyhub.app;

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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_DAILY_LIMIT_MINUTES = 120; // 2 hours

    private CardView cardPermissionWarning;
    private Button btnGrantPermission;
    private TextView tvTotalScreenTime;
    private ProgressBar progressDailyLimit;
    private TextView tvRemainingTime;
    private LinearLayout layoutTopAppsList;
    private TextView tvEmptyAppsState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardPermissionWarning = findViewById(R.id.cardPermissionWarning);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
        tvTotalScreenTime = findViewById(R.id.tvTotalScreenTime);
        progressDailyLimit = findViewById(R.id.progressDailyLimit);
        tvRemainingTime = findViewById(R.id.tvRemainingTime);
        layoutTopAppsList = findViewById(R.id.layoutTopAppsList);
        tvEmptyAppsState = findViewById(R.id.tvEmptyAppsState);

        btnGrantPermission.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasUsageStatsPermission()) {
            cardPermissionWarning.setVisibility(View.GONE);
            loadScreenTimeData();
        } else {
            cardPermissionWarning.setVisibility(View.VISIBLE);
            tvTotalScreenTime.setText("0h 00m");
            progressDailyLimit.setProgress(0);
            tvRemainingTime.setText(getString(R.string.remaining_time_label, "2h 00m"));
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) {
            return false;
        }
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

    private void loadScreenTimeData() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return;
        }

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

        if (stats == null || stats.isEmpty()) {
            tvTotalScreenTime.setText("0h 00m");
            progressDailyLimit.setProgress(0);
            tvRemainingTime.setText(getString(R.string.remaining_time_label, "2h 00m"));
            tvEmptyAppsState.setVisibility(View.VISIBLE);
            return;
        }

        Map<String, Long> usageMap = new HashMap<>();
        long totalMillis = 0;

        for (UsageStats stat : stats) {
            long foregroundTime = stat.getTotalTimeInForeground();
            if (foregroundTime > 60_000L) { // Only track apps used for > 1 min
                usageMap.put(stat.getPackageName(), foregroundTime);
                totalMillis += foregroundTime;
            }
        }

        int totalMinutes = (int) (totalMillis / (1000 * 60));
        tvTotalScreenTime.setText(ScreenTimeHelper.formatDuration(totalMinutes));

        int progress = ScreenTimeHelper.calculateProgress(totalMinutes, DEFAULT_DAILY_LIMIT_MINUTES);
        progressDailyLimit.setProgress(progress);

        int remainingMinutes = ScreenTimeHelper.calculateRemaining(totalMinutes, DEFAULT_DAILY_LIMIT_MINUTES);
        tvRemainingTime.setText(getString(
                R.string.remaining_time_label,
                ScreenTimeHelper.formatDuration(remainingMinutes)
        ));

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
