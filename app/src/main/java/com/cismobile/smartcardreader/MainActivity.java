package com.cismobile.smartcardreader;

import android.app.Activity;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.securespaces.android.securemode.ResponseCode;
import com.securespaces.android.securemode.ResponseStatusInfo;
import com.securespaces.android.ssm.SpacesManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class MainActivity extends AppCompatActivity implements ReaderCallback {
   private static final String TAG = "SmartCardReader";

   // AID for our loyalty card service.
   private static final String AID = "A0000002471001";

   // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
   // activity is interested in NFC-A devices (including other Android devices), and that the
   // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
   public static int READER_FLAGS =
           NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;

   private NfcAdapter nfcAdapter;
   private SpacesManager spacesManager;

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.setContentView(R.layout.activity_main);
      this.nfcAdapter = NfcAdapter.getDefaultAdapter((Context)this);
      this.spacesManager = new SpacesManager((Context)this);
   }

   public void onResume() {
      super.onResume();
      NfcAdapter nfc = this.nfcAdapter;
      if (nfc != null) {
      nfc.enableReaderMode((Activity)this, (ReaderCallback)this, READER_FLAGS, (Bundle)null);
      }
   }

   public void onPause() {
      super.onPause();
      NfcAdapter nfc = this.nfcAdapter;
      if (nfc != null) {
         nfc.disableReaderMode((Activity)this);
      }
   }

   /**
    * Callback when a new tag is discovered by the system.
    *
    * <p>Communication with the card should take place here.
    *
    * @param tag Discovered tag
    */
   @Override
   public void onTagDiscovered(Tag tag) {
      IsoDep isoDep = IsoDep.get(tag);
      final StringBuilder responseText = new StringBuilder();
      responseText.append("Card Reader");
      try {
         // Connect to the remote NFC device
         isoDep.connect();

         // Build and send the command
         final byte[] response = isoDep.transceive(Utils.BuildSelectApdu(AID));

         // If AID is successfully selected, 0x9000 is returned as the status word (last 2
         // bytes of the result) by convention. Everything before the status word is
         // optional payload, which is used here to hold the account number.
         int resultLength = response.length;
         byte[] statusWord = {response[resultLength-2], response[resultLength-1]};
         byte[] payload = Arrays.copyOf(response, resultLength-2);

         // If the response code is OK, display the code and the message (payload)
         if (Arrays.equals(Utils.SELECT_OK_SW, statusWord)) {
            String payloadString = new String(payload, "UTF-8");
            responseText.append("\nCard Response: " + Utils.ByteArrayToHexString(statusWord));
            responseText.append("\nCard Value: " + payloadString);
            // Decrypt the message
            try {
//               ResponseCode encryptedResponse = convertResponse(new String(payload, "UTF-8"));
//               ResponseStatus responseStatus = new ResponseStatus();
               ResponseStatusInfo responseInfo = getResponseInfo(payloadString);
//               responseText.append("\nCard Status: " + message);
               if (responseInfo.errorType.toLowerCase().equals("success")) {
                  responseText.append("\nCard Status: success");
               } else {
                  responseText.append("\nCard Status: failed");
               }
            } catch (NullPointerException e1) {
               Log.e(TAG, "Error decrypting data: " + e1.toString());
               responseText.append("\nCard decryption failed");
            }
         }
         // Close the NFC connection
         isoDep.close();
      } catch (IOException e) {
         Log.e(TAG, "Error communicating with card: " + e.toString());
//      } catch (NullPointerException e1) {
//         Log.e(TAG, "Error decrypting data: " + e1.toString());
      }
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            // Stuff that updates the UI and shows the response code
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(responseText); //set text for text view
         }
      });
   }

   private ResponseStatusInfo getResponseInfo(String jsonStr) {
      Gson gson = new Gson();
      Type typeResponseCode = new TypeToken<ResponseCode>() {}.getType();
      ResponseCode encryptedResponse = gson.fromJson(jsonStr, typeResponseCode);
      String certPem = encryptedResponse.getCertificate();
      Log.d(TAG, "certPem: " + certPem);
      String signature = encryptedResponse.getSignature();
      Log.d(TAG, "signature: " + signature);
      String signingMessage = encryptedResponse.getSigningMessage();
      Log.d(TAG, "signingMessage: " + signingMessage);
      String message = spacesManager.verifyMessage(signingMessage, signature, certPem);
      ResponseStatusInfo /*<?>*/ responseStatusInfo = gson.fromJson(message, ResponseStatusInfo.class);
      return responseStatusInfo; //gson.fromJson<ResponseStatusInfo>(message, ResponseStatusInfo.class);
   }
}
