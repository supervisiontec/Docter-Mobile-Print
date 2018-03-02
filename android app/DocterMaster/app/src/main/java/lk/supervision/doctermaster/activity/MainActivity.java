package lk.supervision.doctermaster.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lk.supervision.doctermaster.R;
import lk.supervision.doctermaster.common.AppEnvironmentValues;
import lk.supervision.doctermaster.common.RestApiRecever;
import lk.supervision.doctermaster.database.DocterMasterDbHelper;
import lk.supervision.doctermaster.model.DoctorAndLocation;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.text_user_name)
    EditText textUserName;

    @BindView(R.id.text_password)
    EditText textPassowrd;

    @BindView(R.id.sppiner_docter_location)
    Spinner sppinerDocterLocation;

    private DocterMasterDbHelper docterMasterDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        this.docterMasterDbHelper = new DocterMasterDbHelper(this);

    }

    @OnClick(R.id.btn_get_location)
    public void getLocations(View view) {
        if (textUserName.getText().toString().isEmpty()) {
            textUserName.setError("PLEASE ENTER USER NAME");
        } else if (textPassowrd.getText().toString().isEmpty()) {
            textPassowrd.setError("PLEASE ENTER PASSWORD");
        } else if (!textUserName.getText().toString().isEmpty() && !textPassowrd.getText().toString().isEmpty()) {
            if (isNetworkConnected()) {
                try {
                    RestApiRecever restApiRecever = new RestApiRecever();
                    DoctorAndLocation[] docterAndLocationDetails = restApiRecever.getDocterAndLocationDetails(textUserName.getText().toString(), textPassowrd.getText().toString(), AppEnvironmentValues.SIMPLE_DATE_FORMAT.format(new Date()));
                    if (docterAndLocationDetails.length > 0) {
                        List<DoctorAndLocation> locationList = new ArrayList<>(Arrays.asList(docterAndLocationDetails));
                        ArrayAdapter<DoctorAndLocation> locationAdepter = new ArrayAdapter<DoctorAndLocation>(getApplicationContext(), android.R.layout.select_dialog_item, locationList);
                        locationAdepter.setDropDownViewResource(android.R.layout.select_dialog_item);
                        sppinerDocterLocation.setAdapter(locationAdepter);
                    } else {
                        AppEnvironmentValues.snackbarCustome(view, "LOCATIONS NOT FOUND", "ERROR");
                    }
                } catch (Exception ex) {
                    AppEnvironmentValues.snackbarCustome(view, "SERVER NOT CONNECT", "ERROR");
                }
            } else {
                AppEnvironmentValues.snackbarCustome(view, "PLEASE CONNECT INTERNET", "ERROR");
            }
        }
    }

    @OnClick(R.id.btn_login)
    public void login(View view) {
        if (textUserName.getText().toString().isEmpty()) {
            textUserName.setError("PLEASE ENTER USER NAME");
        } else if (textPassowrd.getText().toString().isEmpty()) {
            textPassowrd.setError("PLEASE ENTER PASSWORD");
        } else if (sppinerDocterLocation.getSelectedItem() == null) {
            AppEnvironmentValues.snackbarCustome(view, "PLEASE SELECT LOCATION", "ERROR");
        } else if (!textUserName.getText().toString().isEmpty() && !textPassowrd.getText().toString().isEmpty() && (sppinerDocterLocation.getSelectedItem() != null)) {
            if (isNetworkConnected()) {
                //SharedPreferences set user data
                SharedPreferences pref = getApplicationContext().getSharedPreferences("SVDOCTERMASTER", 0);
                SharedPreferences.Editor editor = pref.edit();
                DoctorAndLocation selectDoctorAndLocation = (DoctorAndLocation) sppinerDocterLocation.getSelectedItem();
                editor.putInt("LOGIN_DOCTER_ID", selectDoctorAndLocation.getDoctorIndexNo());
                editor.putString("LOGIN_DOCTER_NAME", selectDoctorAndLocation.getDoctor());
                editor.putInt("LOCATION_ID", selectDoctorAndLocation.getLocationIndexNo());
                editor.putString("LOCATION_NAME", selectDoctorAndLocation.getLocation());
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                startActivity(intent);
            } else {
                AppEnvironmentValues.snackbarCustome(view, "PLEASE CONNECT INTERNET", "ERROR");
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
