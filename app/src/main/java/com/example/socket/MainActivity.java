package com.example.socket;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button btnPaired;
    ListView listDanhSach;
    public static int REQUEST_BLUETOOTH = 1;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPaired = (Button) findViewById(R.id.btnTimthietbi);
        listDanhSach = (ListView) findViewById(R.id.listTb);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, REQUEST_BLUETOOTH);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                } else {
                    pairedDevicesList();
                }
            }
        });
    }

    private void pairedDevicesList() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            pairedDevices = myBluetooth.getBondedDevices();
            ArrayList<String> list = new ArrayList<>();

            if (!pairedDevices.isEmpty()) {
                for (BluetoothDevice bt : pairedDevices) {
                    list.add(bt.getName() + "\n" + bt.getAddress());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị kết nối.", Toast.LENGTH_LONG).show();
            }
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            listDanhSach.setAdapter(adapter);
            listDanhSach.setOnItemClickListener(myListClickListener);
        }
    }

    private final AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(MainActivity.this, BlueControl.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pairedDevicesList();
            } else {
                Toast.makeText(this, "Cần quyền BLUETOOTH_CONNECT để hoạt động", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
