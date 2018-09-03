package devindow.AndroidGolf;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class AndroidGolf extends Activity {
	
    private CoursesDbAdapter mDbHelper;

    private LocationManager locationManager;
    private LocationIntentReceiver intentReceiver = new LocationIntentReceiver();
    private static final String LOCATION_CHANGED_ACTION = new String("android.intent.action.LOCATION_CHANGED");     
    private IntentFilter intentFilter = new IntentFilter(LOCATION_CHANGED_ACTION);
	private Intent intent = new Intent(LOCATION_CHANGED_ACTION);

	private Location driveFrom = null;
	private long courseID = -1;
	private int hole = 1;
	private Location greenLocation;
	
	
	// OnCreate
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        
        mDbHelper = new CoursesDbAdapter(this);
        mDbHelper.open();
        fillCourses();
        
        
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        registerReceiver(intentReceiver, intentFilter);

        
        final Button btnMeasureDrive = (Button) findViewById(R.id.measureDrive);
        btnMeasureDrive.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
                final TextView txtDrive = (TextView) findViewById(R.id.driveDistance);
        		
        		if (driveFrom == null) {
        			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        			driveFrom = locationManager.getCurrentLocation("gps");
        			txtDrive.setText("0 yards");
        		}
        		else {
        			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        			Location currentLocation = locationManager.getCurrentLocation("gps");
        			if (driveFrom.distanceTo(currentLocation) < 1)
            			txtDrive.setText("");

        			driveFrom = null;
        		}
        	}
        });
        
        final Spinner spnCourse = (Spinner) findViewById(R.id.courseName);
        spnCourse.setOnItemSelectedListener(new OnItemSelectedListener() { 
            public void onItemSelected(AdapterView parent, View v, int position, long id) {
            	courseID = spnCourse.getSelectedItemId();
    			setHole(1);
            }
    
            public void onNothingSelected(AdapterView parent) {
            	courseID = -1;
              }
        });
        
        final Button btnIncreaseHole = (Button) findViewById(R.id.increaseHole);
        btnIncreaseHole.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		if (hole < 18) 
        			setHole(hole+1);
        	}
        });
        
        final Button btnDecreaseHole = (Button) findViewById(R.id.decreaseHole);
        btnDecreaseHole.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		if (hole > 1) 
        			setHole(hole-1);
        	} 
        });
    }
    
    private void setHole(int holeNumber) {
    	hole = holeNumber;
        final TextView txtHole = (TextView) findViewById(R.id.holeNumber);
        txtHole.setText(String.format("Hole %d", hole));   	

        if (courseID < 0)
    		return;
    	
		Cursor cursor = mDbHelper.fetchHole(courseID, hole);
		String holeLat = cursor.getString(1);
		String holeLng = cursor.getString(2);
		if (holeLat == null || holeLng == null) 
			greenLocation = null;
		else {
			greenLocation = new Location();
			greenLocation.setLatitude(Double.parseDouble(holeLat));
			greenLocation.setLongitude(Double.parseDouble(holeLng));
		}
		
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location currentLocation = locationManager.getCurrentLocation("gps");
		setYardage(currentLocation);
    }
    
    
    // Location Changed
    public class LocationIntentReceiver extends IntentReceiver{
    	@Override
    	public void onReceiveIntent(Context context, Intent intent) {
			//Location currentLocation = (Location)intent.getExtra("location");
			Location currentLocation = locationManager.getCurrentLocation("gps");

			if (driveFrom != null) {
    			double driveDistance = driveFrom.distanceTo(currentLocation) * 1.0936133;
                final TextView txtDrive = (TextView) findViewById(R.id.driveDistance);
                if (driveDistance > 500) {
                	driveFrom = null;
                	txtDrive.setText("Far");
                }
                else
                	txtDrive.setText(String.format("%.0f yards", driveDistance));
    		}
			
			setYardage(currentLocation);
    	}
    }
    
    private void setYardage(Location currentLocation) {
        final TextView txtYardage = (TextView) findViewById(R.id.yardage);

        if (courseID < 0)
        	txtYardage.setText("");
        else if (greenLocation == null)
        	txtYardage.setText("No Loc");
        else {
			double yardage = greenLocation.distanceTo(currentLocation) * 1.0936133;
            if (yardage > 999) {
            	txtYardage.setText("Far");
            }
            else
            	txtYardage.setText(String.format("%.0f yards", yardage));
		}
    }
    
    
    // Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean supRetVal = super.onCreateOptionsMenu(menu);
		menu.add(0, 0, "New Course");
		menu.add(0, 1, "Update Green Center");
		menu.add(0, 2, "Delete Course");
		return supRetVal;
	}
	
	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
			case 0: // New course
    	    	final Dialog dialog = new Dialog(this);
    	     	dialog.setContentView(R.layout.input_dialog);
    	    	dialog.setTitle("Course Name");
    	        Button button = (Button) dialog.findViewById(R.id.ok);
    	        button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
            	        EditText editText = (EditText) dialog.findViewById(R.id.name);
           	        	long result = mDbHelper.createCourse(editText.getText().toString());
           	        	fillCourses();
                    	dialog.dismiss();
                    }
                });
    	    	dialog.show();
				return true;
				
			case 1: // Update green Center
    			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    			greenLocation = locationManager.getCurrentLocation("gps");
    			mDbHelper.updateHole(courseID, hole, greenLocation.getLatitude(), greenLocation.getLongitude());
    	        final TextView txtYardage = (TextView) findViewById(R.id.yardage);
    	        txtYardage.setText("0 yards");
				return true;
				
			case 2: // Delete Course
		        final Spinner spnCourse = (Spinner) findViewById(R.id.courseName);
    	        mDbHelper.deleteCourse(spnCourse.getSelectedItemId());
    	        fillCourses();
    	        return true;
		}
		return false;
	}
    
	
    // Fill Spinner from DB
    private void fillCourses() {
         // Get all of the rows from the database and create the item list
        Cursor coursesCursor = mDbHelper.fetchAllCourses();
        startManagingCursor(coursesCursor);
        
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{CoursesDbAdapter.KEY_COURSE};
        
        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.course};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter courses = new SimpleCursorAdapter(this, R.layout.locations_row, coursesCursor, from, to);

        Spinner spinnerTo = (Spinner) findViewById(R.id.courseName);
        spinnerTo.setAdapter(courses);
    }

    
    // onResume, onFreeze, & onDestroy
    @Override 
    public void onResume() { 
    	super.onResume(); 
		List<LocationProvider> providers = locationManager.getProviders();
		LocationProvider locationProvider = providers.get(0);
		locationManager.requestUpdates(locationProvider, 0, 0, intent);
    }
    
    @Override 
    public void onFreeze(Bundle icicle) { 
    	locationManager.removeUpdates(intent); 
    	super.onFreeze(icicle); 
    } 

    @Override 
    public void onDestroy() { 
    	unregisterReceiver(intentReceiver); 
    	super.onDestroy(); 
    } 
}