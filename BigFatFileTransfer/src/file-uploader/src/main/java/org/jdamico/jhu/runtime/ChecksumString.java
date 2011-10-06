package org.jdamico.jhu.runtime;

/*
 * This class is used by the Table Models.
 */

public class ChecksumString {
	
	private String value;
	
	public ChecksumString(String input) {
		this.value = input;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String input) {
		this.value = input;
	}
	
	public String toString() {
		return this.value;
	}

}
