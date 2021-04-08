package com.demo.mdisplaytest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import de.proglove.sdk.ConnectionStatus;
import de.proglove.sdk.IServiceOutput;
import de.proglove.sdk.PgError;
import de.proglove.sdk.PgManager;
import de.proglove.sdk.display.IDisplayOutput;
import de.proglove.sdk.display.IPgSetScreenCallback;
import de.proglove.sdk.display.PgScreenData;
import de.proglove.sdk.display.PgTemplateField;
import de.proglove.sdk.display.RefreshType;
import de.proglove.sdk.scanner.BarcodeScanResults;
import de.proglove.sdk.scanner.IScannerOutput;

public class MainActivity extends AppCompatActivity implements IScannerOutput, IServiceOutput, IDisplayOutput {

    PgManager pm = new PgManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pair_button = findViewById(R.id.connect);
        Button unpair_button = findViewById(R.id.disconnect);
        Boolean result = pm.ensureConnectionToService(this);

        pair_button.setOnClickListener(v -> {
            if(result) {
             try {
                 pm.startPairing();
                 pm.subscribeToScans(this);
                 pm.subscribeToServiceEvents(this);
                 pm.subscribeToDisplayEvents(this);
             }catch (Exception error){
                 Log.d("Error Message: ", error.getMessage());
                 Dialog("Error: ", error.getMessage());
             }
            } else {
                Dialog("Error: ", "Mark Display is not connected");
            }
        });

        unpair_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm.disconnectScanner();
                pm.disconnectDisplay();
            }
        });
    }

    public void Dialog(String title, String message){
        AlertDialog.Builder dialog_builder =  new AlertDialog.Builder(this);
        dialog_builder.setTitle(title);
        dialog_builder.setMessage(message);
        dialog_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog_builder.show();
    }

    @Override
    protected void onDestroy() {
        pm.unsubscribeFromScans(this);
        pm.unsubscribeFromDisplayEvents(this);
        pm.unsubscribeFromServiceEvents(this);
        super.onDestroy();
    }

    private void sendScreen(PgScreenData screenData) {
        if (pm.isConnectedToService() && pm.isConnectedToDisplay()) {
            pm.setScreen(screenData, new IPgSetScreenCallback() {
                @Override
                public void onSuccess() {
                    String msg = "Screen set successfully";
                    /*showMessage(msg, false);*/
                }

                @Override
                public void onError(@NonNull final PgError pgError) {
                    String msg = "Setting the screen failed. Error: " + pgError;
                    /*showMessage(msg, true);*/
                }
            });
        }
    }

    @Override
    public void onBarcodeScanned(@NotNull BarcodeScanResults barcodeScanResults) {
        TextView result = findViewById(R.id.result);
        result.setText(barcodeScanResults.getBarcodeContent());

        PgTemplateField[] data = {
                new PgTemplateField(1, barcodeScanResults.getSymbology(), barcodeScanResults.getBarcodeContent())
        };
        PgScreenData screenData = new PgScreenData("PG1", data, RefreshType.DEFAULT);
        sendScreen(screenData);
        }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onDisplayConnected() {

    }

    @Override
    public void onDisplayDisconnected() {

    }

    @Override
    public void onDisplayStateChanged(@NotNull ConnectionStatus connectionStatus) {

    }

    @Override
    public void onScannerConnected() {

    }

    @Override
    public void onScannerDisconnected() {

    }

    @Override
    public void onScannerStateChanged(@NotNull ConnectionStatus connectionStatus) {

    }
}