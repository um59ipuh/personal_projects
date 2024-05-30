package org.fau.faps.embweb;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class SearchService {

	QueryManager qm;

	public SearchService() {
		// initialize QueryManager
		this.qm = new QueryManager("localhost");
	}

	// Get all paths results as JSON
	public String getAllPathsFrom(String start, String end, String constraint, String category, boolean isUpperH) {

		// connect and get result from TripleStore::Apache Jena
		Query query = qm.getQueryForAllPathsFrom(start, end, constraint, category, isUpperH);
		// check if there is some problem
		if(query == null) {
			return null;
		}
		String sjson = qm.getQuerySolutions(query);

		// convert result to cytoscpape JSON
		String cytoJson = ServiceUtils.convertToCytoJson(sjson);

		// send json
		return cytoJson;
	}
	
	// Get all paths results as JSON
	public String getAllPathsFrom(String sparql) {

		// connect and get result from TripleStore::Apache Jena
		Query query = QueryFactory.create(sparql);
		// check if there is some problem
		if(query == null) {
			return null;
		}
		String sjson = qm.getQuerySolutions(query);

		// convert result to cytoscpape JSON
		String cytoJson = ServiceUtils.convertToCytoJson(sjson);

		// send json
		return cytoJson;
	}

}
