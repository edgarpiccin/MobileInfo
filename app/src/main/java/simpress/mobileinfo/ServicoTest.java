package simpress.mobileinfo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class ServicoTest extends Service {
	public List<Worker> threads = new ArrayList<Worker>();
	public String latitudeStr, longitudeStr, imeiString;
    private DatabaseHelper helper;

    @Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		Log.i("Script", "onCreate()");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i("Script", "onStartCommand()");

		/*
		Worker w = new Worker(startId);
		w.start();
		threads.add(w);
		*/

		LocationManager locationManager;
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Listener listener = new Listener();

		long tempoAtualizacao = 0;
		float distancia = 0;

		if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION))
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempoAtualizacao, distancia, listener);

		return(super.onStartCommand(intent, flags, startId));
		// START_NOT_STICKY
		// START_STICKY
		// START_REDELIVER_INTENT
	}

	public boolean checkPermission(String permission) {
		int res = this.checkCallingOrSelfPermission(permission);
		return (res == PackageManager.PERMISSION_GRANTED);
	}

    private void saveLocation() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        String dataHora = sdf.format(new Date());

        values.put("imei", imeiString);
        values.put("latitude", latitudeStr);
        values.put("longitude", longitudeStr);
        values.put("data", dataHora);//"%Y-%m-%d %H:%M:%S"));

        db.insert("ImeiLocationHistory", null, values);
    }

    private void dropLocation() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("ImeiLocationHistory", "", null);
    }

    private ArrayList<ImeiLocation> getSavedLocation() {
        SQLiteDatabase db1 = helper.getReadableDatabase();

        ArrayList<ImeiLocation> itensImei = new ArrayList<ImeiLocation>();

        Cursor cursor = db1.rawQuery("select _id, imei, latitude, longitude, data from ImeiLocationHistory", null);
        cursor.moveToFirst();

        if (cursor.getCount() >= 1) {
            Toast.makeText(this, "IMEI: " + cursor.getString(0), Toast.LENGTH_SHORT).show();

            for (int i = 0; i < cursor.getCount(); i++) {
                ImeiLocation item = new ImeiLocation(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

                itensImei.add(item);

                cursor.moveToNext();
            }
        }

        cursor.close();
        return itensImei;
    }

    public static boolean Conectado(Context context) {

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        String LogSync = null;
        String LogToUserTitle = null;

        NetworkInfo[] a = cm.getAllNetworkInfo();
        NetworkInfo networkInfo;
        boolean x = false;

        for (NetworkInfo myNet : a) {
            //networkInfo = cm.getNetworkInfo(myNet);
            if (myNet.getState().equals(NetworkInfo.State.CONNECTED)) {
                x = true;
                return x;
            } else {
                x = false;
            }
        }
        return x;

    }

    public class ImeiLocation extends ArrayList {
        private int _id;
        private String _imei;
        private String _latitude;
        private String _longitude;
        private String _data;


        public ImeiLocation(int id, String imei, String latitude, String longitude, String data) {
            _id = id;
            _imei = imei;
            _latitude = latitude;
            _longitude = longitude;
            _data = data;
        }
    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("app_token", "h7l6cCeFM79t");
                conn.addRequestProperty("client_id", "zbA3yQmjcC8f");
                conn.addRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(300000);

                // para activar el metodo post
                conn.setDoOutput(true);
                conn.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                InputStream is = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();
            } catch (Exception e) {
                Log.e("Erro API", e.toString());

            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //this method will be running on UI thread

        }
    }

    private void getIMEI() {
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        /*
        if (tel.getPhoneCount() > 1)
            imeiString = tel.getDeviceId(0);
        else
        */
            imeiString = tel.getDeviceId();
    }

    private String generateJson(ImeiLocation imei) {
        String json = "{" + "\"" + "IMEI" + "\"" + ":" + "\"" + imei._imei + "\"" + "," + "\"" + "Latitude" + "\"" + ":" + "\"" + imei._latitude + "\"" + "," + "\"" + "Longitude" + "\"" + ":" + "\"" + imei._longitude + "\"" + "," + "\"" + "Data_Localizacao" + "\"" + ":" + "\"" + imei._data.toString() + "\"}";
        return json;
    }

	class Listener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Brazil/East"));
			int hora = c.get(c.HOUR_OF_DAY);
			int diaSemana = c.get(c.DAY_OF_WEEK);
			int minuto = c.get(c.MINUTE);
			int segundo = c.get(c.SECOND);

			latitudeStr = String.valueOf(location.getLatitude());
			longitudeStr = String.valueOf(location.getLongitude());
            getIMEI();

            if (hora > 7 && hora < 23 && (minuto % 2 == 0) && segundo == 0){
                helper = new DatabaseHelper(getBaseContext());
                saveLocation();

                if(Conectado(getBaseContext())){
                    ArrayList<ImeiLocation> itensImei = getSavedLocation();

                    if (itensImei.size() > 0) {
                        String jsonItem = "";
                        for (ImeiLocation item : itensImei) {
                            jsonItem = jsonItem + generateJson(item) + ",";
                        }
                        jsonItem = "[" + jsonItem + "]";
                        new HttpAsyncTask().execute("https://apiexterno-hom.simpress.com.br/camadadeservico/infraestrutura/v1/mobileinfo/localizacao/list/", jsonItem);

                        Log.i("Script", "Enviou dados para a API.");
                    }
                    dropLocation();
                    Log.i("Script", "Conectou");
                } else
                    Log.i("Script", "Sem rede");
            }
			Log.i("Script", "IMEI: " + imeiString + " / Latitude: " + latitudeStr + " / Longitude: " + longitudeStr);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { Log.i("Script", "GPS status alterado"); }

		@Override
		public void onProviderEnabled(String provider) {
			Log.i("Script", "GPS ativado");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.i("Script", "GPS desativado");
		}

	}

	class Worker extends Thread{
		public int count = 0;
		public int startId;
		public boolean ativo = true;
		
		public Worker(int startId){
			this.startId = startId;
		}
		
		public void run(){
			while(ativo && count < 1000){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				count++;
				Log.i("Script", "COUNT: "+count);
			}
			stopSelf(startId);
		}
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		
		for(int i = 0, tam = threads.size(); i < tam; i++){
			threads.get(i).ativo = false;
		}
	}
}
