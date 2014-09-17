package org.brightify.piggybank.example;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.brightify.piggybank.PiggyBank;
import org.brightify.piggybank.PiggyBankActivity;


public class MainActivity extends PiggyBankActivity {

    private static final String SKU = "piggy_donation";

    private PiggyBank piggyBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected PiggyBank getPiggyBank() {
        if (piggyBank == null) {
            PiggyBank.Builder builder = new PiggyBank.Builder(this);
            builder.setSKU(SKU);
            //builder.showPrice(true);
            piggyBank = builder.build();
        }
        return piggyBank;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.donate) {
            getPiggyBank().donate(new PiggyBank.DonateCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Donated.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String reason) {
                    Toast.makeText(MainActivity.this, "Something went wrong while donating. " + reason,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}
