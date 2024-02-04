package com.example.mobileoffloadingmatrix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {


Intent intent;
EditText device;
Button start;
RadioButton master,slave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intialize_Views();
        start.setEnabled(false);

        master.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplicationContext(), "Selected Master", Toast.LENGTH_SHORT).show();
                if(device.getText().length()>0 && (master.isChecked() || slave.isChecked()))
                {
                    start.setEnabled(true);
                }
            }
        });

        slave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplicationContext(), "Selected Slave", Toast.LENGTH_SHORT).show();
                if(device.getText().length()>0 && (master.isChecked() || slave.isChecked()))
                {
                    start.setEnabled(true);
                }
            }
        });





        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(master.isChecked())
                {
                    intent = new Intent(getApplicationContext(), MasterHomePage.class);
                    intent.putExtra("Status","Master");
                }
                else
                {
                    intent = new Intent(getApplicationContext(), MasterHomePage.class);
                    intent.putExtra("Status","Slave");
                }
                intent.putExtra("ID",device.getText().toString());
                startActivity(intent);
            }
        });


    }

    public void Intialize_Views()
    {
        device = findViewById(R.id.device);
        master = findViewById(R.id.master);
        slave = findViewById(R.id.slave);
        start = findViewById(R.id.start);
    }


}