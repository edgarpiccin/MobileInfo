package simpress.mobileinfo;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
}
