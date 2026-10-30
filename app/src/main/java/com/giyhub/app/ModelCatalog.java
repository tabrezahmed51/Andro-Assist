package com.giyhub.app;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModelCatalog {

    private static final String PREFS_NAME = "andro_assist_models";
    private static final String KEY_LOADED_MODEL_ID = "loaded_model_id";
    private static final String KEY_DOWNLOAD_ID_PREFIX = "download_id_";

    public enum DownloadStatus {
        NOT_DOWNLOADED,
        DOWNLOADING,
        PAUSED,
        DOWNLOADED,
        LOADED
    }

    public static class ModelItem {
        public String id;
        public String name;
        public int sizeMb;
        public double minRamGb;
        public String hfUrl;
        public String fileName;
        public String description;

        public ModelItem(String id, String name, int sizeMb, double minRamGb, String hfUrl, String fileName, String description) {
            this.id = id;
            this.name = name;
            this.sizeMb = sizeMb;
            this.minRamGb = minRamGb;
            this.hfUrl = hfUrl;
            this.fileName = fileName;
            this.description = description;
        }
    }

    private final Context context;
    private final SharedPreferences prefs;

    public ModelCatalog(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static List<ModelItem> getAvailableModels() {
        List<ModelItem> list = new ArrayList<>();
        list.add(new ModelItem(
                "qwen-0.5b",
                "Qwen 2.5 0.5B Instruct (Q4_K_M)",
                398,
                1.5,
                "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
                "qwen2.5-0.5b-instruct-q4_k_m.gguf",
                "Recommended for Budget Devices (< 4GB RAM). Blazing fast edge agent."
        ));
        list.add(new ModelItem(
                "smollm-135m",
                "SmolLM2 135M Instruct (Q4_K_M)",
                102,
                1.0,
                "https://huggingface.co/HuggingFaceTB/SmolLM2-135M-Instruct-GGUF/resolve/main/smollm2-135m-instruct-q4_k_m.gguf",
                "smollm2-135m-instruct-q4_k_m.gguf",
                "Ultra-lightweight micro agent for low-battery and low-memory environments."
        ));
        list.add(new ModelItem(
                "smollm-1.7b",
                "SmolLM2 1.7B Instruct (Q4_K_M)",
                1050,
                4.0,
                "https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF/resolve/main/smollm2-1.7b-instruct-q4_k_m.gguf",
                "smollm2-1.7b-instruct-q4_k_m.gguf",
                "Recommended for Mid-Range (4-6GB RAM). High reasoning & tool accuracy."
        ));
        list.add(new ModelItem(
                "llama-3.2-3b",
                "Llama 3.2 3B Instruct (Q4_K_M)",
                2020,
                8.0,
                "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
                "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
                "Flagship Agentic AI for High-End Devices (8GB+ RAM). Advanced multi-turn dialogues."
        ));
        return list;
    }

    public File getModelFile(ModelItem model) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            dir = context.getFilesDir();
        }
        return new File(dir, model.fileName);
    }

    public DownloadStatus getStatus(ModelItem model) {
        String loadedId = prefs.getString(KEY_LOADED_MODEL_ID, null);
        if (model.id.equals(loadedId) && isDownloaded(model)) {
            return DownloadStatus.LOADED;
        }

        if (isDownloaded(model)) {
            return DownloadStatus.DOWNLOADED;
        }

        long downloadId = prefs.getLong(KEY_DOWNLOAD_ID_PREFIX + model.id, -1);
        if (downloadId != -1) {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
                try (Cursor c = dm.query(query)) {
                    if (c != null && c.moveToFirst()) {
                        int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                            return DownloadStatus.DOWNLOADING;
                        } else if (status == DownloadManager.STATUS_PAUSED) {
                            return DownloadStatus.PAUSED;
                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            return DownloadStatus.DOWNLOADED;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return DownloadStatus.NOT_DOWNLOADED;
    }

    public boolean isDownloaded(ModelItem model) {
        File f = getModelFile(model);
        return f.exists() && f.length() > 0;
    }

    public long startDownload(ModelItem model) {
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) return -1;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(model.hfUrl))
                .setTitle("Downloading " + model.name)
                .setDescription("Hugging Face GGUF Model Download")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(getModelFile(model)))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false);

        long id = dm.enqueue(request);
        prefs.edit().putLong(KEY_DOWNLOAD_ID_PREFIX + model.id, id).apply();
        return id;
    }

    public void removeModel(ModelItem model) {
        long downloadId = prefs.getLong(KEY_DOWNLOAD_ID_PREFIX + model.id, -1);
        if (downloadId != -1) {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                try {
                    dm.remove(downloadId);
                } catch (Exception ignored) {}
            }
            prefs.edit().remove(KEY_DOWNLOAD_ID_PREFIX + model.id).apply();
        }

        File f = getModelFile(model);
        if (f.exists()) {
            f.delete();
        }

        if (model.id.equals(prefs.getString(KEY_LOADED_MODEL_ID, null))) {
            unloadModel();
        }
    }

    public void loadModel(ModelItem model) {
        if (isDownloaded(model)) {
            prefs.edit().putString(KEY_LOADED_MODEL_ID, model.id).apply();
        }
    }

    public void unloadModel() {
        prefs.edit().remove(KEY_LOADED_MODEL_ID).apply();
    }

    public String getLoadedModelId() {
        return prefs.getString(KEY_LOADED_MODEL_ID, null);
    }

    public ModelItem getLoadedModel() {
        String id = getLoadedModelId();
        if (id == null) return null;
        for (ModelItem item : getAvailableModels()) {
            if (item.id.equals(id)) return item;
        }
        return null;
    }
}
