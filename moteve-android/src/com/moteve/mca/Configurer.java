package com.moteve.mca;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Configurer extends Activity {
    
    private static final String TAG = "Moteve_Configurer";
    public static Activity CONF_ACTIVITY;
    
    private Spinner videoPermissionsSpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.config_basic);
	final Context ctx = this;
	CONF_ACTIVITY = this;
	
	Button connectionSettingsButton = (Button) findViewById(R.id.configConnection);
	connectionSettingsButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Intent intent = new Intent(ctx, ConnectionSettings.class);
		startActivity(intent);
	    }
	});
	
	Button okButton = (Button) findViewById(R.id.saveConfig);
	okButton.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		// TODO save the permissions
		finish();
	    }
	});
	
	videoPermissionsSpinner = (Spinner) findViewById(R.id.videoPermissionsSpinner);
	refreshPermissionGroups();
    }

    private void refreshPermissionGroups() {
	ArrayAdapter adapter = new ArrayAdapter(this,
		android.R.layout.simple_spinner_item,
		getGroups());
	videoPermissionsSpinner.setAdapter(adapter);
    }

    /**
     * 
     * @return names of the Groups the user has created on the server
     */
    private String[] getGroups() {
	SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
	String serverUrl = prefs.getString("serverUrl", null);
	String token = prefs.getString("token", null);
	if (serverUrl == null || serverUrl.length() == 0
		|| token == null || token.length() == 0) {
	    return new String[] {"Connection not configured"};
	}
	
	
	try {
	    return retrieveGroups(serverUrl, token);
	} catch (IOException e) {
	    Log.e(TAG, "Error retrieving group names", e);
	    return new String[] {"Error connecting to server"};
	}
    }
    
    private String[] retrieveGroups(String serverUrl, String token)
	    throws IOException {
	String groupsUrl = serverUrl + "/mca/listGroups.htm";
	URL url = new URL(groupsUrl);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setDoInput(true);
	conn.setDoOutput(true);
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Cache-Control", "no-cache");
	conn.setRequestProperty("Pragma", "no-cache");
	conn.setRequestProperty("Moteve-Token", token);

	String groupsNames = ConnectionUtils.receiveResponse(conn);
	Log.i(TAG, "Obtained group names='" + groupsNames + "'");
	conn.disconnect();

	return groupsNames.split("\\\\"); // just one backslash: escaped for java & for regexp
    }

    @Override
    protected void onResume() {
	super.onResume();
	refreshPermissionGroups();
    }
    
    

}
