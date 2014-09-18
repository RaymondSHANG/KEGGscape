package org.cytoscape.keggscape.internal.task;


import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.cytoscape.keggscape.internal.read.kgml.KeggConstants;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class ExpandPathwayContextMenuTaskFactory extends AbstractNodeViewTaskFactory {

	private static final String KEGG_REST_API = "http://rest.kegg.jp/get/";
	private static final String KEGG_FILE_TYPE = "kgml";
	
	private final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory;
	private final VisualMappingManager vmm;

	public ExpandPathwayContextMenuTaskFactory(final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory, VisualMappingManager vmm) {
		super();
		this.vmm = vmm;
		this.loadNetworkURLTaskFactory = loadNetworkURLTaskFactory;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		// Create query
		final List<String> idList = netView.getModel().getRow(nodeView.getModel()).getList(KeggConstants.KEGG_ID, String.class);
		if(idList == null || idList.isEmpty()) {
			throw new NullPointerException("Could not get Map ID.");
		}
		String mapID = idList.get(0);
		// Get collection name
		final CyNetwork network = netView.getModel();
		final String pathwayName = network.getRow(nodeView.getModel()).get(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST, String.class);
		
		if (mapID == null)
			throw new NullPointerException("Map ID is null.");
		else {
			// Create url
			String id = mapID.split(":")[1];
			String urlString = KEGG_REST_API + id + "/" + KEGG_FILE_TYPE;
			URL resourceURL;
			try {
				resourceURL = new URL(urlString);
				return new TaskIterator(new ExpandPathwayTask(loadNetworkURLTaskFactory, resourceURL, pathwayName, mapID, netView, vmm));
			} catch (IOException e) {
				throw new IllegalStateException("Could not open connection to KEGG REST API.", e);
			}
		}
	}
}
