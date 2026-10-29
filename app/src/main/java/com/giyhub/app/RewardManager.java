package com.giyhub.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class RewardManager {

    private static final String PREF_NAME = "andro_assist_rewards";
    private static final String KEY_BONUS_MINUTES = "bonus_minutes";

    public static class ChoreTask {
        public String id;
        public String title;
        public int rewardMinutes;
        public boolean completed;

        public ChoreTask(String id, String title, int rewardMinutes, boolean completed) {
            this.id = id;
            this.title = title;
            this.rewardMinutes = rewardMinutes;
            this.completed = completed;
        }
    }

    private final SharedPreferences prefs;

    public RewardManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getBonusMinutes() {
        return prefs.getInt(KEY_BONUS_MINUTES, 0);
    }

    public void addBonusMinutes(int minutes) {
        int current = getBonusMinutes();
        prefs.edit().putInt(KEY_BONUS_MINUTES, current + Math.max(0, minutes)).apply();
    }

    public void resetDailyBonus() {
        prefs.edit().putInt(KEY_BONUS_MINUTES, 0).apply();
    }

    public List<ChoreTask> getDefaultTasks() {
        List<ChoreTask> tasks = new ArrayList<>();
        tasks.add(new ChoreTask("1", "Complete daily school homework", 30, false));
        tasks.add(new ChoreTask("2", "Tidy up bedroom & study desk", 15, false));
        tasks.add(new ChoreTask("3", "Read 20 pages of an educational book", 20, false));
        return tasks;
    }
}
