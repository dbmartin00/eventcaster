package com.dbm.split.eventcaster;

import java.util.Map;

// Interaction models a subject, verb and object...
// Attributes are for calls to split
public class Interaction {

	enum VERB { MENU, ORDER, CHECK };
	
	private VERB verb;
	private String id;
	private Map<String, Object> attributes;
	private Map<String, Object> object;
	
	public Interaction() {
		
	}

	public VERB getVerb() {
		return verb;
	}

	public void setVerb(VERB verb) {
		this.verb = verb;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public Map<String, Object> getObject() {
		return object;
	}

	public void setObject(Map<String, Object> object) {
		this.object = object;
	}

	@Override
	public String toString() {
		return "Interaction [verb=" + verb + ", id=" + id + ", attributes="
				+ attributes + ", object=" + object + "]";
	}
	
	

}
