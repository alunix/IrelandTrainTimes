package ie.markomeara.irelandtraintimes.trains;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ie.markomeara.irelandtraintimes.db.DBManager;
import ie.markomeara.irelandtraintimes.db.DBNotAvailableException;

/**
 * Created by Mark on 19/10/2014.
 */
public class StationsDataSource {

    private static final String TAG = StationsDataSource.class.getSimpleName();

    private SQLiteDatabase db;
    private DBManager dbManager;
    private String[] allColumns = { DBManager.COLUMN_ID, DBManager.COLUMN_STN_NAME,
            DBManager.COLUMN_STN_ALIAS , DBManager.COLUMN_STN_LAT, DBManager.COLUMN_STN_LONG,
            DBManager.COLUMN_STN_CODE, DBManager.COLUMN_STN_FAV};

    public StationsDataSource(Context context){
        dbManager = new DBManager(context);
    }

    public void open() throws SQLException, DBNotAvailableException {
        if(dbManager != null) {
            db = dbManager.getWritableDatabase();
        }
        else{
            throw new DBNotAvailableException("dbManager has not been initialised");
        }
    }

    public void close() {
        dbManager.close();
    }

    public boolean updateStoredStations(List<Station> stationList){

        boolean overallSuccess = true;

        for(Station station : stationList){

            boolean stationSuccess = storeStation(station);
            if(!stationSuccess){
                overallSuccess = false;
            }

        }

        return overallSuccess;
    }

    /**
     * Stores station in SQLite DB
     *
     * @param stationToStore
     * @return false if station isn't entered in DB. This can happen if station with same name already exists.
     */
    public boolean storeStation(Station stationToStore) {

        boolean success = false;

        StringBuilder stationInsertQuery = new StringBuilder();
        stationInsertQuery.append("INSERT OR REPLACE INTO ").append(DBManager.TABLE_STATIONS).append(" ");
        stationInsertQuery.append("(");
        stationInsertQuery.append(DBManager.COLUMN_ID).append(",");
        stationInsertQuery.append(DBManager.COLUMN_STN_NAME).append(",");
        stationInsertQuery.append(DBManager.COLUMN_STN_ALIAS).append(",");
        stationInsertQuery.append(DBManager.COLUMN_STN_LAT).append(",");
        stationInsertQuery.append(DBManager.COLUMN_STN_LONG).append(",");
        stationInsertQuery.append(DBManager.COLUMN_STN_CODE).append(",");
        stationInsertQuery.append(DBManager.COLUMN_STN_FAV);
        stationInsertQuery.append(") ");
        stationInsertQuery.append("VALUES ( ?, ?, ?, ?, ?, ?, (");
        stationInsertQuery.append("SELECT ").append(DBManager.COLUMN_STN_FAV);
        stationInsertQuery.append(" FROM ").append(DBManager.TABLE_STATIONS);
        stationInsertQuery.append(" WHERE ").append(DBManager.COLUMN_ID).append(" = ?");
        stationInsertQuery.append(")");
        stationInsertQuery.append(")");

        String[] queryArgs = new String[7];
        queryArgs[0] = "" + stationToStore.getId();
        queryArgs[1] = stationToStore.getName();
        queryArgs[2] = stationToStore.getAlias();
        queryArgs[3] = "" + stationToStore.getLatitude();
        queryArgs[4] = "" + stationToStore.getLongitude();
        queryArgs[5] = stationToStore.getCode();
        queryArgs[6] = "" + stationToStore.getId();

        try {
            db.rawQuery(stationInsertQuery.toString(), queryArgs);
            Cursor cursor = db.query(DBManager.TABLE_STATIONS, allColumns, DBManager.COLUMN_ID + " = " + stationToStore.getId(), null, null, null, null);

            if (cursor.moveToFirst()) {
                Station updatedStation = cursorToStation(cursor);
                if(updatedStation != null){
                    success = true;
                }
            }
            cursor.close();
        } catch(SQLiteConstraintException ex){
            Log.e(TAG, ex.getMessage(), ex);
        }
        return success;
    }

    public Station updateFavourite(Station stn, boolean fav){
        ContentValues cv = new ContentValues();
        cv.put(DBManager.COLUMN_STN_FAV, false);

        db.update(DBManager.TABLE_STATIONS, cv, DBManager.COLUMN_ID + " = " + stn.getId(), null);

        Cursor cursor = db.query(DBManager.TABLE_STATIONS, allColumns, DBManager.COLUMN_ID + " = " + stn.getId(), null, null, null, null);

        cursor.moveToFirst();
        Station updatedStation = cursorToStation(cursor);
        cursor.close();
        return updatedStation;
    }

    public List<Station> retrieveAllStations(){

        // Ordering by name
        Cursor cursor = db.query(DBManager.TABLE_STATIONS, allColumns, null, null, null, null, DBManager.COLUMN_STN_NAME);
        List<Station> stns = new ArrayList<Station>();

        while(cursor.moveToNext()){
            stns.add(cursorToStation(cursor));
        }

        return stns;
    }

    public Station retrieveStationById(long id){

        Cursor cursor = db.query(DBManager.TABLE_STATIONS, allColumns, DBManager.COLUMN_ID +" = "+ id, null, null, null, null);
        cursor.moveToFirst();
        return cursorToStation(cursor);

    }

    public void clearAllStations(){
        db.beginTransaction();
        db.delete(DBManager.TABLE_STATIONS, null, null);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private Station cursorToStation(Cursor cursor){

        int id = cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndex(DBManager.COLUMN_STN_NAME));
        String alias = cursor.getString(cursor.getColumnIndex(DBManager.COLUMN_STN_ALIAS));
        double latitude = cursor.getDouble(cursor.getColumnIndex(DBManager.COLUMN_STN_LAT));
        double longitude = cursor.getDouble(cursor.getColumnIndex(DBManager.COLUMN_STN_LONG));
        String code = cursor.getString(cursor.getColumnIndex(DBManager.COLUMN_STN_CODE));
        boolean fav = (cursor.getInt(cursor.getColumnIndex(DBManager.COLUMN_STN_FAV)) > 0);

        Station stn = new Station(id, name, alias, latitude, longitude, code, fav);

        Log.i(TAG, "Returning station with id " + id);
        return stn;
    }
}