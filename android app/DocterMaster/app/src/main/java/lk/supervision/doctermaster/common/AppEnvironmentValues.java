package lk.supervision.doctermaster.common;

import android.app.ActivityManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.supervision.doctermaster.R;

/**
 * Created by kavish manjitha
 */

public class AppEnvironmentValues {

    //SETTINGS
    private static Context context;
    public static final Integer BRANCH = 5;
    public static final String APP_VERSION = "2";

    public static final String MAIN_SERVER_ADDRESS = "http://123.231.11.177:8070";
    //public static final String MAIN_SERVER_ADDRESS = "http://192.168.7.105:8080";

    public static final SimpleDateFormat SIMPLE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SIMPLE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss a");


    public static String getSystemDateTimeFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(System.currentTimeMillis());
    }

    public static String getSystemDateTimeFormat(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static Date getSystemDateTimeParse(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static Date getSystemDateParse(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static void snackbarCustome(View view, String message, String type) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        if ("ERROR".equals(type)) {
            snackBarView.setBackgroundColor(view.getResources().getColor(R.color.error));
        } else {
            snackBarView.setBackgroundColor(view.getResources().getColor(R.color.right));
        }
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(view.getResources().getColor(R.color.whiteColor));
        snackbar.show();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
