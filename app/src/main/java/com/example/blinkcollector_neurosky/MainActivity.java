package com.example.blinkcollector_neurosky;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.blinkcollector_neurosky.files_list.FilesListActivity;
import com.example.blinkcollector_neurosky.repository.FilesListRepository;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final String DATE_FORMAT = "dd-M-yyyy hh:mm:ss";

    private BluetoothAdapter mBluetoothAdapter = null;
    private TgStreamReader tgStreamReader = null;

    private Button btn_start = null;
    private Button btn_stop = null;
    private Button btn_save = null;
    private GraphView graph1 = null;

    private Spinner blinksSpinner;
    private Button btnToFiles;
    private TextView directoryName;
    private TextView operatorName;

    @Inject
    FilesListRepository filesListRepository;

    ArrayList<Integer> rawData = new ArrayList<Integer>(Collections.nCopies(1536, 0)); // 512 Hz - 3 seconds 1536
    String[] numberOfBlinks = {"2", "3", "4"};

    String filename = null;

    private LineGraphSeries<DataPoint> series1 = null;
    DataPoint[] dataPoints = {};

    private int badPacketCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        try {
            // Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
                finish();
//				return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);
        // setGetDataTimeOutTime, the default time is 5s, please call it before connect() of connectAndStart()
        tgStreamReader.setGetDataTimeOutTime(6);
        // startLog, you will get more sdk log by logcat if you call this function
        tgStreamReader.startLog();

    }

    private void initView() {

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_stop.setEnabled(false);
        btn_save = (Button) findViewById(R.id.btn_save);
//        btn_save.setEnabled(false);

        blinksSpinner = (Spinner) findViewById(R.id.blinks_spinner);

        graph1 = (GraphView) findViewById(R.id.graph);

        btnToFiles = findViewById(R.id.btn_to_files);

        directoryName = findViewById(R.id.directoryName);

        operatorName = findViewById(R.id.operatorName);

        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, numberOfBlinks);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        blinksSpinner.setAdapter(adapter);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Log.d("iBlink", "onClick Start!!!");
                badPacketCount = 0;
                dataPoints = new DataPoint[rawData.size()];
                //isBTConnected
                if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                    // Prepare for connecting
                    tgStreamReader.stop();
                    tgStreamReader.close();
                }

                // Using connect() and start() to replace connectAndStart(),
                // please call start() when the state is changed to STATE_CONNECTED
                tgStreamReader.connect();
                //tgStreamReader.connectAndStart();

                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                tgStreamReader.stop();
                tgStreamReader.close();

                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
                btn_save.setEnabled(true);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dataPoints.length > 0 && isStoragePermissionGranted() && isStoragePermissionGrantedRead()) {

                    String base = directoryName.getText().toString();
                    String operator = operatorName.getText().toString();
                    String blink = blinksSpinner.getSelectedItem().toString();

                    filesListRepository.put(
                            blink,
                            operator,
                            base,
                            dataPoints
                    );
                }
                btn_start.setEnabled(true);
                btn_stop.setEnabled(false);
                //btn_save.setEnabled(false);
            }
        });

        btnToFiles.setOnClickListener(view -> {
            if (tgStreamReader != null) {
                tgStreamReader.stop();
                tgStreamReader.close();
            }
            startActivity(new Intent(this, FilesListActivity.class));
        });

        // activate horizontal scrolling
        graph1.getViewport().setScrollable(true);

        graph1.getViewport().setYAxisBoundsManual(true);
        graph1.getViewport().setMinY(-800);
        graph1.getViewport().setMaxY(800);
        // set manual X bounds
        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(0);
        graph1.getViewport().setMaxX(800);

        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey("data")) {
            Double[] message = (Double[]) intent.getExtras().get("data");
            if (message != null) {
                dataPoints = new DataPoint[message.length];
                for (int i = 0; i < message.length; ++i) {
                    dataPoints[i] = new DataPoint(i, message[i]);
                }

                series1 = new LineGraphSeries<DataPoint>(dataPoints);
                series1.setTitle("Fpz EEG data");
                series1.setColor(Color.GREEN);
                graph1.addSeries(series1);
            }
        } else {
            dataPoints = new DataPoint[rawData.size()];
            for (int i = 0; i < rawData.size(); ++i) {
                dataPoints[i] = new DataPoint(i, rawData.get(i));
            }

            series1 = new LineGraphSeries<DataPoint>(dataPoints);
            series1.setTitle("Fpz EEG data");
            series1.setColor(Color.GREEN);
            graph1.addSeries(series1);
        }

    }

  private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    // Do something when connecting
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    showToast("Connected", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    // Recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                    tgStreamReader.startRecordRawData();

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    // Recording raw data, exception handling
                    tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
            }

            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG, "onRecordFail: " + flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
            badPacketCount++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);
        }


        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.

            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);

            //Log.i(TAG,"onDataReceived");
        }

    };


    private boolean isPressing = false;
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;

    @SuppressLint("HandlerLeak")
    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            // MindDataType
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    proccessDataWave(msg.arg1);
                    break;
                case MindDataType.CODE_MEDITATION:
                    Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
                    break;
                case MindDataType.CODE_ATTENTION:
                    Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                    break;
                case MindDataType.CODE_POOR_SIGNAL://
                    int poorSignal = msg.arg1;
                    Log.d(TAG, "poorSignal:" + poorSignal);
                    break;
                case MSG_UPDATE_BAD_PACKET:
                    //tv_badpacket.setText("" + msg.arg1);

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void showToast(final String msg, final int timeStyle) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }


    public void proccessDataWave(int data) {

        rawData.remove(0);
        rawData.add(data);

        for (int i = 0; i < rawData.size(); ++i) {
            // add new DataPoint object to the array for each of your list entries
            dataPoints[i] = new DataPoint(i, rawData.get(i)); // not sure but I think the second argument should be of type double
        }

        series1.resetData(dataPoints);
    }

    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Write permission is granted");
                return true;
            } else {
                Log.v(TAG, "Write permission is revoked");
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1
                );
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Write permission is granted");
            return true;
        }
    }

    boolean isStoragePermissionGrantedRead() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1
                );
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

}