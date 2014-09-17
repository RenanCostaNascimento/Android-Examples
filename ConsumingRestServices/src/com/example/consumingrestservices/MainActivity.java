package com.example.consumingrestservices;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MainActivity extends ActionBarActivity {

	private static final String URL = "http://172.16.109.80:8080/sincap/rest";
	String token;

	Gson gson = new Gson();
	TextView textViewGet, textViewPost;
	Button buttonGet, buttonPost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textViewGet = (TextView) findViewById(R.id.text1);
		buttonGet = (Button) findViewById(R.id.button1);

		textViewPost = (TextView) findViewById(R.id.text2);
		buttonPost = (Button) findViewById(R.id.button2);

		buttonGet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				textViewGet.setText("Executing");
				getButton(v);
			}
		});

		buttonPost.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				textViewPost.setText("Executing");
				postButton(v);

			}
		});
	}

	/*
	 * GET
	 */

	private void getButton(View view) {
		new getService().execute(URL);
	}

	private class getService extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... urls) {
			return getJSON(urls[0]);
		}

		protected void onPostExecute(String result) {
			try {
				// String jsonString = gson.fromJson(result, String.class);
				// System.out.println(jsonString);

				token = result.substring(32, result.length() - 5);

				textViewGet.setText(token);
				
			} catch (Exception e) {
				textViewGet.setText(e.getLocalizedMessage());
			}
		}
	}

	public String getJSON(String URL) {
		StringBuilder stringBuilder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL + "/token");
		try {
			HttpResponse response = httpClient.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("csrf")) {
						stringBuilder.append(line + "\n");
						System.out.println(stringBuilder.toString());
						break;
					}
				}
				inputStream.close();
			} else {
				Log.d("JSON", "Failed to download file");
			}
		} catch (Exception e) {
			Log.d("GET", e.getLocalizedMessage());
		}
		return stringBuilder.toString();
	}

	/*
	 * POST
	 */

	public void postButton(View view) {

		new postService().execute(URL);
	}

	private class postService extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			return postJSON(urls[0]);
		}

		protected void onPostExecute(String result) {
			try {

				// String jsonString = gson.fromJson(result, String.class);

				textViewPost.setText(result);

			} catch (Exception e) {
				Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
			}
		}
	}

	public String postJSON(String URL) {
		String result = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL + "/autenticar");
		httpPost.setHeader("Content-type", "application/json; charset=UTF-8");
		httpPost.setHeader("CSRFToken", token);
		
		BasicCookieStore basicCookieStore = new BasicCookieStore();
		BasicClientCookie basicClientCookie = new BasicClientCookie("csrftoken", token);
		System.out.println("DOMAIN: "+basicClientCookie.getDomain());
		basicCookieStore.addCookie(basicClientCookie);
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute("cookie", basicCookieStore);


		JsonObject object = new JsonObject();
		object.addProperty("documentoSocial", "documento de markin");
		object.addProperty("nome", "Markin");
		object.addProperty("telefone", "tel de markin");

		StringEntity stringEntity;
		try {
			stringEntity = new StringEntity(object.toString());
			httpPost.setEntity(stringEntity);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			HttpResponse response = httpClient.execute(httpPost, httpContext);

			StatusLine statusLine = response.getStatusLine();

			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			if (inputStream != null) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					result += line;
				}
				inputStream.close();
			} else {
				result = "Did not work!";
			}
		} catch (Exception e) {
			Log.d("JSONPOST", e.getLocalizedMessage());
		}
		return result;
	}

}
