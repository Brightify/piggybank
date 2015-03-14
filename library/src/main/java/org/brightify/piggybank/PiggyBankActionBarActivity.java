package org.brightify.piggybank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * @author <a href="mailto:hyblmatous@gmail.com">Matous Hybl</a>
 */
public abstract class PiggyBankActionBarActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPiggyBank().onCreate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPiggyBank().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getPiggyBank().onActivityResult(requestCode, resultCode, data);
    }

    protected abstract PiggyBank getPiggyBank();
}
