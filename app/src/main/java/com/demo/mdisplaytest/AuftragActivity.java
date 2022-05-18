package com.demo.mdisplaytest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.proglove.sdk.ConnectionStatus;
import de.proglove.sdk.PgManager;
import de.proglove.sdk.button.ButtonPress;
import de.proglove.sdk.button.IButtonOutput;
import de.proglove.sdk.scanner.BarcodeScanResults;
import de.proglove.sdk.scanner.IScannerOutput;

public class AuftragActivity extends AppCompatActivity implements IScannerOutput, IButtonOutput {

    PgManager pm = new PgManager();
    boolean isReady = pm.ensureConnectionToService(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auftrag);

        Button btnPair = findViewById(R.id.btnPair);
        btnPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isReady){
                    pm.startPairing();
                }
            }
        });
    }

    @Override
    public void onBarcodeScanned(@NonNull BarcodeScanResults barcodeScanResults) {
        String displayedContent = barcodeScanResults.getBarcodeContent();
        displayedContent += "TEST";
    }

    @Override
    public void onScannerConnected() {

    }

    @Override
    public void onScannerDisconnected() {

    }

    @Override
    public void onScannerStateChanged(@NonNull ConnectionStatus connectionStatus) {

    }

    @Override
    public void onButtonPressed(@NonNull ButtonPress buttonPress) {

    }
}