package org.cytoscape.kegg.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.cytoscape.data.reader.kgml.PathwayMapper;
import org.cytoscape.equations.builtins.Left;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualStyle;

/**
 * Very simple Client for togoWS Rest service.
 * 
 * @author kono
 * @author Kozo.Nishida
 * 
 */
public class KEGGRestClient {

	private static final String USER_AGENT = "Cytoscape KEGG/togoWS REST Client v0.06 (Apache HttpClient 4.0.1)";

	private static final String KEGG_BASE_URL = "http://togows.dbcls.jp/entry/";
	private static final String KEGG_REST_BASE_URL = "http://rest.kegg.jp/";
	private static final String FORMAT_JSON = ".json";

	private static final String KEGG_NAME_LIST = "KEGG.name.list";
	private static final String KEGG_ENTRY = "KEGG.entry";
	private static final String KEGG_LABEL = "KEGG.label";
	private static final String KEGG_LABEL_LIST = "KEGG.label.list";
	private static final String KEGG_LABEL_LIST_FIRST = "KEGG.label.first";
	private static final String KEGG_RELATION = "KEGG.relation";
	private static final String KEGG_REACTION = "KEGG.reaction";
	private static final String KEGG_REACTION_LIST = "KEGG.reaction.list";
	private static final String KEGG_LINK = "KEGG.link";
	private static final String KEGG_TYPE = "KEGG.type";
	private static final String KEGG_COLOR = "KEGG.color";

	private final CyAttributes attr;
	private final CyAttributes nodeAttr;

	private enum DatabaseType {
		COMPOUND("compound"), PATHWAY("kegg-pathway"), MODULE("kegg-module"), REACTION(
				"reaction");

		private final String type;

		private DatabaseType(final String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	private enum FieldType {
		DISEASE("diseases"), DBLINKS("dblinks"), REL_PATHWAY("relpathways"), MODULE(
				"modules"), MODULE_JSON("modules.json"), NAME("name"), REACTION(
				"reactions"), EQUATION("equation");

		private final String type;

		private FieldType(final String type) {
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

	private final KEGGResponseParser parser;

	private KEGGRestClient() {
		this.parser = new KEGGResponseParser();
		this.attr = Cytoscape.getNetworkAttributes();
		this.nodeAttr = Cytoscape.getNodeAttributes();
	}

	public void importCompoundName(final String pathwayName, CyNetwork network)
			throws IOException {

		final List<CyNode> cyNodes = Cytoscape.getCyNodesList();
		final String vsName = "KEGG: " + network.getTitle() + " ("
				+ pathwayName + ")";

		for (CyNode cyNode : cyNodes) {
			if (nodeAttr.getStringAttribute(cyNode.getIdentifier(),
					"KEGG.entry").equals("compound")) {
				final String compoundName = getEntryField(
						DatabaseType.COMPOUND, nodeAttr.getStringAttribute(
								cyNode.getIdentifier(), "KEGG.label"),
						FieldType.NAME);
				nodeAttr.setAttribute(cyNode.getIdentifier(),
						"KEGG.label.first", compoundName);
				nodeAttr.setAttribute(cyNode.getIdentifier(),
						"compound.label.width", 10);
			}
		}

		final VisualStyle targetStyle = Cytoscape.getVisualMappingManager()
				.getCalculatorCatalog().getVisualStyle(vsName);
		Cytoscape.getVisualMappingManager().setVisualStyle(targetStyle);
		final CyNetworkView view = Cytoscape.getNetworkView(network
				.getIdentifier());
		Cytoscape.getVisualMappingManager().setNetworkView(view);
		view.redrawGraph(false, true);

	}

	public List<CyEdge> completeReactionEdges(final String reactionIds) throws IOException {
		final List<CyNode> cyNodes = Cytoscape.getCyNodesList();
		List<CyEdge> edges = new ArrayList<CyEdge>();
		Set<String> currencyCpds = new HashSet<String>();
		currencyCpds.add("C00005");
		currencyCpds.add("C00006");
		currencyCpds.add("C00010");
	
		String entries = getRestEntries(reactionIds);

		for (String entry : entries.split("\n///\n")) {
			if (StringUtils.isNotBlank(entry)) {
				for (String field : entry.split("\n[A-Z]")) {
					if (field.substring(0, 11).equals("PAIR       ")) {
						for (String rpair : field.replaceAll("PAIR       ", "").split("\n            ")) {
							String substrateId = rpair.split("  ")[1].split("_")[0];
							String productId = rpair.split("  ")[1].split("_")[1].substring(0, 6);
							String category = rpair.split("  ")[1].split("_")[1].split(" ")[1];
							
							CyNode cyNode1 = null;
							CyNode cyNode2 = null;

							for (CyNode cyNode : cyNodes) {
								if (nodeAttr.getAttribute(cyNode.getIdentifier(), KEGG_LABEL).equals(substrateId)) {
									cyNode1 = cyNode;
								}
								if (nodeAttr.getAttribute(cyNode.getIdentifier(), KEGG_LABEL).equals(productId)) {
									cyNode2 = cyNode;
								}
							}
							if (cyNode1 != null && cyNode2 != null && category.equals("main")) {
								
								Object cpdId1 = nodeAttr.getAttribute(cyNode1.getIdentifier(), KEGG_LABEL);
								Object cpdId2 = nodeAttr.getAttribute(cyNode2.getIdentifier(), KEGG_LABEL);
								
								if (!currencyCpds.contains(cpdId1) && !currencyCpds.contains(cpdId2)) {
									final CyEdge edge = Cytoscape.getCyEdge(cyNode1, cyNode2, Semantics.INTERACTION, "cc", true);
									System.out.println("created edge between " + cpdId1 + ", " + cpdId2);
									edges.add(edge);
								}
								
							}
						}
					}
				}
			}
		}

		return edges;
	}

	public Set<String> getReactionIds(final String geneIds) throws IOException {
		Set<String> reactionIds = new HashSet<String>();
		Set<String> orthologyIds = new HashSet<String>();

		for (String line : getRestLinkEntries(geneIds, "orthology").split("\n")) {
			if (StringUtils.isNotBlank(line)) {
				orthologyIds.add(line.split("\t")[1]);
			}
		}

		for (String line : getRestLinkEntries(
				StringUtils.join(orthologyIds, "+"), "reaction").split("\n")) {
			if (StringUtils.isNotBlank(line)) {
				reactionIds.add(line.split("\t")[1]);
			}
		}

		return reactionIds;
	}

	public void importAnnotation(final String pathwayID, CyNetwork network)
			throws IOException {

		final String result = getEntries(DatabaseType.PATHWAY, pathwayID);
		System.out.println("# pw result = " + result);

		final String moduleEntryField = getEntryField(DatabaseType.PATHWAY,
				pathwayID, FieldType.MODULE);
		final String relpathwayEntryField = getEntryField(DatabaseType.PATHWAY,
				pathwayID, FieldType.REL_PATHWAY);
		final String dblinkEntryField = getEntryField(DatabaseType.PATHWAY,
				pathwayID, FieldType.DBLINKS);
		final String diseaseEntryField = getEntryField(DatabaseType.PATHWAY,
				pathwayID, FieldType.DISEASE);

		if (moduleEntryField != null)
			parser.mapModule(moduleEntryField, network);

		if (relpathwayEntryField != null)
			parser.mapRelpathway(relpathwayEntryField, network);

		if (dblinkEntryField != null)
			parser.mapDblink(dblinkEntryField, network);

		if (diseaseEntryField != null)
			parser.mapDisease(diseaseEntryField, network);

		final List<String> moduleIDs = attr.getListAttribute(
				network.getIdentifier(), "KEGG.moduleID");
		final Map<String, List<String>> module2reactionMap = new HashMap<String, List<String>>();

		for (String moduleID : moduleIDs) {
			String moduleReactions = getEntryField(DatabaseType.MODULE,
					moduleID, FieldType.REACTION);
			module2reactionMap.put(moduleID,
					parser.getReactionIDs(moduleReactions));
		}

		if (module2reactionMap != null)
			parser.mapModuleReaction(module2reactionMap, network);
	}

	public Map<List<String>, List<String>> getReactions(String reactionID)
			throws IOException {
		final String response = getEntryField(DatabaseType.REACTION,
				reactionID, FieldType.EQUATION);
		final Map<List<String>, List<String>> resultMap = new HashMap<List<String>, List<String>>();

		final String[] parts = response.split("<=>");

		if (parts.length == 2) {
			System.out.print("Left = " + parts[0]);
			System.out.println("  Right = " + parts[1]);
			final String[] left = parts[0].split("\\+");
			final String[] right = parts[1].split("\\+");
			final List<String> keys = new ArrayList<String>();
			final List<String> vals = new ArrayList<String>();
			for (String val : left)
				keys.add(val.trim());
			for (String val : right)
				vals.add(val.trim());
			resultMap.put(keys, vals);
		}
		return resultMap;
	}

	private String getRestEntries(final String ids) throws IOException {
		String restQueryUrl = KEGG_REST_BASE_URL + "get/" + ids;
		System.out.println(restQueryUrl);
		final HttpGet httpget = new HttpGet(restQueryUrl);
		return fetchData(httpget);
	}

	private String getRestLinkEntries(final String ids, final String targetDb)
			throws IOException {
		final HttpGet httpget = new HttpGet(KEGG_REST_BASE_URL + "link/"
				+ targetDb + "/" + ids);
		return fetchData(httpget);
	}

	private String getEntries(final DatabaseType type, final String id)
			throws IOException {
		final HttpGet httpget = new HttpGet(KEGG_BASE_URL + type.getType()
				+ "/" + id.trim());

		return fetchData(httpget);
	}

	private String getEntryField(final DatabaseType dbType, final String id,
			final FieldType fieldType) throws IOException {

		final String uriString = KEGG_BASE_URL + dbType.getType() + "/"
				+ id.trim() + "/" + fieldType.getType();
		final HttpGet httpget = new HttpGet(uriString);

		System.out.println("* Entry URL = " + uriString);

		return fetchData(httpget);
	}

	private String fetchData(HttpGet httpget) throws IOException {
		final DefaultHttpClient httpclient = new DefaultHttpClient();
		final HttpParams param = httpclient.getParams();
		HttpProtocolParams.setUserAgent(param, USER_AGENT);
		final HttpResponse response = httpclient.execute(httpget);
		final HttpEntity entity = response.getEntity();

		if (entity != null)
			return EntityUtils.toString(entity);
		else
			return null;
	}

}
