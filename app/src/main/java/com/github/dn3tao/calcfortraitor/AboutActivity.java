package com.github.dn3tao.calcfortraitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {
    TextView aboutLinkText, aboutLinkEmail, aboutWogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        aboutLinkText = findViewById(R.id.aboutLinkText);
        aboutLinkEmail = findViewById(R.id.aboutLinkEmail);
        aboutWogText = findViewById(R.id.aboutTextWog);
        aboutLinkText.setMovementMethod(LinkMovementMethod.getInstance());
        aboutLinkEmail.setMovementMethod(LinkMovementMethod.getInstance());
        aboutWogText.setMovementMethod(LinkMovementMethod.getInstance());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //включение стрелки назад в экшнбаре

    }
    @Override //переопределение стандартного поведения стрелки назад в экшнбаре
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
