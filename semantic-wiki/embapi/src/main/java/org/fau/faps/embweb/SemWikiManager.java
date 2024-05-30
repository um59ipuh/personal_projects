package org.fau.faps.embweb;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;

// all code regarding making query to request to apache jena
public class SemWikiManager {

	public static RDFConnection getConnection() {

		//TODO: replace with environment variable
		String hostIP = "faps_host_pc_ip_address";
		String uri = String.format("http://%s:3030/", hostIP);

		// build connection
		RDFConnection conn0 = RDFConnectionRemote.newBuilder().destination(uri).queryEndpoint("db/query")
				// Set a specific accept header; here, sparql-results+json (preferred) and
				// text/tab-separated-values
				// The default is "application/sparql-results+json,
				// application/sparql-results+xml;q=0.9, text/tab-separated-values;q=0.7,
				// text/csv;q=0.5, application/json;q=0.2, application/xml;q=0.2, */*;q=0.1"
				.acceptHeaderSelectQuery("application/sparql-results+json, application/sparql-results+xml;q=0.9")
				.build();

		return conn0;
	}

}
