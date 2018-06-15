package com.dbm.split.eventcaster;

public class Item {

	public Item() {
		
	}
	
	@Override
	public String toString() {
		return "Item [name=" + name + ", quantity=" + quantity + "]";
	}

	public Item(String name, int quantity) {
		this.name = name;
		this.quantity = quantity;
	}

	private String name;
	
	private int quantity;
	
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
