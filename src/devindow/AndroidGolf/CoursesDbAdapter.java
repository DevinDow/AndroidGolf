package devindow.AndroidGolf;

import java.io.FileNotFoundException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple Courses database access helper class. 
 * Defines the basic CRUD operations for the Android Golf application, 
 * and gives the ability to list all Courses as well as
 * retrieve or modify a specific Course.
 */
public class CoursesDbAdapter {

    public static final String KEY_ROWID="_id";
    public static final String KEY_COURSE="course";
    public static final String KEY_LAT="lat";
    public static final String KEY_LNG="lng";
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table courses (" +
        "_id integer primary key autoincrement, " +
        "course text not null, " +
    	"lat1 text, lng1 text, " +
    	"lat2 text, lng2 text, " +
    	"lat3 text, lng3 text, " +
    	"lat4 text, lng4 text, " +
    	"lat5 text, lng5 text, " +
    	"lat6 text, lng6 text, " +
    	"lat7 text, lng7 text, " +
    	"lat8 text, lng8 text, " +
    	"lat9 text, lng9 text, " +
    	"lat10 text, lng10 text, " +
    	"lat11 text, lng11 text, " +
    	"lat12 text, lng12 text, " +
    	"lat13 text, lng13 text, " +
    	"lat14 text, lng14 text, " +
    	"lat15 text, lng15 text, " +
    	"lat16 text, lng16 text, " +
    	"lat17 text, lng17 text, " +
    	"lat18 text, lng18 text);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "courses";
    private static final int DATABASE_VERSION = 2;

    private SQLiteDatabase mDb;
    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be opened/created
     * @param ctx the Context within which to work
     */
    public CoursesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the Courses database. If it cannot be opened, try to create a new instance of
     * the database. If it cannot be created, throw an exception to signal the failure
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public CoursesDbAdapter open() throws SQLException {
        try {
            mDb = mCtx.openDatabase(DATABASE_TABLE, null);
        } catch (FileNotFoundException e) {
            try {
                mDb = mCtx.createDatabase(DATABASE_TABLE, DATABASE_VERSION, 0, null);
                mDb.execSQL(DATABASE_CREATE);
            } 
            catch (FileNotFoundException e1) {
                throw new SQLException("Could not create database");
            }
        }
        return this;
    }

    public void close() {
        mDb.close();
    }

    /**
     * Create a new Courses using the name, latitude, and longitude provided. 
     * If the Course is successfully created
     * 	return the new rowId for that , 
     * 	otherwise return a -1 to indicate failure.
     * @param course the name of the Course
     * @return rowId or -1 if failed
     */
    public long createCourse(String course) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_COURSE, course);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the Course with the given rowId
     * @param rowId id of Course to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCourse(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all Courses in the database
     * @return Cursor over all Courses
     */
    public Cursor fetchAllCourses() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_COURSE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the Course that matches the given rowId
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching Course, if found
     * @throws SQLException if Course could not be found/retrieved
     */
    public Cursor fetchHole(long rowId, int hole) throws SQLException {
    	String holeLat = String.format("%s%d", KEY_LAT, hole);
    	String holeLng = String.format("%s%d", KEY_LNG, hole);
        Cursor result = mDb.query(true, DATABASE_TABLE, 
        		new String[] {KEY_ROWID, holeLat, holeLng}, 
        		KEY_ROWID + "=" + rowId, 
        		null, null, null, null);
        if ((result.count() == 0) || !result.first()) {
            throw new SQLException("No Course matching ID: " + rowId);
        }
        return result;
    }

    /**
     * Update the note using the details provided. The note to be updated is specified using
     * the rowId, and it is altered to use the title and body values passed in
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateHole(long rowId, int hole, double lat, double lng) {
    	String holeLat = String.format("%s%d", KEY_LAT, hole);
    	String holeLng = String.format("%s%d", KEY_LNG, hole);
        ContentValues args = new ContentValues();
        args.put(holeLat, Double.toString(lat));
        args.put(holeLng, Double.toString(lng));
        return mDb.update(DATABASE_TABLE, args, 
        		KEY_ROWID + "=" + rowId, null) > 0;
    }
}
