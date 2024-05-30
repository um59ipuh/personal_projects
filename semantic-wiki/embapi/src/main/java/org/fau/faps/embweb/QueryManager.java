package org.fau.faps.embweb;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;

public class QueryManager {

	// Constants for host
	private String hostname;

	// global query
	private String queryPrefixString;

	public QueryManager(String host) {
		this.hostname = host;
		
		this.queryPrefixString = String.format("""
				PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				PREFIX uri: <http://%s/emb-wiki/index.php/Special:URIResolver/>
				PREFIX property: <http://%s/emb-wiki/index.php/Special:URIResolver/Property-3A>
				PREFIX category: <http://%s/emb-wiki/index.php/Special:URIResolver/Category-3A>

				""", this.hostname, this.hostname, this.hostname);
	}

	// create query when one material is given and and search
	public Query getQueryForAllPathsFrom(String from, String to, String constraint, String category, boolean isUpperH) {
		
		// mapping data for category
		Map<String, String> mapper = getMappingData(category);
		
		// if upper hierarchy is set to true then we are creating category specific query
		if(isUpperH && ServiceUtils.isValid(category)) {
			
			if(mapper == null) {
				return null;
			}
			
			// Qeury variable :: endProduct and specific category
			String queryString = String.format(this.queryPrefixString + """
					
					SELECT DISTINCT ?from ?to ?shapeFrom ?shapeTo
					WHERE {
					  ?to property:Hat_Nachfolger* uri:%s.
					  ?from property:Hat_Nachfolger ?to.
					  ?start property:Hat_Nachfolger* ?from.
					  
					?category rdf:type category:%s.
					filter (?to = ?category || ?from = ?category )
					  
					OPTIONAL { ?from rdf:type ?shapeFrom.
					VALUES ?shapeFrom {category:Komponente}}
					  
					OPTIONAL { ?to rdf:type ?shapeTo.
					VALUES ?shapeTo {category:Komponente}}
					}
					order by ?start

					""", mapper.get("endProduct"), mapper.get("specificCategory"));
			
			System.out.println("Query:");
			System.out.println(queryString);
			System.out.println("------------------------------------------------------------------------------------------");
			
			return QueryFactory.create(queryString);
		}
		
		// sections for replacing variable :: ?from, ?to, ?constraint
		String constSection = "";
		String categorySection = "";
		String fromSection = ServiceUtils.isValid(from) ? (" uri:" + from) : " ?start ";
		String toSection = ServiceUtils.isValid(to) ? (" uri:" + to) : " ?to ";
		
		
		// add code segments, one is for category and another is for constraints
		
		
		// add this segment if constraints is chosen 
		if(ServiceUtils.isValid(category)) {
			categorySection = String.format("""
					?teilprozess rdf:type category:%s.
					?teilprozess property:Hat_Verfahren ?verfahren.
					""", mapper.get("specificCategory"));
		}
		
		// add this segment if constraints is chosen 
		if(ServiceUtils.isValid(constraint)) {
			constSection = String.format("""
					?verfahren property:%s "Ja".
					""", constraint);
		}
		
		String filterSection = "";
		if(ServiceUtils.isValid(category) || ServiceUtils.isValid(constraint)) {
			filterSection = "FILTER (?from = ?verfahren || ?to = ?verfahren)";
		}
		
		// Qeury string
		String queryString = String.format(this.queryPrefixString + """
				
				SELECT DISTINCT ?from ?to ?operator ?shapeFrom ?shapeTo 
				WHERE {
				  ?to property:Hat_Nachfolger* %s.
				  ?from property:Hat_Nachfolger ?to.
				  %s property:Hat_Nachfolger* ?from.
				  
				  %s
				
				  %s
				  
				  %s
				  
				  ?from rdf:type ?shapeFrom.
				  ?to rdf:type ?shapeTo.
				  
				  VALUES ?shapeFrom {category:Komponente category:Verfahren}
				  VALUES ?shapeTo {category:Komponente category:Verfahren}
				  OPTIONAL {?from property:Hat_Operator ?operator}
				}
				order by ?start

				""", toSection, fromSection, categorySection, constSection, filterSection);
		
		// for inspecting query
		System.out.println("Query:");
		System.out.println(queryString);
		System.out.println("------------------------------------------------------------------------------------------");
		
		return QueryFactory.create(queryString);
	}

	
	// create dataset for category then get one
	private Map<String, String> getMappingData(String category) {
		
		Map<String, Map<String, String>> categories = new HashMap<>();

		// map all data
        categories.put("Hairpinstatorproduction", createSubMap("4b_Statorfertigung_-28Formspulen-29", "4_Statorfertigung", "Stator"));
        categories.put("Blechpaketfertigung", createSubMap("2_Blechpaketfertigung", "2_Blechpaketfertigung", "Blechpaket"));
        categories.put("Wellenfertigung", createSubMap("3_Wellenfertigung", "3_Wellenfertigung", "Welle"));
        categories.put("Gehäusefertigung", createSubMap("1_Gehäusefertigung", "1_Gehäusefertigung", "Gehäuse"));
        categories.put("konventionelleStatorproduktion", createSubMap("4a_Statorfertigung_-28konventionell-29", "4_Statorproduktion", "Stator"));
        categories.put("RotorfertigungFSM", createSubMap("5c_Rotorfertigung_-28FSM-29", "5_Rotorfertigung", "Rotor"));
        categories.put("RotorfertigungPSM", createSubMap("5a_Rotorfertigung_-28PSM-29", "5_Rotorfertigung", "Rotor"));
        categories.put("RotorfertigungASM", createSubMap("5a_Rotorfertigung_-28ASM-29", "5_Rotorfertigung", "Rotor"));

        // get mapping data for one category
        return categories.get(category);
	}
	
	// support method for creating mapping data for categories
	private static Map<String, String> createSubMap(String specificCategory, String overallCategory, String endproduct) {
        Map<String, String> subMap = new HashMap<>();
        subMap.put("specificCategory", specificCategory);
        subMap.put("overallCategory", overallCategory);
        subMap.put("endProduct", endproduct);
        return subMap;
    }

	// get query solutions
	public String getQuerySolutions(Query query) {

		// get connection and get result set from triple-store
		try (RDFConnection conn = SemWikiManager.getConnection()) {
			QueryExecution qexec = conn.query(query);

			ResultSet results = qexec.execSelect();

			// write to a ByteArrayOutputStream
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			ResultSetFormatter.outputAsJSON(outputStream, results);

			// and turn that into a String
			String json = new String(outputStream.toByteArray());
			
			return json;

		}
	}

}
