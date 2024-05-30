package org.fau.faps.embweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Binding;
import models.QueryJson;

public class ServiceUtils {
	
	public static String convertToCytoJson(String qjson) {
		ObjectMapper objectMapper = new ObjectMapper();
		QueryJson queryJson;
		String cytoJsonString = null;
		try {
			queryJson = objectMapper.readValue(qjson, QueryJson.class);
			
			Binding[] bindings = queryJson.getResults().getBindings();
			
			Map<String, Object> cytoJson = new HashMap<>();
			
			// Add the elements array
			List<Map<String, Object>> elements = new ArrayList<>();
			
			// check and remove all duplicate value
			Set<String> uniquePair = new HashSet<>();
			Set<String> nodes = new HashSet<>();
			for (Binding binding : bindings) {
				
				// all basic informations
				String from = getNameOnlyFromResource(binding.from.value);
				String to = getNameOnlyFromResource(binding.to.value);
				String operator = binding.operator != null ? getNameOnlyFromResource(binding.operator.value) : "";
				
				// add only when they are not added already
				String pair = !operator.isBlank() ? String.format("%s-%s-%s", from, operator, to) 
						: String.format("%s-%s", from, to);
				if(uniquePair.contains(pair)) {
					continue;
				}
				
				
				// add new node if not already exists
				if(!nodes.contains(from)) {
					// add from to element
					Map<String, Object> node1 = new HashMap<>();
			        node1.put("id", from);
			        node1.put("label", from);
			        node1.put("url", binding.from.value);
			        //node1.put("isProcess", ServiceUtils.isProcess(binding.shapeFrom.value));
			        String shapeF = binding.shapeFrom == null ? "process" : ServiceUtils.getType(binding.shapeFrom.value);
			        node1.put("type", shapeF);
			        
			        // add to elements
			        Map<String, Object> nodeFrom = new HashMap<>();
			        nodeFrom.put("data", node1);
			        elements.add(nodeFrom);
			        
			        nodes.add(from);
				}

				// check if node already created or not
				if(!nodes.contains(to)) {
					// add from to element
					Map<String, Object> node1 = new HashMap<>();
			        node1.put("id", to);
			        node1.put("label", to);
			        node1.put("url", binding.to.value);
			        //node1.put("isProcess", ServiceUtils.isProcess(binding.shapeTo.value));
			        if(binding.shapeTo == null) {
			        	node1.put("type", "process");
			        } else {
			        	node1.put("type", ServiceUtils.getType(binding.shapeTo.value));
			        }
			        
			        // add to elements
			        Map<String, Object> nodeTo = new HashMap<>();
			        nodeTo.put("data", node1);
			        elements.add(nodeTo);
			        
			        nodes.add(to);
				}
				
				// add edges here
				Map<String, Object> edge = new HashMap<>();
		        edge.put("id", pair);
		        edge.put("source", from);
		        edge.put("target", to);
		        edge.put("label", operator);
		        
		        Map<String, Object> nodeEdge = new HashMap<>();
		        nodeEdge.put("data", edge);
		        
		        elements.add(nodeEdge);
		        
		        // add this pair to unique pair set
		        uniquePair.add(pair);
			}
			
			// add all elements in cytoscape
			cytoJson.put("elements", elements);
			
			cytoJsonString = objectMapper.writeValueAsString(cytoJson);
			
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cytoJsonString;
	}

	// find out process/property name from rdf resource
	public static String getNameOnlyFromResource(String resource) {
		// get string as param and pick the actual name from their
		int lastSlashIndex = resource.lastIndexOf('/');

		return resource.substring(lastSlashIndex + 1);
	}
	
	public static boolean isValid(String parameter) {
		boolean flag = false;
		// check for validity
		if(!parameter.isBlank() && parameter != null && (parameter.length() > 0) && !(parameter.equals("null"))) {
			flag = true;
		}
		return flag;
	}
	
	@SuppressWarnings("unused")
	private static boolean isProcess(String resource) {
		String resourceName = ServiceUtils.getNameOnlyFromResource(resource);
		// check if it's type is Category-3AVerfahren
		return resourceName.equals("Category-3AVerfahren") ? true : false;
	}
	
	private static String getType(String resource) {
		String resourceName = ServiceUtils.getNameOnlyFromResource(resource);
		// check if it's type is Category-3AVerfahren
		return resourceName.equals("Category-3AVerfahren") ? "process" : "component";
	}
}
