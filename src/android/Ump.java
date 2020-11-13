package org.apache.cordova.ump;

import org.apache.cordova.*;
import com.google.ads.consent.*;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Ump extends CordovaPlugin {
    private CallbackContext callbackContext;
    protected static Context applicationContext = null;
    private static Activity cordovaActivity = null;
    private static ConsentForm myForm = null;
    private static String lastPrivacyString = "";
    private static String lastPubId = "";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView){
        super.initialize(cordova, webView);
        cordovaActivity = this.cordova.getActivity();
        applicationContext = webView.getContext();

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if ("verifyConsent".equals(action)) {
            String pubId = args.getString(0);
            String privacyUrl = args.getString(1);
            this.verifyConsent(pubId, privacyUrl, callbackContext);
            return true;
        }
        else if ("forceForm".equals(action)){
            forceForm(callbackContext);
        }
        return false;  // Returning false results in a "MethodNotFound" error.
    }

    private void forceForm(CallbackContext callbackContext){
        if (lastPrivacyString != "") {
            String[] publisherIds = {lastPubId};
            ConsentInformation consentInformation = ConsentInformation.getInstance(applicationContext);
            consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
                @Override
                public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                    // User's consent status successfully updated.
                    myForm = null;
                    createConsentForm(lastPrivacyString);
                    myForm.load();
                    callbackContext.success();
                }

                @Override
                public void onFailedToUpdateConsentInfo(String errorDescription) {
                    // User's consent status failed to update.
                }
            });
        }
        else {
            callbackContext.error("You must initialize the form first by using verifyConsent");
        }
    }

    private void verifyConsent(String pubId, String privacyString, CallbackContext callbackContext){
        lastPrivacyString = privacyString;
        ConsentInformation consentInformation = ConsentInformation.getInstance(applicationContext);
        String[] publisherIds = {pubId};
        lastPubId = pubId;
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                 // User's consent status successfully updated.
                if (myForm == null){
                    if (consentStatus.name() == "UNKNOWN") {
                        createConsentForm(privacyString);
                        myForm.load();
                    }

                }
                if (consentStatus.name() == "NON_PERSONALIZED") {
                    Bundle extras = new Bundle();
                    extras.putString("npa", "1");

                    AdRequest request = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, "consentStatus is " + consentStatus.toString());

                result.setKeepCallback(false);
                callbackContext.success("consentStatus is: " + consentStatus.toString());
                //callbackContext.sendPluginResult(result);
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                 // User's consent status failed to update.
            }
        });


    }

    private void createConsentForm (String privacyString){
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL(privacyString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }

        myForm = new ConsentForm.Builder(applicationContext, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        myForm.show();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        // Consent form was closed.
                        ConsentInformation.getInstance(applicationContext).setConsentStatus(consentStatus);
                        if (consentStatus.name() == "NON_PERSONALIZED"){
                            Bundle extras = new Bundle();
                            extras.putString("npa", "1");

                            AdRequest request = new AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                    .build();

                            //tell google ads to serve non personalized ads
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        // Consent form error.
                        int kkk = 0;
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build();
    }
}