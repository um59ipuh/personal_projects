package models;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class Binding {
	// public properties
	public Entry from;
	public Entry to;
	public Entry shapeFrom;
	public Entry shapeTo;
	public Entry operator;
}
