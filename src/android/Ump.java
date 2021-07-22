package com.jptrainor.plugins.ump;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;

public class Ump extends CordovaPlugin {

    static enum Consent {
        OBTAINED(true), NOT_OBTAINED(false);
        private final boolean value;

        private Consent(boolean value) {
            this.value = value;
        }

        boolean getValue() {
            return this.value;
        }
    }

    static enum HasShownDialog {
        SHOWN(true), NOT_SHOWN(false);
        private final boolean value;

        private HasShownDialog(boolean value) {
            this.value = value;
        }

        boolean getValue() {
            return this.value;
        }
    }

    static enum FormAvailable {
        AVAILABLE(true), NOT_AVAILABLE(false);
        private final boolean value;

        private FormAvailable(boolean value) {
            this.value = value;
        }

        boolean getValue() {
            return this.value;
        }
    }

    static String nonNull(String str) {
        return str == null ? "" : str;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            return _execute(action, args, callbackContext);
        } catch (IOException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
            callbackContext.error(e.getMessage());
            return true;
        }
    }


    private boolean _execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException, GooglePlayServicesNotAvailableException, IOException, GooglePlayServicesRepairableException {
        if (action.equals("verifyConsent")) {
            boolean isAgeConsent = args.optBoolean(0);
            boolean isDebug = args.optBoolean(1);
            String testDeviceHashId = args.optString(2);
            verifyConsent(isAgeConsent, isDebug, testDeviceHashId, callbackContext);
            return true;
        } else if (action.equals("forceForm")) {
            forceForm(callbackContext);
            return true;
        } else if (action.equals("reset")) {
            reset(callbackContext);
            return true;
        }

        return false;
    }

    private void verifyConsent(boolean isAgeConsent, boolean isDebug, String testDeviceHashId, CallbackContext callbackContext)
            throws GooglePlayServicesNotAvailableException, IOException, GooglePlayServicesRepairableException {

        ConsentRequestParameters requestParameters;

        if (isDebug) {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this.cordova.getContext());
            ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(cordova.getActivity())
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId(nonNull(testDeviceHashId))
                    .build();

            requestParameters = new ConsentRequestParameters.Builder()
                    .setConsentDebugSettings(debugSettings)
                    .build();
        } else {
            requestParameters = new ConsentRequestParameters.Builder()
                    .build();
        }

        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(cordova.getContext());
        consentInformation.requestConsentInfoUpdate(cordova.getActivity(), requestParameters,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        if (consentInformation.isConsentFormAvailable()) {
                            loadForm(consentInformation, false, callbackContext);
                        } else {
                            success(Consent.OBTAINED, HasShownDialog.NOT_SHOWN, FormAvailable.NOT_AVAILABLE, callbackContext);
                        }
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        callbackContext.error(formError.getErrorCode());
                    }
                }
        );
    }

    private void forceForm(CallbackContext callbackContext) {
        loadForm(UserMessagingPlatform.getConsentInformation(cordova.getContext()), true, callbackContext);
    }

    private void reset(CallbackContext callbackContext) {
        UserMessagingPlatform.getConsentInformation(cordova.getContext()).reset();
    }

    private void loadForm(ConsentInformation consentInformation, boolean forceForm, CallbackContext callbackContext) {
        if (forceForm || consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
            presentForm(consentInformation, callbackContext);
        } else {
            success(Consent.OBTAINED, HasShownDialog.NOT_SHOWN, FormAvailable.AVAILABLE, callbackContext);
        }
    }

    private void presentForm(ConsentInformation consentInformation, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UserMessagingPlatform.loadConsentForm(cordova.getContext(),
                        new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                            @Override
                            public void onConsentFormLoadSuccess(ConsentForm consentForm) {
                                consentForm.show(cordova.getActivity(),
                                        new ConsentForm.OnConsentFormDismissedListener() {
                                            @Override
                                            public void onConsentFormDismissed(FormError formError) {
                                                if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                                                    success(Consent.OBTAINED, HasShownDialog.SHOWN, FormAvailable.AVAILABLE, callbackContext);
                                                } else {
                                                    success(Consent.NOT_OBTAINED, HasShownDialog.SHOWN, FormAvailable.AVAILABLE, callbackContext);
                                                }
                                            }
                                        });
                            }
                        },
                        new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                            @Override
                            public void onConsentFormLoadFailure(FormError formError) {
                                callbackContext.error(formError.getMessage());
                            }
                        }
                );
            }
        });
    }

    private void success(Consent consent, HasShownDialog hasShownDialog, FormAvailable formAvailable, CallbackContext callbackContext) {
        JSONObject json = new JSONObject();
        try {
            json.put("consent", consent.getValue());
            json.put("hasShownDialog", hasShownDialog.getValue());
            json.put("formAvailable", formAvailable.getValue());
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
        } catch (JSONException e) {
            callbackContext.error(e.getMessage());
        }
    }

}
