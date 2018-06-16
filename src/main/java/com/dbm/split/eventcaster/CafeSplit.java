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
			// one in five customers is vegetarian
			if(random.nextInt(5) == 0) {
				herbivoreActivty("herbivore-" + (random.nextInt(5) + 1), veggieAttributes);
			} else {
				omnivoreActivity("omnivore-" + (random.nextInt(5) + 1), meatAttributes);
			}
			
			Thread.sleep(1000);
		}
	}

	private void omnivoreActivity(String id, Map<String, Object> splitAttributes) throws Exception {
		Interaction askForMenu = new Interaction();
		askForMenu.setId(id);
		askForMenu.setVerb(Interaction.VERB.MENU);
		askForMenu.setObject(new HashMap<String, Object>());
		askForMenu.setAttributes(splitAttributes);
		JSONObject menu = execute(askForMenu);

		List<Item> foods = orderAtRandom(menu.getJSONArray("food"));
		List<Item> drinks = orderAtRandom(menu.getJSONArray("drinks"));
		Interaction order = new Interaction();
		order.setId(id);
		order.setVerb(Interaction.VERB.ORDER);
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("drinks", drinks);
		object.put("foods", foods);
		order.setObject(object);
		order.setAttributes(splitAttributes);
		execute(order);
		
		order.setVerb(Interaction.VERB.CHECK);
		execute(order);
	}

	// Herbivores are interesting
	private void herbivoreActivty(String id, Map<String, Object> splitAttributes) throws Exception {
		Interaction askForMenu = new Interaction();
		askForMenu.setId(id);
		askForMenu.setVerb(Interaction.VERB.MENU);
		askForMenu.setObject(new HashMap<String, Object>());
		askForMenu.setAttributes(splitAttributes);
		JSONObject menu = execute(askForMenu);

		List<Item> foods = new LinkedList<Item>();

		// if the menu has pasta primevera, order it 1/3 of the time
		if(hasMenuItem(menu, "food", "pasta_primavera")) {
			if(random.nextInt(3) == 0) {
				foods.add(new Item("pasta_primavera", random.nextInt(3) + 1));		
			}
		}

		foods.add(new Item("salad", 1)); // always order a salad

		// order drinks like an omnivore
		List<Item> drinks = orderAtRandom(menu.getJSONArray("drinks"));

		// 100% of the time, if the drink menu has kombucha, order some
		// herbivores like kombucha
		if(hasMenuItem(menu, "drinks", "kombucha")) {
			drinks.add(new Item("kombucha", random.nextInt(5) + 1));
		}

		Interaction order = new Interaction();
		order.setId(id);
		order.setVerb(Interaction.VERB.ORDER);
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("drinks", drinks);
		object.put("foods", foods);
		order.setObject(object);
		order.setAttributes(splitAttributes);
		execute(order);
		
		order.setVerb(Interaction.VERB.CHECK);
		execute(order);
	}

	private boolean hasMenuItem(JSONObject menu, String menuSection, String itemName) {
		JSONArray foodArray = menu.getJSONArray(menuSection);
		boolean result = false;
		for(int i = 0; i < foodArray.length(); i++) {
			if(foodArray.getString(i).equalsIgnoreCase(itemName)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private JSONObject execute(Interaction interaction) throws Exception {
		System.out.println(">> " + interaction); 
		JSONObject result = new JSONObject();

		if(interaction.getVerb() == Interaction.VERB.MENU) {
			String veggieTreatment = client.getTreatment(interaction.getId(), "vegetarian_menu", interaction.getAttributes());

			List<String> foodOptions = new LinkedList<String>();
			foodOptions.add("pepperoni_pizza");
			foodOptions.add("hot_wings");
			foodOptions.add("meatball_sandwich");
			foodOptions.add("salad");

			// if split blesses it, roll out the new veggie food option
			if(veggieTreatment.equals("on")) {
				foodOptions.add("pasta_primavera");
			}

			List<String> drinkOptions = new LinkedList<String>();
			drinkOptions.add("soda");
			drinkOptions.add("water");
			drinkOptions.add("beer");
			drinkOptions.add("wine");

			// if split blesses it, roll out the new veggie drink option
			if(veggieTreatment.equals("on")) {
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

			result = new JSONObject("{ \"message\" : \"food is on the way!\"}");
		} else if (interaction.getVerb() == Interaction.VERB.CHECK) {
			float drinksTotal = 0;
			float foodTotal = 0;

			@SuppressWarnings("unchecked")
			List<Item> drinks = (List<Item>) interaction.getObject().get("drinks");
			for(Item drink : drinks) {
				drinksTotal += priceList.getOrDefault(drink.getName(), 0.0);
			}

			@SuppressWarnings("unchecked")
			List<Item> foods = (List<Item>) interaction.getObject().get("foods");
			for(Item food : foods) {
				foodTotal += priceList.getOrDefault(food.getName(), 0.0);
			}

			client.track(interaction.getId(), "user", "drink_total", drinksTotal);
			client.track(interaction.getId(), "user", "food_total", foodTotal);
			result = new JSONObject("{ \"message\" : \"drinks: $" + drinksTotal + " food: $" + foodTotal + " -- Don't forget to tip your server!\"}");
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

	static Map<String, Double> priceList;
	static {
		priceList = new TreeMap<String, Double>();
		
		priceList.put("pepperoni_pizza", 16.75);
		priceList.put("hot_wings", 11.25);
		priceList.put("meatball_sandwich", 7.50);
		priceList.put("salad", 8.50);
		priceList.put("pasta_primavera", 6.99);
		
		priceList.put("soda", 2.25);
		priceList.put("water", 0.0);
		priceList.put("beer", 4.00);
		priceList.put("wine", 5.50);
		priceList.put("kombucha", 3.75);

	}
}
