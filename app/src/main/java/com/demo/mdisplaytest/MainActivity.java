package com.demo.mdisplaytest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.proglove.sdk.ConnectionStatus;
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

public class MainActivity extends AppCompatActivity implements IScannerOutput, IDisplayOutput, IButtonOutput {
    public static final String MENGE = "menge";
    public static final String PLTZ = "pltz";
    public static final String ORT = "ort";
    public static final String BEZ = "bez";
    public static final String ARTIKEL_NR = "artikelNr";
    public static final String AUFTRAG = "auftrag";
    public static final String ARTIKEL_NUMMER = "Artikel Nummer";
    public static final String BEZEICHNUNG = "Bezeichnung";
    public static final String LAGERORT = "Lagerort";
    public static final String LAGERPLATZ = "Lagerplatz";
    public static final String ART_MENGE = "Menge";
    public static final String FERTIG = "fertig";
    public static final String ZWEIMAL_DRÜCKEN_ZU_BEGINNEN = "Zweimal drücken zu beginnen";

    PgManager pm = new PgManager(); //Proglove Manager
    TextView txtArtikelNr, txtBezeichnung, txtOrt, txtMenge, txtPlatz, auftrag;
    ImageView imageView;
    boolean orderIsNull = true;
    PgScreenData screenData = null;
    String displayedContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //set App Orientation to Portrait

        Button pair_button = findViewById(R.id.connect);
        Button unpair_button = findViewById(R.id.disconnect);
        txtArtikelNr = findViewById(R.id.artNr);
        txtBezeichnung = findViewById(R.id.bez);
        txtOrt = findViewById(R.id.ort);
        txtMenge = findViewById(R.id.menge);
        txtPlatz = findViewById(R.id.pltz);
        auftrag = findViewById(R.id.result);
        boolean result = pm.ensureConnectionToService(this);


        pair_button.setOnClickListener(v -> {
            if (result) {
                try {
                    pm.startPairing();
                    pm.subscribeToScans(this);
                    pm.subscribeToDisplayEvents(this);
                    pm.subscribeToButtonPresses(this);
                } catch (Exception error) {
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
    }

    public void Dialog(String title, String message) {
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
        dialog_builder.setTitle(title);
        dialog_builder.setMessage(message);
        dialog_builder.setPositiveButton("OK", (dialog, which) -> finish());
        dialog_builder.show();
    }


    @Override
    protected void onDestroy() {
        pm.unsubscribeFromScans(this);
        pm.unsubscribeFromDisplayEvents(this);
        pm.unsubscribeFromButtonPresses(this);
        super.onDestroy();
    }

    //setting what to be displayed on Mark Display
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

    //Dummy Item to be picked up
    HashMap<String, String> artikel = new HashMap<String, String>() {{
        put(ORT, "LA007");
        put(PLTZ, "T21");
        put(ARTIKEL_NR, "1824869");
        put(MENGE, "1");
    }};

    HashMap<String, String> second_artikel = new HashMap<String, String>() {{
        put(ORT, "LA007");
        put(PLTZ, "T25");
        put(ARTIKEL_NR, "4869182");
        put(MENGE, "1");
    }};

    @Override
    public void onBarcodeScanned(@NotNull BarcodeScanResults barcodeScanResults) {
        displayedContent = screenData.component2().iterator().next().getContent();
        String scannedBarcode = barcodeScanResults.getBarcodeContent();

        PgTemplateField[] artikel_data = {
                new PgTemplateField(1, ARTIKEL_NUMMER, artikel.get(ARTIKEL_NR)),
                new PgTemplateField(2, MENGE, artikel.get(MENGE))
        };

        PgTemplateField[] first_order = {
                new PgTemplateField(1, LAGERORT, artikel.get(ORT)),
                new PgTemplateField(2, LAGERPLATZ, artikel.get(PLTZ)),
                new PgTemplateField(3, ARTIKEL_NUMMER, artikel.get(ARTIKEL_NR))
        };

        //If Barcode scanned is "auftrag" and Order is Null
        //Order details fill be filled in the App and at the same time "double click to start Picking" will be displayed on Mark Display
        if (scannedBarcode.equals(AUFTRAG) && orderIsNull) {
            orderIsNull = false;
            screenData = new PgScreenData("PG1A", Collections.singleton(new PgTemplateField(1, "", ZWEIMAL_DRÜCKEN_ZU_BEGINNEN)), RefreshType.FULL_REFRESH);
            sendScreen(screenData);
            auftrag.setText("Auftrag: Mustermann");
            txtOrt.setText(artikel.get(ORT));
            txtPlatz.setText(artikel.get(PLTZ));
            txtArtikelNr.setText(artikel.get(ARTIKEL_NR));
            txtBezeichnung.setText(artikel.get(BEZ));
            txtMenge.setText(artikel.get(MENGE));
            return; //Return is needed in order to prevent the rest of the Code to be run
        }

        //If condition is met, Order details on the App will be reset
        if (scannedBarcode.equals(FERTIG) && displayedContent.equals(artikel.get(MENGE))) {
            reset();
            return;
        }

        //If scanned Barcode equals to what's shown on Mark Display, Picking routine will be carried out (check ORT -> check PLATZ -> check Artikel Nummer)
        if (scannedBarcode.equals(displayedContent)) {
            if (scannedBarcode.equals(artikel.get(ORT))) {
                //show the Lagerplatz to User
                screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, LAGERPLATZ, artikel.get(PLTZ))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            }
            if (scannedBarcode.equals(artikel.get(PLTZ))) {
                //show the Artikelnummer and Menge to User
                screenData = new PgScreenData("PG2", artikel_data, RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            }
            if (scannedBarcode.equals(artikel.get(ARTIKEL_NR))) {
                //show the next Artikel
                if(scannedBarcode.equals(second_artikel.get(ARTIKEL_NR))) reset();
                else screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, LAGERPLATZ, artikel.get(PLTZ))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            }
        } else {
            // throw error Beep
            feedback(2);
        }
    }

    public void reset() {
        PgTemplateField[] complete = {
                new PgTemplateField(1, "", "Auftrag ist fertig"),
                new PgTemplateField(2, "", "Bitte neuen Auftrag scannen")
        };
        sendScreen(new PgScreenData("PG2C", complete, RefreshType.PARTIAL_REFRESH));
        auftrag.setText("");
        txtArtikelNr.setText("");
        txtBezeichnung.setText("");
        txtOrt.setText("");
        txtPlatz.setText("");
        txtMenge.setText("");
        displayedContent = "";
        orderIsNull = true;
        feedback(0);
    }

    @Override
    public void onDisplayConnected() {
        startPicking();
    }

    public void startPicking() {
        screenData = new PgScreenData("PG1A", Collections.singleton(new PgTemplateField(1, "", "Bitte Auftrag scannen")), RefreshType.PARTIAL_REFRESH);
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
            default:
                return PgPredefinedFeedback.SPECIAL_1;
        }
    }


    private void startTakingImage() {
        int quality = 20;
        int timeout = 50000;
        PgImageConfig imageConfig = new PgImageConfig(quality, ImageResolution.RESOLUTION_1280_960);

        pm.takeImage(imageConfig, timeout, new IPgImageCallback() {
            @Override
            public void onImageReceived(@NonNull final PgImage pgImage) {
                Bitmap bmp = BitmapFactory.decodeByteArray(pgImage.getBytes(), 0, pgImage.getBytes().length);
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                            View inflater = layoutInflater.inflate(R.layout.image, null);
                            builder.setView(inflater);
                            imageView = inflater.findViewById(R.id.imageTaken);
                            imageView.setImageBitmap(bmp);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    imageView.setImageResource(0);
                                }
                            });
                            builder.setNegativeButton("Take another Picture", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startTakingImage();
                                    dialog.dismiss();
                                    imageView.setImageResource(0);
                                }
                            });
                            builder.setCancelable(false);
                            AlertDialog imageDialog = builder.create();
                            imageDialog.show();
                            //todo tell User to check out the Image on the Device
                        }
                    });
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
            }

            @Override
            public void onError(@NonNull final PgError pgError) {
                final String msg = "Taking an image failed. Error code is: " + pgError;
            }
        });
    }

    @Override
    public void onButtonPressed(@NotNull ButtonPress buttonPress) {
        if (buttonPress.component1() == 1) {
            if (screenData.component2().iterator().next().getContent().equals(ZWEIMAL_DRÜCKEN_ZU_BEGINNEN) && !orderIsNull) {
                screenData = new PgScreenData("PG1", Collections.singleton(new PgTemplateField(1, LAGERORT, artikel.get(ORT))), RefreshType.PARTIAL_REFRESH);
                sendScreen(screenData);
                feedback(1);
            } else {
                runOnUiThread(() -> {
                    startTakingImage();
                });
            }
        }
    }
}