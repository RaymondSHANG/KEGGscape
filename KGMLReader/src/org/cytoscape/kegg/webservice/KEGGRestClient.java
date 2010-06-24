package org.cytoscape.kegg.webservice;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import cytoscape.CyNetwork;

/**
 * Very simple Client for togoWS Rest service.
 * 
 * @author kono
 * 
 */
public class KEGGRestClient {
	private static final String KEGG_BASE_URL = "http://togows.dbcls.jp/entry/";
	private static final String FORMAT_JSON = ".json";

	private enum DatabaseType {
		PATHWAY("kegg-pathway"), MODULE("kegg-module");

		private final String type;

		private DatabaseType(final String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	// This client is a singleton.
	private static KEGGRestClient client = new KEGGRestClient();

	public static KEGGRestClient getCleint() {
		return client;
	}

	
	private final HttpClient httpclient;
	private final KEGGResponseParser parser;

	private KEGGRestClient() {
		this.httpclient = new DefaultHttpClient();
		this.parser = new KEGGResponseParser();
		
	}

	
	public void importAnnotation(final String pathwayID, CyNetwork network)
			throws IOException {
		
		final String result = getEntries(DatabaseType.PATHWAY, pathwayID);
		if (result != null) {
			parser.parse(result, network);
			// Test only
			parser.parse2(result);
		}
	}

	
	private String getEntries(final DatabaseType type, final String id) throws IOException {
		final HttpGet httpget = new HttpGet(KEGG_BASE_URL + type.getType()
				+ "/" + id);
		
		final HttpResponse response = httpclient.execute(httpget);
		final HttpEntity entity = response.getEntity();

		if (entity != null)
			return EntityUtils.toString(entity);
		else
			return null;
	}
}
