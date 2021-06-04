package com.demo.mdisplaytest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.proglove.sdk.ConnectionStatus;
import de.proglove.sdk.IServiceOutput;
import de.proglove.sdk.PgError;
import de.proglove.sdk.PgManager;
import de.proglove.sdk.button.ButtonPress;
import de.proglove.sdk.button.IButtonOutput;
import de.proglove.sdk.commands.PgCommand;
import de.proglove.sdk.display.IDisplayOutput;
import de.proglove.sdk.display.IPgSetScreenCallback;
import de.proglove.sdk.display.PgScreenData;
import de.proglove.sdk.display.PgTemplateField;
import de.proglove.sdk.display.RefreshType;
import de.proglove.sdk.scanner.BarcodeScanResults;
import de.proglove.sdk.scanner.IPgFeedbackCallback;
import de.proglove.sdk.scanner.IPgImageCallback;
import de.proglove.sdk.scanner.IScannerOutput;
import de.proglove.sdk.scanner.ImageResolution;
import de.proglove.sdk.scanner.PgImage;
import de.proglove.sdk.scanner.PgImageConfig;
import de.proglove.sdk.scanner.PgPredefinedFeedback;

public class MainActivity extends AppCompatActivity implements IScannerOutput, IServiceOutput, IDisplayOutput, IButtonOutput {

    PgManager pm = new PgManager();
    TextView txtArtikelNr, txtBezeichnung, txtOrt, txtMenge, txtPlatz, auftrag;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pair_button = findViewById(R.id.connect);
        Button unpair_button = findViewById(R.id.disconnect);
        Button fertig_button = findViewById(R.id.fertigBtn);
        txtArtikelNr = findViewById(R.id.artNr);
        txtBezeichnung = findViewById(R.id.bez);
        txtOrt = findViewById(R.id.ort);
        txtMenge = findViewById(R.id.menge);
        txtPlatz = findViewById(R.id.pltz);
        auftrag = findViewById(R.id.result);
        imageView = findViewById(R.id.image);
        boolean result = pm.ensureConnectionToService(this);

        pair_button.setOnClickListener(v -> {
            if(result) {
             try {
                 pm.startPairing();
                 pm.subscribeToScans(this);
                 pm.subscribeToServiceEvents(this);
                 pm.subscribeToDisplayEvents(this);
                 pm.subscribeToButtonPresses(this);
             }catch (Exception error){
                 Log.d("Error Message: ", error.getMessage());
                 Dialog("Error: ", error.getMessage());
             }
            } else {
                Dialog("Error: ", "Mark Display is not connected");
            }
        });

        unpair_button.setOnClickListener(v -> {
            pm.disconnectScanner();
            pm.disconnectDisplay();
        });

        fertig_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startTakingImage();
                return false;
            }
        });

        fertig_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auftrag.setText("");
                txtArtikelNr.setText("");
                txtBezeichnung.setText("");
                txtOrt.setText("");
                txtPlatz.setText("");
                txtMenge.setText("");
                displayedContent = "";
                startPicking();
                imageView.setImageResource(0);
                feedback(3);
            }
        });
    }

    public void Dialog(String title, String message){
        AlertDialog.Builder dialog_builder =  new AlertDialog.Builder(this);
        dialog_builder.setTitle(title);
        dialog_builder.setMessage(message);
        dialog_builder.setPositiveButton("OK", (dialog, which) -> finish());
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

    Map<String, String> artikel  = new HashMap<String, String>() {{
        put("artikelNr", "1824869");
        put("bez", "Baby Phone");
        put("ort", "LA007");
        put("pltz", "T21");
        put("menge", "5");
    }};

    PgScreenData screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, "Header", "Content")), RefreshType.PARTIAL_REFRESH);
    String displayedContent = "";
    @Override
    public void onBarcodeScanned(@NotNull BarcodeScanResults barcodeScanResults) {
        displayedContent = screenData.component2().iterator().next().getContent();
        String scannedBarcode = barcodeScanResults.getBarcodeContent();
        PgTemplateField[] data = {
                new PgTemplateField(1, "Artikel Nummer", artikel.get("artikelNr")),
                new PgTemplateField(2, "Bezeichnung", artikel.get("bez"))
        };

        switch (scannedBarcode){
            case "007":  screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, "Lagerort", artikel.get("ort"))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                auftrag.setText("Tom Delonge");
                txtArtikelNr.setText(artikel.get("artikelNr"));
                txtBezeichnung.setText(artikel.get("bez"));
                txtOrt.setText(artikel.get("ort"));
                txtPlatz.setText(artikel.get("pltz"));
                txtMenge.setText(artikel.get("menge"));
                break;

            case "LA007" : if(scannedBarcode.equals(displayedContent)){
                screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, "Lagerplatz", artikel.get("pltz"))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            } else {
                sendScreen(new PgScreenData("PG1E", Collections.singleton(new PgTemplateField(1, "Error", "Error")),RefreshType.PARTIAL_REFRESH));
                screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, "Lagerplatz", artikel.get("pltz"))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(2);
            }
                break;

            case "T21" : if(scannedBarcode.equals(displayedContent)){
                screenData = new PgScreenData("PG2", data, RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            } else {
                sendScreen(new PgScreenData("PG1E", Collections.singleton(new PgTemplateField(1, "Error", "Error")),RefreshType.PARTIAL_REFRESH));
                screenData = new PgScreenData("PG2", data, RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(2);
            }
                break;

            case "1824869": if(scannedBarcode.equals(displayedContent)){
                screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, "Menge", artikel.get("menge"))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            } else {
                sendScreen(new PgScreenData("PG1E", Collections.singleton(new PgTemplateField(1, "Error", "Error")),RefreshType.PARTIAL_REFRESH));
                screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, "Menge", artikel.get("menge"))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(2);
            }
                break;

            case "fertig":
                auftrag.setText("");
                txtArtikelNr.setText("");
                txtBezeichnung.setText("");
                txtOrt.setText("");
                txtPlatz.setText("");
                txtMenge.setText("");
                displayedContent = "";
                startPicking();
                imageView.setImageResource(0);
//                feedback(3);
                break;
        }
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onDisplayConnected() {
        startPicking();
    }

    public void startPicking(){
        PgTemplateField pgTemplateField = new PgTemplateField(1, "", "Scan Auftrag zu beginnen");
        PgScreenData screenData = new PgScreenData("PG1A", Collections.singleton(pgTemplateField), RefreshType.FULL_REFRESH);
        sendScreen(screenData);
    }

    @Override
    public void onDisplayDisconnected() {

    }

    @Override
    public void onDisplayStateChanged(@NotNull ConnectionStatus connectionStatus) {

    }

    @Override
    public void onScannerConnected() {
//        Intent toAuftrag = new Intent(getApplicationContext(), AuftragActivity.class);
//        startActivity(toAuftrag);
    }

    @Override
    public void onScannerDisconnected() {

    }

    @Override
    public void onScannerStateChanged(@NotNull ConnectionStatus connectionStatus) {

    }

    private void feedback(int feedbackID) {
        // Wrapping the feedback data in a PgCommand with the PgCommandData
        PgCommand<PgPredefinedFeedback> feedbackCommand = getSelectedFeedback(feedbackID).toCommand();
        pm.triggerFeedback(feedbackCommand, new IPgFeedbackCallback() {
            @Override
            public void onSuccess() {
//                Log.d(TAG, "Feedback successfully played.");
            }

            @Override
            public void onError(@NonNull PgError pgError) {
//                final String msg = "An Error occurred during triggerFeedback: " + pgError;
//                showMessage(msg, true);
            }
        });
    }

    private PgPredefinedFeedback getSelectedFeedback(int feebackID) {
        switch (feebackID) {
            case 1:
                return PgPredefinedFeedback.SUCCESS;
            case 2:
                return PgPredefinedFeedback.ERROR;
            case 3:
                return PgPredefinedFeedback.SPECIAL_1;
            default:
                return PgPredefinedFeedback.ERROR;
        }
    }

    private void startTakingImage() {
        int quality = 20;
        int timeout = 10000;
        PgImageConfig imageConfig = new PgImageConfig(quality, ImageResolution.RESOLUTION_1280_960);

        pm.takeImage(imageConfig, timeout, new IPgImageCallback() {
            @Override
            public void onImageReceived(@NonNull final PgImage pgImage) {
                final Bitmap bmp = BitmapFactory.decodeByteArray(pgImage.getBytes(), 0, pgImage.getBytes().length);
                runOnUiThread(() -> imageView.setImageBitmap(bmp));
            }

            @Override
            public void onError(@NonNull final PgError pgError) {
                final String msg = "Taking an image failed. Error code is: " + pgError;
//                showMessage(msg, true);
            }
        });

    }

    @Override
    public void onButtonPressed(@NotNull ButtonPress buttonPress) {
        if(buttonPress.component1() == 1) startTakingImage();
    }
}