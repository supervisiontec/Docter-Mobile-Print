package lk.supervision.doctermaster.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lk.supervision.doctermaster.R;
import lk.supervision.doctermaster.model.MSettings;

/**
 * Created by kavish manjitha on 2/8/2018.
 */

public class DocterMasterDbHelper extends SQLiteOpenHelper {

    //database
    public static final String DATABASE_NAME = "docter_master.db";
    public static final int DATABASE_VERSION = 1;

    //tables
    private static final String DB_M_SETTINGS = "m_settings";

    private Context context;

    public DocterMasterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        //onUpgrade(getReadableDatabase(), 1, 1);
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {

        return super.getReadableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {

        return super.getWritableDatabase();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

    }

    @Override
    public synchronized void close() {
        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.database);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        StringBuilder builder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String rawSql = builder.toString();

            String[] sqls = rawSql.split(";");
            for (String sql : sqls) {
                db.execSQL(sql);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + DB_M_SETTINGS);
        //recreate database
        onCreate(db);
    }

    //----------------------------------------------------------------------------------------------
    //SETTINGS
    //----------------------------------------------------------------------------------------------

    public int updateMSettings(MSettings mSettings) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues transactionValues = new ContentValues();
        transactionValues.put("bluetooth_printer", mSettings.getBluetoothPrinter());
        transactionValues.put("bluetooth_printer_mac", mSettings.getBluetoothPrinterMac());
        transactionValues.put("center_name", mSettings.getCenterName());
        transactionValues.put("center_address", mSettings.getCenterAddress());
        transactionValues.put("center_contact_no", mSettings.getCenterContactNo());
        transactionValues.put("footer", mSettings.getFooter());
        return (int) db.update(DB_M_SETTINGS, transactionValues, "index_no = ?", new String[]{mSettings.getIndexNo().toString()});
    }

    public MSettings getMSettings() {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT * FROM " + DB_M_SETTINGS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        MSettings mSettings = new MSettings();
        while (!cursor.isAfterLast()) {
            mSettings.setIndexNo(cursor.getInt(cursor.getColumnIndex("index_no")));
            mSettings.setBluetoothPrinter(cursor.getString(cursor.getColumnIndex("bluetooth_printer")));
            mSettings.setBluetoothPrinterMac(cursor.getString(cursor.getColumnIndex("bluetooth_printer_mac")));
            mSettings.setCenterName(cursor.getString(cursor.getColumnIndex("center_name")));
            mSettings.setCenterAddress(cursor.getString(cursor.getColumnIndex("center_address")));
            mSettings.setCenterContactNo(cursor.getString(cursor.getColumnIndex("center_contact_no")));
            mSettings.setFooter(cursor.getString(cursor.getColumnIndex("footer")));
            cursor.moveToNext();
        }
        cursor.close();
        return mSettings;
    }

    public String getDeafaulBluetoothPrinter() {
        SQLiteDatabase db = getReadableDatabase();
        String selectQuery = "SELECT * FROM " + DB_M_SETTINGS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String deafaulBluetoothPrinter = "";
        while (!cursor.isAfterLast()) {
            deafaulBluetoothPrinter = cursor.getString(cursor.getColumnIndex("bluetooth_printer_mac"));
            cursor.moveToNext();
        }
        cursor.close();
        return deafaulBluetoothPrinter;
    }

    public long mSettingsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long cnt = DatabaseUtils.queryNumEntries(db, DB_M_SETTINGS);
        db.close();
        return cnt;
    }


}
