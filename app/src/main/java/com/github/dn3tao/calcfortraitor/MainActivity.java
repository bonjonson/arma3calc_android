package com.github.dn3tao.calcfortraitor;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends AppCompatActivity implements OnClickListener { //указание что эта активити использует интерфес онКликЛистенера

    private static final String CALIBER = "CALIBER";
    // идентификатор диалогового окна AlertDialog с кнопками
    private final int IDD_TWO_BUTTONS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView button120 = findViewById(R.id.button120);
        CardView button82 = findViewById(R.id.button82);
        CardView button30 = findViewById(R.id.button30);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, CalculateActivity.class);
        switch (v.getId()) {
            case R.id.button120:
                intent.putExtra(CALIBER, 120);
                startActivity(intent);
                break;
            case R.id.button82:
                intent.putExtra(CALIBER, 82);
                startActivity(intent);
                break;
            case R.id.button30:
                intent.putExtra(CALIBER, 30);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case IDD_TWO_BUTTONS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.youtube_confirm_text)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes_button),
                                (dialog, id1) -> {
                                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_URL)));
                                    try {
                                        MainActivity.this.startActivity(webIntent);
                                    } catch (ActivityNotFoundException ex) {
                                    }
                                })
                        .setNegativeButton(getString(R.string.no_button),
                                (dialog, id12) -> dialog.cancel());

                return builder.create();
            default:
                return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_faq:
                showDialog(IDD_TWO_BUTTONS);

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}
