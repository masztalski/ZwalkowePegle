package fantomit.zwalkowepegle.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.DBmodels.Settings;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    // name of the database file for your application -- change to something
    // appropriate for your app
    private static final String DATABASE_NAME = "DOLNOSLASKIE.db";
    // any time you make changes to your database objects, you may have to
    // increase the database version
    private static final int DATABASE_VERSION = 14;

    // the DAO object we use to access the SimpleData table
    //private Dao<City, Integer > simpleDao = null;
    //private RuntimeExceptionDao<City_db, String> simpleRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

        Log.i(DatabaseHelper.class.getName(), "onCreate");
        try {
            TableUtils.createTable(connectionSource, River.class);
            TableUtils.createTable(connectionSource, Station.class);
            TableUtils.createTable(connectionSource, Settings.class);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(DatabaseHelper.class.getName(), "onUpgrade");
        try {
            // if you need to create the 'cities' table make this call
            TableUtils.dropTable(connectionSource, River.class, true);
            TableUtils.dropTable(connectionSource, Station.class, true);
            TableUtils.dropTable(connectionSource, Settings.class, true);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        onCreate(database, connectionSource);
    }
}
