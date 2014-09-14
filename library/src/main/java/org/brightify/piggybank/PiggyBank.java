package org.brightify.piggybank;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StringRes;

/**
 * @author <a href="mailto:hyblmatous@gmail.com">Matous Hybl</a>
 */
public class PiggyBank {

    private Activity activity;
    private OnDonationListener donationListener;
    private String sku;
    private String message = "We are poor and we need money. Please donate.";
    private String title = "Give us money!";
    private String cancelText= "Maybe later.";
    private String donateText = "Donate %s";
    private boolean showPrice = false;

    private PiggyBank() {

    }

    public interface OnDonationListener {
        public void onPurchased(String sku);
    }

    public void donate() {

    }

    public boolean isDonated() {
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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
}
