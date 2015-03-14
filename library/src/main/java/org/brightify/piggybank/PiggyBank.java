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
    private String message;
    private String title;
    private String cancelText;
    private String donateText;
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

    /**
     * Method that performs donation
     *
     * @param donateCallback Callback to notify activity about succeeded or failed attempt to donation
     */
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
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    donateCallback.onFailure("Failed to purchase. " + e.getMessage());
                }
            }
        });
        builder.show();
    }

    /**
     * Method for determining whether the user has already donated
     *
     * @return true if user has donated, false when it has not
     */
    public boolean isDonated() throws RemoteException {
        Bundle ownedSkus = billingService.getPurchases(3, activity.getPackageName(), "inapp", null);
        return ownedSkus.getInt("RESPONSE_CODE") == 0
               && ownedSkus.getStringArrayList("INAPP_PURCHASE_ITEM_LIST").contains(sku);
    }

    /**
     * Must be called in activity's onActivityResult
     *
     * @param requestCode code of request
     * @param resultCode  code of result
     * @param intent      Intent containing data from stopped activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PURCHASE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            donationListener.onPurchased(sku);
        }
    }

    /**
     * Must be called in activity's onCreate
     */
    public void onCreate() {
        Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        intent.setPackage("com.android.vending");
        activity.bindService(intent, serviceConnection, Activity.BIND_AUTO_CREATE);
    }

    /**
     * Must be called in activity's onDestroy
     */
    public void onDestroy() {
        if (billingService != null) {
            activity.unbindService(serviceConnection);
        }
    }

    /**
     * This method is used to obtain price
     *
     * @return String containing price with currency
     */
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

    /**
     * Class used for getting PiggyBank instance
     */
    public static class Builder {

        private PiggyBank piggyBank = new PiggyBank();

        public Builder(Activity activity) {
            piggyBank.activity = activity;
            piggyBank.message = activity.getString(R.string.dialogMessage);
            piggyBank.title = activity.getString(R.string.dialogTitle);
            piggyBank.cancelText = activity.getString(R.string.cancelButtonText);
            piggyBank.donateText = activity.getString(R.string.dialogDonateButtonText);
        }

        /**
         * @return ready to use PiggyBank object
         */
        public PiggyBank build() {
            return piggyBank;
        }

        /**
         * Sets SKU of donation
         *
         * @param sku String with SKU from Google Play
         *
         * @return current instance of this Builder
         */
        public Builder setSKU(String sku) {
            piggyBank.sku = sku;
            return this;
        }

        /**
         * Sets listener whose method is invoked when donation is performed
         *
         * @param listener instance of OnDonationListener
         *
         * @return current instance of Builder
         */
        public Builder setOnDonationListener(OnDonationListener listener) {
            piggyBank.donationListener = listener;
            return this;
        }

        /**
         * Sets message displayed in donating dialog
         *
         * @param message message to be displayed
         *
         * @return current instance of Builder
         */
        public Builder setMessage(String message) {
            piggyBank.message = message;
            return this;
        }

        /**
         * Sets message displayed in donating dialog
         *
         * @param id id of String resource
         *
         * @return current instance of Builder
         */
        public Builder setMessage(@StringRes int id) {
            piggyBank.message = piggyBank.activity.getString(id);
            return this;
        }

        /**
         * Sets the title of donating dialog
         *
         * @param title title to be displayed
         *
         * @return current instance of Builder
         */
        public Builder setTitle(String title) {
            piggyBank.title = title;
            return this;
        }

        /**
         * Sets the title of donating dialog
         *
         * @param id of String resource to be displayed
         *
         * @return current instance of Builder
         */
        public Builder setTitle(@StringRes int id) {
            piggyBank.title = piggyBank.activity.getString(id);
            return this;
        }

        /**
         * Sets text of Cancel button in the donating dialog
         *
         * @param text text to be displayed on the cancel button
         *
         * @return current instance of Builder
         */
        public Builder setCancelText(String text) {
            piggyBank.cancelText = text;
            return this;
        }

        /**
         * Sets text of cancel button in the donating dialog
         *
         * @param id id of String resource to be displayed on the cancel button
         *
         * @return current instance of Builder
         */
        public Builder setCancelText(@StringRes int id) {
            piggyBank.cancelText = piggyBank.activity.getString(id);
            return this;
        }

        /**
         * Sets text of donate button in the donating dialog
         *
         * @param donateText text to be displayed on the Donate button
         *
         * @return current instance of this Builder
         */
        public Builder setDonateText(String donateText) {
            piggyBank.donateText = donateText;
            return this;
        }

        /**
         * Sets text of donate button in the donating dialog
         *
         * @param id of a String resource to be displayed on the Donate button
         *
         * @return current instance of this Builder
         */
        public Builder setDonateText(@StringRes int id) {
            piggyBank.donateText = piggyBank.activity.getString(id);
            return this;
        }

        /**
         * Enables showing of price on the donate button
         *
         * @param show enables or disables price showing
         *
         * @return current instance of this Builder
         */
        public Builder showPrice(boolean show) {
            piggyBank.showPrice = show;
            return this;
        }
    }

    /**
     * Interface that's method is used to notify activity about performed donation
     */
    public interface OnDonationListener {
        public void onPurchased(String sku);
    }

    /**
     * CallBack interface used for notifying about errors in donating process
     */
    public interface DonateCallback {
        public void onSuccess();

        public void onFailure(String reason);
    }
}
