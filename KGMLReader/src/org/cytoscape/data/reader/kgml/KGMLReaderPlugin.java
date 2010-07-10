package org.cytoscape.data.reader.kgml;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.cytoscape.data.reader.kgml.util.PathwayDragAndDropManager;
import org.cytoscape.kegg.browser.KEGGNetworkListener;

import cytoscape.Cytoscape;
import cytoscape.data.ImportHandler;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.view.CytoscapeDesktop;

/**
 * KGML Reader Main class
 * 
 * @author kono
 * 
 */
public class KGMLReaderPlugin extends CytoscapePlugin {
	
	// For now, Metabolic pathways only.
	private static final String KEGG_PATHWAY_WEB = "http://www.genome.jp/kegg-bin/get_htext?htext=br08901.keg&filedir=/files&extend=A1&open=A2#A2";

	public KGMLReaderPlugin() {
		final ImportHandler importHandler = new ImportHandler();
		importHandler.addFilter(new KGMLFilter());

		// Add context menu listeners.
		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(
				CytoscapeDesktop.NETWORK_VIEW_CREATED,
				new KEGGNetworkListener());

		// Setup drop target
		PathwayDragAndDropManager.getManager().activateTarget();

		// Add menu to poen KEGG Pathway Web Site
		addMenu();
	}

	private void addMenu() {
		final JMenu menu = new JMenu("KEGG Browser");

		Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("Plugins")
				.add(menu);

		menu.add(new JMenuItem(new AbstractAction("Open KEGG Browser") {
			public void actionPerformed(ActionEvent e) {

				cytoscape.util.OpenBrowser.openURL(KEGG_PATHWAY_WEB);
			}
		}));
	}
}
