package com.dbm.split.eventcaster;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class EventCaster {

	public void execute() throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		long end = System.currentTimeMillis();
		long start = System.currentTimeMillis() - 10 * 60 * 1000;
		String event_type = "food_hot_wings";
		HttpGet httpGet = new HttpGet("https://api.split.io/internal/api/organization/31ceaa80-6f4e-11e8-8fd5-0acd31e5aef0/eventTypes/" + event_type + "/events?offset=0&limit=10&start=" + start + "&end=" + end);
		httpGet.addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiIsImNhbGciOiJERUYifQ.eNoMylEKgCAMANC77Dtrc1rO4wgThCBhFUF09_x9vBfsKpDBYw2sEd1agzoiTU6E2aGWxL5IlYowQTMb2frezrkdy03D9OmQKXoh9LLR9wMAAP__.M-kLnuNzjpPT9THRZQfIVgf9U8AoC-XDRyKiRTTnXR-hTWQhg-85t9s2qkpk6CmDH9tT4BfZ4g-KwBOGIMW5Gw");
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
		    System.out.println(response.getStatusLine());
		    HttpEntity entity = response.getEntity();
		    JSONObject responseObject = new JSONObject(EntityUtils.toString(entity));
		    System.out.println(responseObject.toString(2));
		} finally {
		    response.close();
		}
	}	
	
}
