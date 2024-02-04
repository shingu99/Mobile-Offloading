package com.example.mobileoffloadingmatrix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MasterHomePage extends AppCompatActivity {

    Button calculate_mul;
    double[][] matrix1,matrix2,matrix3;
    int i,j,k;
    String a,b,c;
    Intent intent,batteryStatus;
    TextView device,battery,lat,longi;
    FusedLocationProviderClient fusedLocationClient;
    private static final int row_col = 100;



    private Button btnOnOff, btnDiscover, btnSend;
    protected TextView connectionStatus;
    private TextView read_msg_box;
    private EditText writeMsg;
    private ListView peerListView;
    private WifiManager wifiManager;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_home_page);


        intialize();
        getBatteryInfo();
        getLocationInfo();
        create_random_matrix();

        initialWOrk();
        exqListener();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        calculate_mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiplyMatrices(matrix1,matrix2);
            }
        });

    }

    public void create_random_matrix()
    {
        Random rand = new Random();
        a = new String();
        b = new String();
        matrix1 = new double[row_col][row_col];
        matrix2 = new double[row_col][row_col];
        a+=" [\n";
        b+=" [\n";
        for(i=0;i<matrix1.length;i++)
        {
            a+= "   [";
            b+= "   [";
            for(j=0;j<matrix1[0].length;j++)
            {
                matrix1[i][j] = j+15;
                matrix2[i][j] = i+15;
                a += matrix1[i][j]+"\t";
                b += matrix2[i][j]+"\t";
            }
            a+= " ]";
            b+= " ]";
            a+="\n";
            b+="\n";
        }
        a+= " ]";
        b+= " ]";
    }

    public void display_matrix(String txt,String a)
    {
        Toast.makeText(getApplicationContext(), txt+a, Toast.LENGTH_LONG).show();
    }

    public void MultiplyMatrices(double [][] a_mat,double [][] b_mat)
    {
        double t_start = System.currentTimeMillis();
        double p_start = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        double c_start = batteryStatus.getIntExtra(String.valueOf(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW), 0);

        matrix3 = new double[row_col][row_col];
        c = new String();
        c += " [\n";
        for(i=0;i<a_mat.length;i++)
        {
            c += "   [";
            for(j=0;j<a_mat[0].length;j++)
            {
                for(k=0;k< b_mat[0].length;k++)
                {
                    matrix3[i][j] += a_mat[i][k] * b_mat[k][j];
                }
                c+= matrix3[i][j]+"\t";
            }
            c += " ]\n";
        }
        c += " ]";



        if(intent.getStringExtra("Status").equals("Slave"))
        {
            display_matrix("Multiplied Matrix Slave",c);
            double t_end = System.currentTimeMillis();
            sendReceive.write(("Multiplied Matrix "+c).getBytes(StandardCharsets.UTF_8) );

            Toast.makeText(getApplicationContext(), "Time taken slave = "+(t_end-t_start)+"ms", Toast.LENGTH_SHORT).show();
            double c_end = batteryStatus.getIntExtra(String.valueOf(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE), 0);
            double p_end = batteryStatus.getIntExtra(String.valueOf(BatteryManager.EXTRA_VOLTAGE),0);
            Toast.makeText(getApplicationContext(), "Power taken slave"+(Float.parseFloat((p_end*t_end/3600000)+"")+" mAh"), Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(), ""+c_start+"-"+c_end+"-"+p_start+"-"+p_end, Toast.LENGTH_SHORT).show();
        }
        else
        {
            display_matrix("Multiplied Matrix Master",c);
            double t_end = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "Time taken master = "+(t_end-t_start)+"ms", Toast.LENGTH_SHORT).show();
            double c_end = batteryStatus.getIntExtra(String.valueOf(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW), 0);
            double p_end = batteryStatus.getIntExtra(String.valueOf(BatteryManager.EXTRA_VOLTAGE),0);
            Toast.makeText(getApplicationContext(), "Power taken master"+(Float.parseFloat((p_end*t_end/3600000)+""))+" mAh", Toast.LENGTH_SHORT).show();
        }

    }



    public void intialize()
    {
        intent = getIntent();
        battery = findViewById(R.id.battery);
        lat = findViewById(R.id.lat);
        longi = findViewById(R.id.longi);
        calculate_mul = findViewById(R.id.cal_mul);

    }




    public void getBatteryInfo(){
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, iFilter);
        int batteryPercentage = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int batteryVoltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
//        Toast.makeText(getApplicationContext(), "voltage: "+batteryVoltage, Toast.LENGTH_SHORT).show();
        battery.setText(batteryPercentage + "%");
        //insertBatteryData(batteryPercentage, deviceName, "");
    }

    public double locationProximity(double latitude_1, double longitude_1, double latitude_2, double longitude_2){
        Location location_1 = new Location("Location_1");
        location_1.setLatitude(latitude_1);
        location_1.setLongitude(longitude_1);
        Location location_2 = new Location("Location_2");
        location_1.setLatitude(latitude_2);
        location_1.setLongitude(longitude_2);
        double distance = location_1.distanceTo(location_2);
        return distance;
    }

    @SuppressLint("MissingPermission")
    public void getLocationInfo(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.i("testloc","Inside getloc");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.i("testloc","Inside getloc 2");
                    double latitude_1 = location.getLatitude();
                    double longitude_1 = location.getLongitude();
                    Toast.makeText(getApplicationContext(), "lat "+latitude_1, Toast.LENGTH_SHORT).show();
                    lat.setText(latitude_1+"");
                    longi.setText(longitude_1+"");
                    //implement code for location of connected slave/Master.
                    double distance = locationProximity(latitude_1, longitude_1, 33.1, -111.1);
                }
            }
        });


    }


    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void exqListener() {


        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverPeers();
            }
        });
        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Connected to "+ device.deviceName, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i) {
                            Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},123);
                }


            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String msg=writeMsg.getText().toString();
                Toast.makeText(getApplicationContext(), "name = "+intent.getStringExtra("Status"), Toast.LENGTH_SHORT).show();
                if(intent.getStringExtra("Status").equals("Master")) {
                    sendReceive.write(("matrix a = " + a).getBytes(StandardCharsets.UTF_8));
                    sendReceive.write(("matrix a = " + a).getBytes(StandardCharsets.UTF_8));
                    sendReceive.write(("matrix b = " + b).getBytes(StandardCharsets.UTF_8));
                }
                else if(intent.getStringExtra("Status").equals("Slave")) {
                    sendReceive.write(("Lat = " + lat.getText().toString()).getBytes(StandardCharsets.UTF_8));
                    sendReceive.write(("Lat = " + lat.getText().toString()).getBytes(StandardCharsets.UTF_8));
                    sendReceive.write(("Longi = " + longi.getText().toString()).getBytes(StandardCharsets.UTF_8));
                    sendReceive.write(("battery = " + battery.getText().toString()).getBytes(StandardCharsets.UTF_8));
                }


            }
        });
    }

    private void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    connectionStatus.setText("Discovery Started");
                }

                @Override
                public void onFailure(int i) {
                    connectionStatus.setText("Discovery Starting failed");
                }
            });
            return;
        }else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==123 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            discoverPeers();
        }
    }

    private void initialWOrk() {
        //btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnSend = findViewById(R.id.send);

        connectionStatus = findViewById(R.id.connectionStatus);
        read_msg_box = findViewById(R.id.readMsg);
//

        peerListView = findViewById(R.id.peerListView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WiFiBroadcastReceiver(mManager, mChannel, this,peerListListener, connectionInfoListener, connectionStatus);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                peerListView.setAdapter(arrayAdapter);

                if (peers.size()==0){
                    Toast.makeText(getApplicationContext(), "No Device Found!!!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                connectionStatus.setText("Master");
                serverClass=new ServerClass();
                serverClass.start();
            }
            else if (wifiP2pInfo.groupFormed)
            {
                connectionStatus.setText("Slave");
                clientClass=new ClientClass(groupOwnerAdress);
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket =  new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (socket!=null)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        public ClientClass(InetAddress hostAddress)
        {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();

        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




