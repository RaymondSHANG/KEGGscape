package org.cytoscape.keggscape.internal.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Set;

import org.cytoscape.keggscape.internal.read.kgml.KEGGTags;
import org.cytoscape.keggscape.internal.read.kgml.KeggConstants;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class KGMLVisualStyleBuilder {

	// Default visual style name
	public static final String DEF_VS_NAME = "KEGG Style";
	public static final String GLOBAL_VS_NAME = "KEGG Global Map Style";

	private final VisualStyleFactory vsFactory;
	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;
	private final Set<VisualLexicon> lexicons; 

	public KGMLVisualStyleBuilder(final VisualStyleFactory vsFactory,
			final VisualMappingFunctionFactory discreteMappingFactory,
			final VisualMappingFunctionFactory passthroughMappingFactory, final VisualMappingManager vmm) {
		this.vsFactory = vsFactory;
		this.discreteMappingFactory = discreteMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
		lexicons = vmm.getAllVisualLexicon();
	}

	public VisualStyle getVisualStyle() {
		final VisualStyle defStyle = vsFactory.createVisualStyle(DEF_VS_NAME);
		final Set<VisualPropertyDependency<?>> deps = defStyle.getAllVisualPropertyDependencies();

		// handle locked values
		for (VisualPropertyDependency<?> dep : deps) {
			if (dep.getIdString().equals("nodeSizeLocked")) {
				if (dep.isDependencyEnabled()) {
					dep.setDependency(false);
				}
			}
			if (dep.getIdString().equals("arrowColorMatchesEdge")) {
				dep.setDependency(true);
			}
		}

		createDefaults(defStyle);

		final PassthroughMapping<String, Double> nodexPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_X, String.class, BasicVisualLexicon.NODE_X_LOCATION);
		final PassthroughMapping<String, Double> nodeyPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_Y, String.class, BasicVisualLexicon.NODE_Y_LOCATION);
		final PassthroughMapping<String, Double> nodewidthPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_WIDTH, String.class, BasicVisualLexicon.NODE_WIDTH);
		final PassthroughMapping<String, Double> nodeheightPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_HEIGHT, String.class, BasicVisualLexicon.NODE_HEIGHT);
		final PassthroughMapping<String, String> nodelabelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST, String.class,
						BasicVisualLexicon.NODE_LABEL);
		final PassthroughMapping<String, Paint> nodelabelcolorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_LABEL_COLOR, String.class,
						BasicVisualLexicon.NODE_LABEL_COLOR);
		final PassthroughMapping<String, Paint> nodefillcolorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_FILL_COLOR, String.class,
						BasicVisualLexicon.NODE_FILL_COLOR);
		final PassthroughMapping<String, String> nodeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_LABEL, String.class, BasicVisualLexicon.NODE_TOOLTIP);

		final PassthroughMapping<String, String> edgeLabelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_EDGE_LABEL, String.class,
						BasicVisualLexicon.EDGE_LABEL);
		
		final PassthroughMapping<String, Paint> edgeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_EDGE_COLOR, String.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);

		defStyle.addVisualMappingFunction(nodexPassthrough);
		defStyle.addVisualMappingFunction(nodeyPassthrough);
		defStyle.addVisualMappingFunction(nodewidthPassthrough);
		defStyle.addVisualMappingFunction(nodeheightPassthrough);
		defStyle.addVisualMappingFunction(nodelabelPassthrough);
		defStyle.addVisualMappingFunction(nodeTooltipPassthrough);
		defStyle.addVisualMappingFunction(nodelabelcolorPassthrough);
		defStyle.addVisualMappingFunction(nodefillcolorPassthrough);
		
		defStyle.addVisualMappingFunction(edgeLabelPassthrough);
		defStyle.addVisualMappingFunction(edgeColorPassthrough);

		final DiscreteMapping<String, LineType> edgelinetypeMapping = (DiscreteMapping<String, LineType>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_EDGE_SUBTYPES, String.class,
						BasicVisualLexicon.EDGE_LINE_TYPE);
		edgelinetypeMapping.putMapValue("maplink", LineTypeVisualProperty.LONG_DASH);
		edgelinetypeMapping.putMapValue("state change", LineTypeVisualProperty.DOT);
		edgelinetypeMapping.putMapValue("binding/association", LineTypeVisualProperty.LONG_DASH);
		edgelinetypeMapping.putMapValue("indirect effect", LineTypeVisualProperty.LONG_DASH);
		
		final DiscreteMapping<String, ArrowShape> targetArrowShapeMapping = (DiscreteMapping<String, ArrowShape>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_EDGE_SUBTYPES, String.class,
						BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		targetArrowShapeMapping.putMapValue("irreversible", ArrowShapeVisualProperty.ARROW);
		targetArrowShapeMapping.putMapValue("activation", ArrowShapeVisualProperty.ARROW);
		targetArrowShapeMapping.putMapValue("inhibition", ArrowShapeVisualProperty.T);
		targetArrowShapeMapping.putMapValue("expression", ArrowShapeVisualProperty.ARROW);
		targetArrowShapeMapping.putMapValue("repression", ArrowShapeVisualProperty.T);
		targetArrowShapeMapping.putMapValue("indirect effect", ArrowShapeVisualProperty.HALF_TOP);
		
		final DiscreteMapping<String, NodeShape> nodetypeMapping = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
		nodetypeMapping.putMapValue("ortholog", NodeShapeVisualProperty.RECTANGLE);
		nodetypeMapping.putMapValue("gene", NodeShapeVisualProperty.RECTANGLE);
		nodetypeMapping.putMapValue("map", NodeShapeVisualProperty.ROUND_RECTANGLE);
		nodetypeMapping.putMapValue("compound", NodeShapeVisualProperty.ELLIPSE);
		nodetypeMapping.putMapValue("group", NodeShapeVisualProperty.RECTANGLE);

		final DiscreteMapping<String, Double> nodeBorderMapping = (DiscreteMapping<String, Double>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class,
						BasicVisualLexicon.NODE_BORDER_WIDTH);
		nodeBorderMapping.putMapValue("compound", 2d);
		nodeBorderMapping.putMapValue("ortholog", 1d);
		nodeBorderMapping.putMapValue("group", 1d);
		
		final DiscreteMapping<String, Integer> nodeTransparencyMapping = (DiscreteMapping<String, Integer>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class,
						BasicVisualLexicon.NODE_TRANSPARENCY);
		nodeTransparencyMapping.putMapValue("group", 0);
		
		final DiscreteMapping<String, Integer> nodeLabelFontSizeMapping = (DiscreteMapping<String, Integer>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class,
						BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		nodeLabelFontSizeMapping.putMapValue("compound", 6);
		nodeLabelFontSizeMapping.putMapValue("ortholog", 9);
		nodeLabelFontSizeMapping.putMapValue("group", 9);
		nodeLabelFontSizeMapping.putMapValue("map", 9);
		nodeLabelFontSizeMapping.putMapValue("gene", 9);

		defStyle.addVisualMappingFunction(edgelinetypeMapping);
		defStyle.addVisualMappingFunction(targetArrowShapeMapping);
		
		defStyle.addVisualMappingFunction(nodetypeMapping);
		defStyle.addVisualMappingFunction(nodeBorderMapping);
		defStyle.addVisualMappingFunction(nodeLabelFontSizeMapping);
		defStyle.addVisualMappingFunction(nodeTransparencyMapping);
		
		VisualMappingFunction<?, ?> labelPositionMap = createLabelPositionMapping(defStyle);
		if(labelPositionMap != null) {
			defStyle.addVisualMappingFunction(labelPositionMap);
		}
		return defStyle;
	}

	private final void createDefaults(final VisualStyle style) {
		// Defaults for nodes
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 8);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_WIDTH, 110d);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0d);
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 240);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 255);
		style.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 70d);
		style.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 70d);
		style.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 70d);
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0x0066bb, false));
		
		Font nodeFont = new Font("HelveticaNeue", Font.PLAIN, 12);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, nodeFont);

		// Defaults for Edges
		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 1d);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 10);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 255);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, new Color(0xdc143c, false));
		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.DARK_GRAY);
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.DARK_GRAY);
		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 180);

		Font edgeFont = new Font("HelveticaNeue", Font.PLAIN, 12);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_FACE, edgeFont);
		
	}
	private final VisualMappingFunction<?, ?> createLabelPositionMapping(final VisualStyle style) {
		VisualProperty<?> labelPosition = null;
		if(lexicons != null) {
			for(VisualLexicon lex: lexicons) {
				Set<VisualProperty<?>> vps = lex.getAllVisualProperties();
				for(VisualProperty<?> vp:vps) {
					if(vp.getIdString().equals("NODE_LABEL_POSITION")) {
						labelPosition = vp;
						break;
					}
				}
			}
			if(labelPosition != null) {
				Object compoundLabelPosition = labelPosition.parseSerializableString("S,N,c,0.00,2.00");
				@SuppressWarnings("unchecked")
				final DiscreteMapping<String, Object> nodeLabelPositionMapping = (DiscreteMapping<String, Object>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class, labelPosition);
				nodeLabelPositionMapping.putMapValue("compound", compoundLabelPosition);
				return nodeLabelPositionMapping;
			}
		}
		return null;
	}

	public VisualStyle getGlobalVisualStyle() {
		// First, create standard style.
		final VisualStyle originalStyle = vsFactory.createVisualStyle(this.getVisualStyle());
		originalStyle.setTitle(GLOBAL_VS_NAME);
		final Set<VisualPropertyDependency<?>> deps = originalStyle.getAllVisualPropertyDependencies();

		// handle locked values
		for (VisualPropertyDependency<?> dep : deps) {
			if (dep.getIdString().equals("nodeSizeLocked")) {
				if (dep.isDependencyEnabled()) {
					dep.setDependency(false);
				}
			}
			if (dep.getIdString().equals("arrowColorMatchesEdge")) {
				dep.setDependency(true);
			}
		}

		// Then, update for Global Map.
		originalStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_BORDER_WIDTH);
		originalStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY);
		originalStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_TRANSPARENCY);
		
		final DiscreteMapping<String, Integer> nodeTransparencyMapping = (DiscreteMapping<String, Integer>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class,
						BasicVisualLexicon.NODE_TRANSPARENCY);
		nodeTransparencyMapping.putMapValue(KEGGTags.GROUP.getTag(), 0);
		nodeTransparencyMapping.putMapValue(KEGGTags.MAP.getTag(), 100);
		
		final DiscreteMapping<String, Integer> nodeLabelTransparencyMapping = (DiscreteMapping<String, Integer>) discreteMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_TYPE, String.class,
						BasicVisualLexicon.NODE_LABEL_TRANSPARENCY);
		nodeLabelTransparencyMapping.putMapValue(KEGGTags.GROUP.getTag(), 0);
		nodeLabelTransparencyMapping.putMapValue(KEGGTags.MAP.getTag(), 240);
		
		final PassthroughMapping<String, String> nodeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST, String.class,
						BasicVisualLexicon.NODE_TOOLTIP);
		
		final PassthroughMapping<String, String> edgeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(CyNetwork.NAME, String.class,
						BasicVisualLexicon.EDGE_TOOLTIP);
		
		final PassthroughMapping<String, Paint> edgeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_EDGE_COLOR, String.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		
		final PassthroughMapping<String, Paint> edgeStrokeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KeggConstants.KEGG_EDGE_COLOR, String.class,
						BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		
		@SuppressWarnings("unchecked")
		DiscreteMapping<String, Integer> nodeFontSizeMapping = (DiscreteMapping<String, Integer>) originalStyle.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		nodeFontSizeMapping.putMapValue(KEGGTags.MAP.getTag(), 14);
		
		originalStyle.addVisualMappingFunction(nodeTransparencyMapping);
		originalStyle.addVisualMappingFunction(nodeLabelTransparencyMapping);
		originalStyle.addVisualMappingFunction(nodeTooltipPassthrough);
		
		originalStyle.addVisualMappingFunction(edgeTooltipPassthrough);
		originalStyle.addVisualMappingFunction(edgeColorPassthrough);
		originalStyle.addVisualMappingFunction(edgeStrokeColorPassthrough);
		

		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 240);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 100);
		
		originalStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 200);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0d);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_WIDTH, 150d);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.white);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, new Color(80, 80, 80));
		originalStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 7d);
		originalStyle.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);

		return originalStyle;
	}
}