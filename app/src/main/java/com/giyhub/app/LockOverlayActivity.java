package com.giyhub.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LockOverlayActivity extends AppCompatActivity {

    public static final String EXTRA_REASON = "extra_lock_reason";

    private RoleManager roleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_overlay);

        roleManager = new RoleManager(this);

        TextView tvLockTitle = findViewById(R.id.tvLockTitle);
        TextView tvLockMessage = findViewById(R.id.tvLockMessage);
        Button btnAskMoreTime = findViewById(R.id.btnAskMoreTime);
        Button btnEmergencySos = findViewById(R.id.btnEmergencySos);
        Button btnParentPinUnlock = findViewById(R.id.btnParentPinUnlock);

        String reason = getIntent().getStringExtra(EXTRA_REASON);
        if ("BEDTIME".equalsIgnoreCase(reason)) {
            tvLockTitle.setText("Bedtime Mode Active");
            tvLockMessage.setText("Devices rest during sleep hours. See you tomorrow morning!");
        }

        btnAskMoreTime.setOnClickListener(v -> {
            Toast.makeText(this, "Extension request sent to Parent device (+15m)", Toast.LENGTH_LONG).show();
            btnAskMoreTime.setEnabled(false);
            btnAskMoreTime.setText("Request Sent");
        });

        // Hardcoded Emergency SOS Bypass
        btnEmergencySos.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));
            startActivity(dialIntent);
        });

        btnParentPinUnlock.setOnClickListener(v -> promptParentPin());
    }

    private void promptParentPin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pin_dialog_title);
        builder.setMessage(R.string.pin_dialog_prompt);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(R.string.pin_confirm, (dialog, which) -> {
            String enteredPin = input.getText().toString().trim();
            if (roleManager.verifyPin(enteredPin)) {
                Toast.makeText(this, "Parent override granted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.pin_cancel, null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        // Prevent bypassing lock overlay with Back button
    }
}
