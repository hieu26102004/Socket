package com.example.socket;
import android.Manifest;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BlueControl extends AppCompatActivity {
    ImageButton btnTb1, btnTb2, btnDis;
    TextView txt1, txtMAC;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    Set<BluetoothDevice> pairedDevices1;
    String address = null;
    private ProgressDialog progress;
    int flaglamp1;
    int flaglamp2;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); // receive the address of the bluetooth device
        setContentView(R.layout.activity_control);

        // Ánh xạ
        btnTb1 = (ImageButton) findViewById(R.id.btnTb1);
        btnTb2 = (ImageButton) findViewById(R.id.btnTb2);
        txt1 = (TextView) findViewById(R.id.textV1);
        txtMAC = (TextView) findViewById(R.id.textViewMAC);
        btnDis = (ImageButton) findViewById(R.id.btnDisc);

        // Kiểm tra và yêu cầu quyền Bluetooth
        checkBluetoothPermissions();

        new ConnectBT().execute(); // Call the class to connect

        btnTb1.setOnClickListener(v -> thietTbi1()); // Gọi hàm xử lý thiết bị 1
        btnTb2.setOnClickListener(v -> thiettb7()); // Gọi hàm xử lý thiết bị 7
        btnDis.setOnClickListener(v -> Disconnect()); // Gọi hàm ngắt kết nối
    }

    // Kiểm tra và yêu cầu quyền Bluetooth
    private void checkBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 101);
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BlueControl.this, "Đang kết nối...", "Xin vui lòng đợi!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter(); // Get Bluetooth adapter
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    // Kiểm tra quyền Bluetooth Connect
                    if (ActivityCompat.checkSelfPermission(BlueControl.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        btSocket.connect();
                    }
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Kết nối thất bại! Kiểm tra thiết bị.");
                finish();
            } else {
                msg("Kết nối thành công.");
                isBtConnected = true;
                pairedDevicesList1();
            }
            progress.dismiss();
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void pairedDevicesList1() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
            return;
        }

        pairedDevices1 = myBluetooth.getBondedDevices();
        if (pairedDevices1.size() > 0) {
            for (BluetoothDevice bt : pairedDevices1) {
                txtMAC.setText(bt.getName() + " - " + bt.getAddress()); // Hiển thị tên và địa chỉ MAC
            }
        } else {
            Toast.makeText(getApplicationContext(), "Không tìm thấy thiết bị kết nối.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pairedDevicesList1(); // Gọi lại khi đã được cấp quyền
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền BLUETOOTH_CONNECT để xem thiết bị.", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Thực hiện hành động nếu quyền BLUETOOTH_SCAN được cấp
        }
    }

    // Hàm xử lý cho thiết bị 1
    private void thietTbi1() {
        if (btSocket != null) {
            try {
                if (this.flaglamp1 == 0) {
                    this.flaglamp1 = 1;
                    this.btnTb1.setBackgroundResource(R.drawable.tb1on);
                    btSocket.getOutputStream().write("1".toString().getBytes());
                    txt1.setText("Thiết bị số 1 đang bật");
                    return;
                } else {
                    if (this.flaglamp1 != 1) return;
                    this.flaglamp1 = 0;
                    this.btnTb1.setBackgroundResource(R.drawable.tb1off);
                    btSocket.getOutputStream().write("A".toString().getBytes());
                    txt1.setText("Thiết bị số 1 đang tắt");
                }
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
    }

    // Hàm xử lý ngắt kết nối Bluetooth
    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
        finish();
    }

    private void thiettb7() {
        if (btSocket != null) {
            try {
                if (this.flaglamp2 == 0) {
                    this.flaglamp2 = 1;
                    this.btnTb2.setBackgroundResource(R.drawable.tb2on);
                    btSocket.getOutputStream().write("7".toString().getBytes());
                    txt1.setText("Thiết bị số 7 đang bật");
                } else {
                    if (this.flaglamp2 != 1) return;
                    this.flaglamp2 = 0;
                    this.btnTb2.setBackgroundResource(R.drawable.tb2off);
                    btSocket.getOutputStream().write("G".toString().getBytes());
                    txt1.setText("Thiết bị số 7 đang tắt");
                }
            } catch (IOException e) {
                msg("Lỗi");
            }
        }
    }
}
