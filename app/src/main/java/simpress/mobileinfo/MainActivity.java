package simpress.mobileinfo;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView imeiString = (TextView) findViewById(R.id.textView);
		imeiString.setText(getIMEI());

		Intent it = new Intent(MainActivity.this, ServicoTest.class);
		startService(it);
	}
	
	public void startService(View view){
		Intent it = new Intent(MainActivity.this, ServicoTest.class);
		startService(it);
	}
	
	public void stopService(View view){
		Intent it = new Intent(MainActivity.this, ServicoTest.class);
		stopService(it);
	}

	private String getIMEI() {
		TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return tel.getDeviceId();
	}
}
