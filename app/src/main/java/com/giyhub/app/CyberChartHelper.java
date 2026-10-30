package com.giyhub.app;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Styling and configuration engine for MPAndroidChart.
 * Renders high-performance neon line graphs and donut charts inspired by crypto and cyberpunk dashboards.
 */
public class CyberChartHelper {

    public static final int COLOR_NEON_CYAN = Color.parseColor("#00F0FF");
    public static final int COLOR_NEON_EMERALD = Color.parseColor("#00FFA3");
    public static final int COLOR_CYBER_PURPLE = Color.parseColor("#818CF8");
    public static final int COLOR_AMBER = Color.parseColor("#F59E0B");
    public static final int COLOR_CRIMSON = Color.parseColor("#EF4444");
    public static final int COLOR_GRID = Color.parseColor("#1E293B");
    public static final int COLOR_TEXT_MUTED = Color.parseColor("#94A3B8");
    public static final int COLOR_DONUT_HOLE = Color.parseColor("#111827");

    /**
     * Styles a LineChart with cyberpunk dark theme and cubic bezier neon curves.
     */
    public static void setupNeonLineChart(LineChart chart) {
        if (chart == null) return;

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);

        // X-Axis (Timeline)
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(COLOR_GRID);
        xAxis.setGridLineWidth(0.8f);
        xAxis.setTextColor(COLOR_TEXT_MUTED);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);

        // Left Y-Axis (Minutes)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(COLOR_GRID);
        leftAxis.setGridLineWidth(0.8f);
        leftAxis.setTextColor(COLOR_TEXT_MUTED);
        leftAxis.setTextSize(10f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f);

        // Right Y-Axis (Hidden)
        chart.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }

    /**
     * Populates LineChart with hourly screen time usage points and smooth cubic transitions.
     */
    public static void updateLineChartData(LineChart chart, List<Float> values, List<String> timeLabels) {
        if (chart == null || values == null || values.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Screen Time");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setColor(COLOR_NEON_CYAN);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(COLOR_NEON_CYAN);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleColor(COLOR_DONUT_HOLE);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(COLOR_NEON_EMERALD);

        // Draw soft glowing fill below the curve
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(COLOR_NEON_CYAN);
        dataSet.setFillAlpha(35);

        if (timeLabels != null && !timeLabels.isEmpty()) {
            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(timeLabels));
        }

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
        chart.animateX(700, Easing.EaseInOutQuad);
    }

    /**
     * Styles a PieChart as a glowing cyberpunk donut chart.
     */
    public static void setupNeonDonutChart(PieChart chart, String centerTitle) {
        if (chart == null) return;

        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(true);
        chart.setExtraOffsets(5, 5, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setBackgroundColor(Color.TRANSPARENT);

        // Donut Hole
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(COLOR_DONUT_HOLE);
        chart.setTransparentCircleColor(COLOR_DONUT_HOLE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(56f);
        chart.setTransparentCircleRadius(60f);

        // Center Title
        chart.setDrawCenterText(true);
        chart.setCenterText(centerTitle != null ? centerTitle : "App Usage");
        chart.setCenterTextColor(Color.WHITE);
        chart.setCenterTextSize(12f);

        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(COLOR_TEXT_MUTED);
        legend.setTextSize(10f);
        legend.setForm(Legend.LegendForm.CIRCLE);
    }

    /**
     * Populates PieChart with category distribution slices.
     */
    public static void updateDonutChartData(PieChart chart, Map<String, Float> categoryMap) {
        if (chart == null || categoryMap == null || categoryMap.isEmpty()) return;

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int colorIdx = 0;
        int[] palette = new int[]{
                COLOR_NEON_EMERALD,
                COLOR_NEON_CYAN,
                COLOR_CYBER_PURPLE,
                COLOR_AMBER,
                COLOR_CRIMSON
        };

        for (Map.Entry<String, Float> entry : categoryMap.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                colors.add(palette[colorIdx % palette.length]);
                colorIdx++;
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(chart));

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate();
        chart.animateY(800, Easing.EaseInOutQuad);
    }
}
