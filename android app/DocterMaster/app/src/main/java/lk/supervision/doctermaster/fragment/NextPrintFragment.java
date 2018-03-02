package lk.supervision.doctermaster.fragment;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lk.supervision.doctermaster.R;
import lk.supervision.doctermaster.common.AppEnvironmentValues;
import lk.supervision.doctermaster.common.RestApiRecever;
import lk.supervision.doctermaster.database.DocterMasterDbHelper;
import lk.supervision.doctermaster.model.MSettings;
import lk.supervision.doctermaster.model.MaxAppointmentAndRuningNo;
import lk.supervision.doctermaster.print.UnicodeFormatter;

public class NextPrintFragment extends Fragment implements Runnable {

    //bluetooth
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothAdapter mBluetoothAdapter;

    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;
    private View nextPrintFragment;
    private DocterMasterDbHelper docterMasterDbHelper;
    private MSettings mSettings;

    @BindView(R.id.btn_print_next_no)
    Button btnPrintNextNo;

    @BindView(R.id.btn_online_print_next_no)
    Button btnOnlinePrintNextNo;

    @BindView(R.id.btn_lastno_sync_maual)
    Button btnLastNoSyncManual;

    @BindView(R.id.btn_network_connect)
    Button btnNetWorkConnect;

//    @BindView(R.id.btn_printer_connect)
//    Button btnPrinterConnect;

    @BindView(R.id.text_running_no)
    Button textRunningNo;

    @BindView(R.id.text_online_print_no)
    EditText textOnlinePrintNo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        nextPrintFragment = inflater.inflate(R.layout.fragment_next_print, container, false);
        docterMasterDbHelper = new DocterMasterDbHelper(nextPrintFragment.getContext());
        ButterKnife.bind(this, nextPrintFragment);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getNextPrintNoAndRunningNo();

            }
        }, 0, 1000);
        networkRefresh();
        return nextPrintFragment;
    }

    @OnClick(R.id.btn_print_next_no)
    public void printNextNo(View view) {
        mSettings = docterMasterDbHelper.getMSettings();
        String deafaulBluetoothPrinter = docterMasterDbHelper.getDeafaulBluetoothPrinter();
        mBluetoothSocket = getBlueToothPrinter(deafaulBluetoothPrinter);
        SharedPreferences sharedPreferences = nextPrintFragment.getContext().getSharedPreferences("SVDOCTERMASTER", 0);
        final String loginDocterName = sharedPreferences.getString("LOGIN_DOCTER_NAME", "NULL");
        final String loginLocationName = sharedPreferences.getString("LOCATION_NAME", "NULL");
        final Integer loginDocterIndex = sharedPreferences.getInt("LOGIN_DOCTER_ID", 0);
        final Integer loginLocationIndex = sharedPreferences.getInt("LOCATION_ID", 0);
        networkRefresh();
        getNextPrintNoAndRunningNo();
        RestApiRecever restApiRecever = new RestApiRecever();
        try {
            final Integer printNextNo = restApiRecever.getPrintNextNo(AppEnvironmentValues.SIMPLE_DATE_FORMAT.format(new Date()), loginDocterIndex, loginLocationIndex);
            if (mBluetoothSocket != null) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            OutputStream os = mBluetoothSocket.getOutputStream();
                            String BILL = "";
                            BILL = BILL + "\n";
                            BILL = BILL + "\n";
                            BILL = BILL + "\n";
                            BILL = BILL + "-------------------------------\n";
                            BILL = BILL + mSettings.getCenterName() + "\n";
                            BILL = BILL + mSettings.getCenterAddress() + "\n";
                            BILL = BILL + mSettings.getCenterContactNo() + "\n";
                            BILL = BILL + "-------------------------------\n";
                            BILL = BILL + "DOCTER - " + loginDocterName + " \n";
                            BILL = BILL + "LOCATION - " + loginLocationName + " \n";
                            BILL = BILL + "APPOYMENT NO  - " + printNextNo.toString() + " \n";
                            BILL = BILL + AppEnvironmentValues.getSystemDateTimeFormat() + " \n";
                            BILL = BILL + "-------------------------------\n";
                            BILL = BILL + "     " + mSettings.getFooter();
                            BILL = BILL + "\n";
                            BILL = BILL + "\n";
                            BILL = BILL + "\n";
                            BILL = BILL + "\n";
                            BILL = BILL + "\n";
                            os.write(BILL.getBytes());
                            //This is printer specific code you can comment ==== > Start

                            // Setting height
                            int gs = 29;
                            os.write(intToByteArray(gs));
                            int h = 104;
                            os.write(intToByteArray(h));
                            int n = 162;
                            os.write(intToByteArray(n));

                            // Setting Width
                            int gs_width = 29;
                            os.write(intToByteArray(gs_width));
                            int w = 119;
                            os.write(intToByteArray(w));
                            int n_width = 2;
                            os.write(intToByteArray(n_width));
                        } catch (Exception e) {
                            Log.d("PRINT ACTIVITY", e.toString());
                        }
                        closeSocket(mBluetoothSocket);
                    }
                };
                t.start();
            }
        } catch (Exception e) {
            AppEnvironmentValues.snackbarCustome(nextPrintFragment, "SERVER NOT CONNECT", "ERROR");
        }
    }

    @OnClick(R.id.btn_online_print_next_no)
    public void printOnlineNextNo(View view) {
        if (textOnlinePrintNo.getText().toString().isEmpty()) {
            textOnlinePrintNo.setError("PLEASE ENTER ONLINE PAYMENT NO");
        } else {
            networkRefresh();
            getNextPrintNoAndRunningNo();
            mSettings = docterMasterDbHelper.getMSettings();
            String deafaulBluetoothPrinter = docterMasterDbHelper.getDeafaulBluetoothPrinter();
            mBluetoothSocket = getBlueToothPrinter(deafaulBluetoothPrinter);
            SharedPreferences sharedPreferences = nextPrintFragment.getContext().getSharedPreferences("SVDOCTERMASTER", 0);
            final String loginDocterName = sharedPreferences.getString("LOGIN_DOCTER_NAME", "NULL");
            final String loginLocationName = sharedPreferences.getString("LOCATION_NAME", "NULL");
            final Integer loginDocterIndex = sharedPreferences.getInt("LOGIN_DOCTER_ID", 0);
            final Integer loginLocationIndex = sharedPreferences.getInt("LOCATION_ID", 0);
            RestApiRecever restApiRecever = new RestApiRecever();
            try {
                final Integer printNextNo = restApiRecever.getOnlinePrintCode(AppEnvironmentValues.SIMPLE_DATE_FORMAT.format(new Date()), loginDocterIndex, loginLocationIndex, textOnlinePrintNo.getText().toString());
                if (mBluetoothSocket != null) {
                    Thread t = new Thread() {
                        public void run() {
                            try {
                                OutputStream os = mBluetoothSocket.getOutputStream();
                                String BILL = "";
                                BILL = BILL + "\n";
                                BILL = BILL + "\n";
                                BILL = BILL + "\n";
                                BILL = BILL + "-------------------------------\n";
                                BILL = BILL + mSettings.getCenterName() + "\n";
                                BILL = BILL + mSettings.getCenterAddress() + "\n";
                                BILL = BILL + mSettings.getCenterContactNo() + "\n";
                                BILL = BILL + "-------------------------------\n";
                                BILL = BILL + "DOCTER - " + loginDocterName + " \n";
                                BILL = BILL + "LOCATION - " + loginLocationName + " \n";
                                BILL = BILL + "APPOYMENT NO  - " + printNextNo + "\n";
                                BILL = BILL + AppEnvironmentValues.getSystemDateTimeFormat() + " \n";
                                BILL = BILL + "-------------------------------\n";
                                BILL = BILL + "     " + mSettings.getFooter();
                                BILL = BILL + "\n";
                                BILL = BILL + "\n";
                                BILL = BILL + "\n";
                                BILL = BILL + "\n";
                                BILL = BILL + "\n";
                                os.write(BILL.getBytes());
                                //This is printer specific code you can comment ==== > Start

                                // Setting height
                                int gs = 29;
                                os.write(intToByteArray(gs));
                                int h = 104;
                                os.write(intToByteArray(h));
                                int n = 162;
                                os.write(intToByteArray(n));

                                // Setting Width
                                int gs_width = 29;
                                os.write(intToByteArray(gs_width));
                                int w = 119;
                                os.write(intToByteArray(w));
                                int n_width = 2;
                                os.write(intToByteArray(n_width));
                            } catch (Exception e) {
                                Log.d("PRINT ACTIVITY", e.toString());
                            }
                            closeSocket(mBluetoothSocket);
                        }
                    };
                    t.start();
                }
            } catch (Exception e) {
                AppEnvironmentValues.snackbarCustome(nextPrintFragment, "SERVER NOT CONNECT", "ERROR");
            }
        }
        networkRefresh();
    }

    @OnClick(R.id.btn_lastno_sync_maual)
    public void syncLastNo(View view) {
        getNextPrintNoAndRunningNo();
        networkRefresh();
    }

    private void getNextPrintNoAndRunningNo() {
        if (isNetworkConnected()) {
            try {
                SharedPreferences sharedPreferences = nextPrintFragment.getContext().getSharedPreferences("SVDOCTERMASTER", 0);
                final Integer loginDocterIndex = sharedPreferences.getInt("LOGIN_DOCTER_ID", 0);
                final Integer loginLocationIndex = sharedPreferences.getInt("LOCATION_ID", 0);
                RestApiRecever restApiRecever = new RestApiRecever();
                MaxAppointmentAndRuningNo maxAppointmentAndRuningNo = restApiRecever.getMaxAppointmentAndRuningNo(AppEnvironmentValues.SIMPLE_DATE_FORMAT.format(new Date()), loginDocterIndex, loginLocationIndex);
                textRunningNo.setText(maxAppointmentAndRuningNo.getRunningNo().toString());
                btnLastNoSyncManual.setText("LAST SYNC -  " + AppEnvironmentValues.SIMPLE_TIME_FORMAT.format(new Date()));
            } catch (Exception ex) {
            }
        } else {
            AppEnvironmentValues.snackbarCustome(nextPrintFragment, "PLEASE CONNECT INTERNET", "ERROR");
        }
    }

    private void networkRefresh() {
        if (isNetworkConnected()) {
            btnNetWorkConnect.setText("NETWORK CONNECTED");
            btnNetWorkConnect.setBackgroundColor(nextPrintFragment.getResources().getColor(R.color.right));

        } else {
            btnNetWorkConnect.setText("NETWORK NOT CONNECTED");
            btnNetWorkConnect.setBackgroundColor(nextPrintFragment.getResources().getColor(R.color.error
            ));
        }
    }

    private BluetoothSocket getBlueToothPrinter(String blutoothDeviceAddress) {
        BluetoothDevice tempBluetoothDevice = null;
        BluetoothSocket tempBluetoothSocket = null;
        BluetoothAdapter tempBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> mPairedDevices = tempBluetoothAdapter.getBondedDevices();
        if (!mPairedDevices.isEmpty()) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                if (blutoothDeviceAddress.equals(mDevice.getAddress())) {
                    tempBluetoothDevice = mDevice;
                    break;
                }
            }
        }
        try {
            tempBluetoothSocket = tempBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            tempBluetoothAdapter.cancelDiscovery();
            tempBluetoothSocket.connect();
        } catch (IOException eConnectException) {
            closeSocket(mBluetoothSocket);
        } catch (NullPointerException ex) {
            AppEnvironmentValues.snackbarCustome(nextPrintFragment, "PRINT ERROR PLEASE RE CONFIG PRINTER", "ERROR");
        }
        Thread mBlutoothConnectThread = new Thread(this);
        mBlutoothConnectThread.start();
        return tempBluetoothSocket;
    }

    public void run() {
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothSocket.connect();
            mHandler.sendEmptyMessage(0);
        } catch (IOException eConnectException) {
            Log.d("PRINT ACTIVITY", "CouldNotConnectToSocket", eConnectException);
            closeSocket(mBluetoothSocket);
            return;
        } catch (NullPointerException ex) {
            Log.d("PRINT ACTIVITY", "NULL");
        }
    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d("PRINT ACTIVITY", "SocketClosed");
        } catch (IOException ex) {
            Log.d("PRINT ACTIVITY", "CouldNotCloseSocket");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(nextPrintFragment.getContext(), "BlueTooth Printer Device Connected", Toast.LENGTH_SHORT).show();
        }
    };

    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();
        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x" + UnicodeFormatter.byteToHex(b[k]));
        }
        return b[3];
    }

    public byte[] sel(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putInt(val);
        buffer.flip();
        return buffer.array();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) nextPrintFragment.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
