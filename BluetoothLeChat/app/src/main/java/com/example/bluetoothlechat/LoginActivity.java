package com.example.bluetoothlechat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.example.bluetoothlechat.R;

public class LoginActivity extends AppCompatActivity {
    Button ScanUid;
    EditText UID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ScanUid = (Button) findViewById(R.id.ScanUid);
        UID = (EditText) findViewById(R.id.UID);
        Intent intent = new Intent(this , com.example.bluetoothlechat.MainActivity.class);
        ScanUid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data;
                data = UID.getText().toString();
                if(data.length() == 0 || data.contains(" ") || data.contains("\\"))
                    Toast.makeText(getApplicationContext(),"Please enter a valid ID", Toast.LENGTH_LONG).show();
                else{
                    // Save data saomewhewe
                    Toast.makeText(getApplicationContext(), "User ID: "+data, Toast.LENGTH_LONG).show();
                    // make intent call;
                    startActivity(intent);

                }
            }
        });
    }
}