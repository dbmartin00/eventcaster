package com.dbm.split.eventcaster;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventCaster {

	public static void
	main(String[] args) throws Exception {
		new EventCaster().execute();
	}
	
	public void execute() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		long end = System.currentTimeMillis();
		long start = System.currentTimeMillis() - 10 * 60 * 1000;
		String event_type = "food_total";
		HttpGet httpGet = new HttpGet("https://api.split.io/internal/api/organization/31ceaa80-6f4e-11e8-8fd5-0acd31e5aef0/eventTypes/" + event_type + "/events?offset=0&limit=100&start=" + start + "&end=" + end);
		httpGet.addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiIsImNhbGciOiJERUYifQ.eNoMylEKgCAMANC77DtrcxrO4wgbCEHCKoLo7vX7eA_42aBCREusGcNqSQORliDCHFBb4djExBAm6O5_9rH1Y-77ctFveg-olKNQkcT0fgAAAP__.VpU3rgeRl-mOTcvsDzZgV_E4kD9X2jBokpcdM4bcGOs3XooGHQM1r3g0exlV_EAfiIOHtrB0gINAU3u0htNNhw");
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
		    System.out.println(response.getStatusLine());
		    HttpEntity entity = response.getEntity();
		    String result = EntityUtils.toString(entity);
			JSONObject responseObject = new JSONObject(result);
			
			JSONObject eventsObject = responseObject.getJSONObject("events");
			JSONArray dataArray = eventsObject.getJSONArray("data");
			for(int i = 0; i < dataArray.length(); i++) {
				JSONObject elementObject = dataArray.getJSONObject(i);
				JSONObject eventObject = elementObject.getJSONObject("event");
				float value = eventObject.getFloat("value");
				String eventTypeId = eventObject.getString("eventTypeId");
				System.out.println(eventTypeId + " -> " + value);
			}
		} finally {
		    response.close();
		}
	}	
	
}
