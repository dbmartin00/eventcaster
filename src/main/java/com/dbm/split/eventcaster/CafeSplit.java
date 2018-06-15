package com.dbm.split.eventcaster;

import io.split.client.SplitClient;
import io.split.client.SplitClientConfig;
import io.split.client.SplitFactory;
import io.split.client.SplitFactoryBuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class CafeSplit {
	
	private SplitClientConfig config;
	private SplitClient client;

	public CafeSplit() throws Exception {
		config = SplitClientConfig.builder()
				.ready(10000) 
				.build();

		SplitFactory splitFactory = null;
		splitFactory = SplitFactoryBuilder.build("gnrqkgrk2vum5hduk04m4lu67qtm36f68qge", config);
		this.client = splitFactory.client();
	}
	
	public static void main(String[] args) throws Exception {
		CafeSplit cafeSplit = new CafeSplit();
		cafeSplit.simulateActivity();
	}

	private void simulateActivity() throws Exception {
		Map<String, Object> veggieAttributes = new TreeMap<String, Object>();
		veggieAttributes.put("is_vegetarian", true);
		Map<String, Object> meatAttributes = new TreeMap<String, Object>();
		meatAttributes.put("is_vegetarian", false);
		
		while (true) {
			omnivoreActivity(meatAttributes);
			herbivoreActivty(veggieAttributes);
			Thread.sleep(1000);
		}
	}

	private void omnivoreActivity(Map<String, Object> splitAttributes) throws Exception {
		Interaction askForMenu = new Interaction();
		askForMenu.setId("omnivore");
		askForMenu.setVerb(Interaction.VERB.MENU);
		askForMenu.setObject(new HashMap<String, Object>());
		askForMenu.setAttributes(splitAttributes);
		JSONObject menu = execute(askForMenu);
		
		List<Item> foods = orderAtRandom(menu.getJSONArray("food"));
		List<Item> drinks = orderAtRandom(menu.getJSONArray("drinks"));
		Interaction order = new Interaction();
		order.setId("omnivore");
		order.setVerb(Interaction.VERB.ORDER);
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("drinks", drinks);
		object.put("foods", foods);
		order.setObject(object);
		order.setAttributes(splitAttributes);
		execute(order);
	}

	private void herbivoreActivty(Map<String, Object> splitAttributes) throws Exception {
		Interaction askForMenu = new Interaction();
		askForMenu.setId("herbivore");
		askForMenu.setVerb(Interaction.VERB.MENU);
		askForMenu.setObject(new HashMap<String, Object>());
		askForMenu.setAttributes(splitAttributes);
		JSONObject menu = execute(askForMenu);
		
		List<Item> foods = new LinkedList<Item>();
		
		// 50% of the time, if the menu has pasta primevera, order it.
		if(random.nextBoolean()) {
			JSONArray foodArray = menu.getJSONArray("food");
			boolean hasPasta = false;
			for(int i = 0; i < foodArray.length(); i++) {
				if(foodArray.getString(i).equalsIgnoreCase("pasta_primevera")) {
					hasPasta = true;
					break;
				}
			}
			if(hasPasta) {
				foods.add(new Item("pasta_primevera", random.nextInt(2) + 1));
			}
		}
		foods.add(new Item("salad", 1)); // always order a salad
		
		// 100% of the time, if the drink menu has kombucha, order it
		List<Item> drinks = orderAtRandom(menu.getJSONArray("drinks"));
		JSONArray drinksArray = menu.getJSONArray("drinks");
		boolean hasKombucha = false;
		for(int i = 0; i < drinksArray.length(); i++) {
			if(drinksArray.getString(i).equalsIgnoreCase("kombucha")) {
				hasKombucha = true;
				break;
			}
		}
		if(hasKombucha) {
			drinks.add(new Item("kombucha", random.nextInt(5) + 1));
		}
		
		Interaction order = new Interaction();
		order.setId("herbivore");
		order.setVerb(Interaction.VERB.ORDER);
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("drinks", drinks);
		object.put("foods", foods);
		order.setObject(object);
		order.setAttributes(splitAttributes);
		execute(order);
	}

	private JSONObject execute(Interaction interaction) throws Exception {
		System.err.println(">> " + interaction); 
		JSONObject result = new JSONObject();

		if(interaction.getVerb() == Interaction.VERB.MENU) {
			String veggieTreatment = client.getTreatment(interaction.getId(), "vegetarian_menu", interaction.getAttributes());
			System.out.println("*** veggieTreatment? " + veggieTreatment + " ****");
			
			List<String> foodOptions = new LinkedList<String>();
			foodOptions.add("pepporni_pizza");
			foodOptions.add("hot_wings");
			foodOptions.add("meatball_sandwich");
			foodOptions.add("salad");

			if(veggieTreatment.equals("on")) {
				foodOptions.add("pasta_primivera");
			}

			String kombuchaTreatment = client.getTreatment(interaction.getId(), "vegetarian_menu", interaction.getAttributes());

			List<String> drinkOptions = new LinkedList<String>();
			drinkOptions.add("soda");
			drinkOptions.add("water");
			drinkOptions.add("beer");
			drinkOptions.add("wine");

			if(kombuchaTreatment.equals("on")) {
				drinkOptions.add("kombucha");
			}

			JSONArray foodArray = new JSONArray(foodOptions);
			JSONArray drinksArray = new JSONArray(drinkOptions);

			result.put("food", foodArray);
			result.put("drinks", drinksArray);

		} else if (interaction.getVerb() == Interaction.VERB.ORDER) {			
			@SuppressWarnings("unchecked")
			List<Item> drinks = (List<Item>) interaction.getObject().get("drinks");
			for(Item drink : drinks) {
				client.track(interaction.getId(), "user", "drink_" + drink.getName(), drink.getQuantity());
			}
			
			@SuppressWarnings("unchecked")
			List<Item> foods = (List<Item>) interaction.getObject().get("foods");
			for(Item food : foods) {
				client.track(interaction.getId(), "user", "food_" + food.getName(), food.getQuantity());
			}

			result = new JSONObject("{ \"readyInMillis\" : " + 60000 + "}");
		} else if (interaction.getVerb() == Interaction.VERB.CHECK) {
			throw new Exception("unimplemented");
		}

		System.out.println("<< " + result.toString());
		return result;
	}


	static Random random = new Random(System.currentTimeMillis());
	private static List<Item> orderAtRandom(JSONArray itemsArray) {
		List<Item> results = new LinkedList<Item>();

		int limit = random.nextInt(3);
		for(int i = 0; i < limit; i++) {
			results.add(new Item(itemsArray.getString(random.nextInt(itemsArray.length())), random.nextInt(5)));
		}

		return results;
	}

}
