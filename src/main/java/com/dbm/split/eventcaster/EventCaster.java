package com.dbm.split.eventcaster;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
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
		long start = System.currentTimeMillis();

		Map<String, Integer> item2Quantity = new TreeMap<String, Integer>();

		for(String item : Prices.foodList.keySet()) {
			item2Quantity.put("food_ " + item, extractSumForEventType("food_" + item));
		}

		for(String item : Prices.drinkList.keySet()) {
			item2Quantity.put("drink_" + item, extractSumForEventType("drink_" + item));
		}

		String html = new String(Files.readAllBytes(Paths.get("donut.template")));

		String rows = "";
		for(Entry<String, Integer> entry : item2Quantity.entrySet()) {
			if(entry.getKey().startsWith("drink_")) {
				rows += "['" + entry.getKey().substring(6) + "', " + entry.getValue() + "],"+ System.getProperty("line.separator");
			}
		}
		rows = rows.substring(0, rows.lastIndexOf(",")) + System.getProperty("line.separator");

		html = html.replaceAll("@@ITEM_AND_QUANTITY@@", rows);

		PrintWriter writer = new PrintWriter("donut.html");
		writer.println(html);
		writer.flush();
		writer.close();

		System.out.println("charted in " + (System.currentTimeMillis() - start) + "ms");
	}

	private int extractSumForEventType(String event_type) throws IOException,
	ClientProtocolException, Exception {
		int result = 0;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		Date endDate = formatter.parse("06-16-2018 09:00:00");
		long end = endDate.getTime();		
		Date startDate = formatter.parse("06-16-2018 08:00:00");
		long start = startDate.getTime();

		boolean hasNextPage = true;
		int offset = 0;
		int limit = 100;
		while(hasNextPage) {
			HttpGet httpGet = new HttpGet("https://api.split.io/internal/api/organization/31ceaa80-6f4e-11e8-8fd5-0acd31e5aef0/eventTypes/" + event_type + "/events?offset=" + offset + "&limit=" + limit + "&start=" + start + "&end=" + end);
			httpGet.addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiIsImNhbGciOiJERUYifQ.eNoMylsKgCAQAMC77HfWPtRajyOsIAQJVgTR3fN3mBf6lSEBY_FiAV0s3hyRbU5VxKHlTThr0YIwQe195N72es71WG4aZk-DRIGVI-vK3w8AAP__.6SnXVnMjs5UwfeLD8_2s2E0RCu4n4GW1rwbC63r8ish7FGGOvC5fa_9StH87l20ILAIsLL7-ObI94HVz4eWgmg");
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
				//				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				String resultString = EntityUtils.toString(entity);
				JSONObject responseObject = new JSONObject(resultString);
				//				System.err.println(responseObject.toString(2));
				JSONObject eventsObject = responseObject.getJSONObject("events");
				JSONArray dataArray = eventsObject.getJSONArray("data");
				for(int i = 0; i < dataArray.length(); i++) {
					JSONObject elementObject = dataArray.getJSONObject(i);
					JSONObject eventObject = elementObject.getJSONObject("event");
					String key = eventObject.getString("key");
					if(key.startsWith("herbivore")) { // filter to only vegetarians
						int value = eventObject.getInt("value");
						result += value;
					}
					String eventTypeId = eventObject.getString("eventTypeId");
					if(!eventTypeId.equals(event_type)) {
						throw new Exception("mismatch on event type!");
					}
				}
				if(eventsObject.getBoolean("hasNextPage")) {
					offset += limit;					
				} else {
					hasNextPage = false;
				}
			} finally {
				response.close();
			}			
		}
		return result;
	}	

}
