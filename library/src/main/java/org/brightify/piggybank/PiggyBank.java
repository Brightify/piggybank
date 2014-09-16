package org.brightify.piggybank;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.StringRes;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author <a href="mailto:hyblmatous@gmail.com">Matous Hybl</a>
 */
public class PiggyBank {

    private static final int PURCHASE_REQUEST_CODE = 127;

    private Activity activity;
    private OnDonationListener donationListener;
    private String sku;
    private String message = "We are poor and we need money. Please donate.";
    private String title = "Give us money!";
    private String cancelText = "Maybe later.";
    private String donateText = "Donate";
    private boolean showPrice = false;

    private IInAppBillingService billingService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            billingService = IInAppBillingService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            billingService = null;
        }
    };

    private PiggyBank() {

    }

    public void donate(final DonateCallback donateCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        if (showPrice) {
            if (!donateText.contains(" %s")) {
                donateText += " %s";
            }
            try {
                donateText = String.format(donateText, getPrice());
            } catch (RemoteException e) {
                e.printStackTrace();
                donateCallback.onFailure("Failed to get price." + e.getMessage());
            }
        }
        builder.setPositiveButton(donateText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Bundle purchaseBundle = billingService.getBuyIntent(3, activity.getPackageName(), sku, "INAPP", "");
                    PendingIntent purchaseIntent = purchaseBundle.getParcelable("BUY_INTENT");
                    if (purchaseIntent == null) {
                        donateCallback.onFailure("You have not published the app.");
                        return;
                    }
                    activity.startIntentSenderForResult(purchaseIntent.getIntentSender(),
                            PURCHASE_REQUEST_CODE, new Intent(), 0, 0, 0);
                    donateCallback.onSuccess();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    donateCallback.onFailure("Failed to purchase. " + e.getMessage());
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    donateCallback.onFailure("Failed to purchase. " + e.getMessage());
                }
            }
        });
        builder.show();
    }

    public boolean isDonated() throws RemoteException {
        Bundle ownedSkus = billingService.getPurchases(3, activity.getPackageName(), "inapp", null);
        return ownedSkus.getInt("RESPONSE_CODE") == 0
                && ownedSkus.getStringArrayList("INAPP_PURCHASE_ITEM_LIST").contains(sku);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PURCHASE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            donationListener.onPurchased(sku);
        }
    }

    public void onCreate() {
        activity.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"),
                serviceConnection, Activity.BIND_AUTO_CREATE);
    }

    public void onDestroy() {
        if (billingService != null) {
            activity.unbindService(serviceConnection);
        }
    }

    public String getPrice() throws RemoteException {
        String price = "0.99$";
        ArrayList<String> skus = new ArrayList<String>();
        skus.add(sku);
        Bundle query = new Bundle();
        query.putStringArrayList("ITEM_ID_LIST", skus);
        try {
            Bundle skuDetails = billingService.getSkuDetails(3, activity.getPackageName(), "inapp", query);
            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                for (String detail : responseList) {
                    JSONObject jsonObject = new JSONObject(detail);
                    if (sku.equals(jsonObject.getString("productId"))) {
                        return jsonObject.getString("price");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return price;
    }

    public static class Builder {

        private PiggyBank piggyBank = new PiggyBank();

        public Builder(Activity activity) {
            piggyBank.activity = activity;
        }

        public PiggyBank build() {
            return piggyBank;
        }

        public Builder setSKU(String sku) {
            piggyBank.sku = sku;
            return this;
        }

        public Builder setOnDonationListener(OnDonationListener listener) {
            piggyBank.donationListener = listener;
            return this;
        }

        public Builder setMessage(String message) {
            piggyBank.message = message;
            return this;
        }

        public Builder setMessage(@StringRes int id) {
            piggyBank.message = piggyBank.activity.getString(id);
            return this;
        }

        public Builder setTitle(String title) {
            piggyBank.title = title;
            return this;
        }

        public Builder setTitle(@StringRes int id) {
            piggyBank.title = piggyBank.activity.getString(id);
            return this;
        }

        public Builder setCancelText(String text) {
            piggyBank.cancelText = text;
            return this;
        }

        public Builder setCancelText(@StringRes int id) {
            piggyBank.cancelText = piggyBank.activity.getString(id);
            return this;
        }

        public Builder setDonateText(String donateText) {
            piggyBank.donateText = donateText;
            return this;
        }

        public Builder setDonateText(@StringRes int id) {
            piggyBank.donateText = piggyBank.activity.getString(id);
            return this;
        }

        public Builder showPrice(boolean show) {
            piggyBank.showPrice = show;
            return this;
        }
    }

    public interface OnDonationListener {
        public void onPurchased(String sku);
    }

    public interface DonateCallback {
        public void onSuccess();

        public void onFailure(String reason);
    }
}
