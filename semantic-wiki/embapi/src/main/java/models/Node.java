package models;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class Node {
	// public properties
	public String id;
	public String label = null;
	public String isProcess;
}
