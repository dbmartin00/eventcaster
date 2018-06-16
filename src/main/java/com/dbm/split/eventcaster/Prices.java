package com.dbm.split.eventcaster;

import java.util.Map;
import java.util.TreeMap;

public class Prices {
	
	static Map<String, Double> foodList;
	static Map<String, Double> drinkList;
	
	static {
		foodList = new TreeMap<String, Double>();
		drinkList = new TreeMap<String, Double>();
		
		foodList.put("pepperoni_pizza", 16.75);
		foodList.put("hot_wings", 11.25);
		foodList.put("meatball_sandwich", 7.50);
		foodList.put("salad", 8.50);
		foodList.put("pasta_primavera", 6.99);
		
		drinkList.put("soda", 2.25);
		drinkList.put("water", 0.0);
		drinkList.put("beer", 4.00);
		drinkList.put("wine", 5.50);
		drinkList.put("kombucha", 3.75);

	}
}
