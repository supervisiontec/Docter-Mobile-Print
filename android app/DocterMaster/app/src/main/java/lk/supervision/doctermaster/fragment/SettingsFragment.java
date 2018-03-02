package lk.supervision.doctermaster.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import lk.supervision.doctermaster.R;
import lk.supervision.doctermaster.activity.DeviceListActivity;
import lk.supervision.doctermaster.common.AppEnvironmentValues;
import lk.supervision.doctermaster.database.DocterMasterDbHelper;
import lk.supervision.doctermaster.model.MSettings;
import lk.supervision.doctermaster.print.UnicodeFormatter;

public class SettingsFragment extends Fragment implements Runnable {

    private View settingsFragment;
    private DocterMasterDbHelper docterMasterDbHelper;
    private MSettings mSettings;

    //bluetooth
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothAdapter mBluetoothAdapter;

    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressDialog mBluetoothConnectProgressDialog;

    @BindView(R.id.btn_bluetooth_scan)
    Button btnBluetoothScan;

    @BindView(R.id.btn_bluetooth_test)
    Button btnBluetoothTest;

    @BindView(R.id.btn_save_settings)
    Button btnSaveSettings;

    @BindView(R.id.text_settings_bluetooth_printer)
    TextView textSettingsBluetoothPrinter;

    @BindView(R.id.text_settings_bluetooth_printer_mac)
    TextView textSettingsBluetoothPrinterMac;

    @BindView(R.id.text_settings_center_name)
    EditText textSettingsCenteName;

    @BindView(R.id.text_settings_center_address)
    EditText textSettingsCenterAddress;

    @BindView(R.id.text_settings_center_contact_no)
    EditText textSettingsContactNo;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settingsFragment = inflater.inflate(R.layout.fragment_settings, container, false);

        //butter knife componet
        ButterKnife.bind(this, settingsFragment);

        docterMasterDbHelper = new DocterMasterDbHelper(settingsFragment.getContext());

        mSettings = new MSettings();
        long mSettingsCount = docterMasterDbHelper.mSettingsCount();
        if (mSettingsCount > 0) {
            mSettings = docterMasterDbHelper.getMSettings();
            textSettingsBluetoothPrinter.setText(mSettings.getBluetoothPrinter());
            textSettingsBluetoothPrinterMac.setText(mSettings.getBluetoothPrinterMac());
            textSettingsCenteName.setText(mSettings.getCenterName());
            textSettingsCenterAddress.setText(mSettings.getCenterAddress());
            textSettingsContactNo.setText(mSettings.getCenterContactNo());
        }

        btnBluetoothScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    Toast.makeText(settingsFragment.getContext(), "Message1", Toast.LENGTH_SHORT).show();
                } else {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        ListPairedDevices();
                        Intent connectIntent = new Intent(getContext(), DeviceListActivity.class);
                        startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                    }
                }
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (textSettingsBluetoothPrinter.getText().toString().isEmpty()) {
                    textSettingsBluetoothPrinter.setError("PLEASE SELECT BLUETOOTH PRINTER");
                } else if (textSettingsBluetoothPrinterMac.getText().toString().isEmpty()) {
                    textSettingsBluetoothPrinterMac.setError("PLEASE SELECT BLUETOOTH PRINTER");
                } else if (textSettingsCenteName.getText().toString().isEmpty()) {
                    textSettingsCenteName.setError("PLEASE SELECT BLUETOOTH PRINTER");
                } else if (textSettingsCenterAddress.getText().toString().isEmpty()) {
                    textSettingsCenterAddress.setError("PLEASE SELECT BLUETOOTH PRINTER");
                } else if (textSettingsContactNo.getText().toString().isEmpty()) {
                    textSettingsContactNo.setError("PLEASE SELECT BLUETOOTH PRINTER");
                } else {
                    mSettings.setBluetoothPrinter(textSettingsBluetoothPrinter.getText().toString());
                    mSettings.setBluetoothPrinterMac(textSettingsBluetoothPrinterMac.getText().toString());
                    mSettings.setCenterName(textSettingsCenteName.getText().toString());
                    mSettings.setCenterAddress(textSettingsCenterAddress.getText().toString());
                    mSettings.setCenterContactNo(textSettingsContactNo.getText().toString());

                    int updateMSettings = docterMasterDbHelper.updateMSettings(mSettings);
                    if (updateMSettings > 0) {
                        AppEnvironmentValues.snackbarCustome(settingsFragment, "PRINT SUCCESS", "SUCCESS");
                    } else {
                        AppEnvironmentValues.snackbarCustome(settingsFragment, "PRINT ERROR", "ERROR");
                    }
                }
            }
        });

        btnBluetoothTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deafaulBluetoothPrinter = docterMasterDbHelper.getDeafaulBluetoothPrinter();
                mSettings = docterMasterDbHelper.getMSettings();
                mBluetoothSocket = getBlueToothPrinter(deafaulBluetoothPrinter);
                SharedPreferences sharedPreferences = settingsFragment.getContext().getSharedPreferences("SVDOCTERMASTER", 0);
                final String loginDocterName = sharedPreferences.getString("LOGIN_DOCTER_NAME", "NULL");
                final String loginLocationName = sharedPreferences.getString("LOCATION_NAME", "NULL");
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
                                BILL = BILL + "APPOYMENT NO  - 01\n";
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
                            AppEnvironmentValues.snackbarCustome(settingsFragment, "PRINT BILL", "SUCCESS");
                            closeSocket(mBluetoothSocket);
                        }
                    };
                    t.start();
                } else {
                    AppEnvironmentValues.snackbarCustome(settingsFragment, "PRINT ERROR", "ERROR");
                }
            }
        });
        return settingsFragment;
    }

    private boolean checklueToothDevice() {
        try {
            if (mBluetoothDevice.getName() != null) {
                textSettingsBluetoothPrinter.setText(mBluetoothDevice.getName());
                textSettingsBluetoothPrinterMac.setText(mBluetoothDevice.getAddress());
                return true;
            }
        } catch (NullPointerException ex) {
            textSettingsBluetoothPrinter.setText("PLEASE SELECT BLUETOOTH PRINTER");
            textSettingsBluetoothPrinterMac.setText("PLEASE SELECT BLUETOOTH PRINTER");
        }
        return false;
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
            AppEnvironmentValues.snackbarCustome(settingsFragment, "PRINT ERROR PLEASE RE CONFIG PRINTER", "ERROR");
        }
        Thread mBlutoothConnectThread = new Thread(this);
        mBlutoothConnectThread.start();
        return tempBluetoothSocket;
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.d("PRINT ACTIVITY", e.toString());
        }
    }

    public void onActivityResult(int mRequestCode, int mResultCode, Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);
        switch (mRequestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    Log.v("PRINT ACTIVITY", "Coming incoming address " + mDeviceAddress);
                    mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(settingsFragment.getContext(), "Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, false);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();
                }
                break;

            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    Intent connectIntent = new Intent(settingsFragment.getContext(), DeviceListActivity.class);
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(settingsFragment.getContext(), "Message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v("PRINT ACTIVITY", "PairedDevices: " + mDevice.getName() + "  " + mDevice.getAddress());
            }
        }
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
            //closeSocket(mBluetoothSocket);
            //return;
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
            checklueToothDevice();
            Toast.makeText(settingsFragment.getContext(), "BlueTooth Printer Device Connected", Toast.LENGTH_SHORT).show();
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
}
