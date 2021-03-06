package com.vmusco.pminer.analyze;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeMarkers;

/**
 * This class persists the graphical visualization obtained with GraphStream
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class GraphDisplayAnalyzerAndExporter extends GraphDisplayAnalyzer{

	private String persistTo;
	
	
	public GraphDisplayAnalyzerAndExporter(Graph makeUpGraph, String persistingName) {
		super(makeUpGraph);
		
		initMakeuping(makeUpGraph);
		
		this.persistTo = persistingName;
	}
	
	private void initMakeuping(Graph makeUpGraph) {
		for(String node : makeUpGraph.getNodesNames()){
			if(makeUpGraph.isNodeMarked(node, NodeMarkers.USES_REFLEXION)){
				makeUpGraph.shadowNode(node, 2, "yellow");
			}
		}
	}

	@Override
	public void intersectionFound(String id, String in, String[] ais, String[] cis) {
		super.makeUp(id,in,ais, cis);
		super.g.persistAsImage(this.persistTo);
		super.intersectionFound(id, in, ais, cis);
	}
}
