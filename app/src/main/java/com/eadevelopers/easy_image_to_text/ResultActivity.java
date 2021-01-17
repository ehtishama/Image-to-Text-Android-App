package com.eadevelopers.easy_image_to_text;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.eadevelopers.easy_image_to_text.databinding.ActivityMainBinding;
import com.eadevelopers.easy_image_to_text.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_result);

        String text = getIntent().getStringExtra("text");

        binding.resultEditText.setText(text);


        binding.copyButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {


                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Extracted text", text);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ResultActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();



            }
        });

    }


}