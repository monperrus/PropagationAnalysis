package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import spoon.compiler.SpoonCompiler;
import spoon.processing.Processor;

import com.jeantessier.classreader.ClassfileLoader;
import com.jeantessier.classreader.LoadListenerVisitorAdapter;
import com.jeantessier.classreader.TransientClassfileLoader;
import com.jeantessier.dependency.ClassNode;
import com.jeantessier.dependency.CodeDependencyCollector;
import com.jeantessier.dependency.DependencyEvent;
import com.jeantessier.dependency.DependencyListener;
import com.jeantessier.dependency.FeatureNode;
import com.jeantessier.dependency.NodeFactory;
import com.jeantessier.dependency.PackageNode;
import com.jeantessier.dependency.SelectionCriteria;
import com.vmusco.smf.exceptions.MalformedSourcePositionException;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class JeantessierGraphBuilder extends GraphBuildLogic {

	@Override
	public Graph build(SpoonCompiler compiler) {
		List<Processor<?>> arg0 = new ArrayList<>();
		File f = new File("/tmp/spoon");
		f.mkdir();
		
		try {
			//compiler.setDestinationDirectory(f);
			compiler.setBinaryOutputDirectory(f);
			compiler.process(arg0);
			compiler.compileInputSources();
			generateGraph(f);
		} catch (Exception e) {
			return null;
		}
		return ProcessorCommunicator.outputgraph; 
	}


	private Graph generateGraph(File builtfolder) {
		ArrayList<String> files = new ArrayList<String>();
		files.add(builtfolder.getAbsolutePath());
		NodeFactory factory = new NodeFactory();
		SelectionCriteria sc = new SelectionCriteria() {
			
			@Override
			public boolean matchesPackageName(String name) {
				return false;
			}
			
			@Override
			public boolean matchesFeatureName(String name) {
				return true;
			}
			
			@Override
			public boolean matchesClassName(String name) {
				return false;
			}
			
			@Override
			public boolean matches(FeatureNode node) {
				return true;
			}
			
			@Override
			public boolean matches(ClassNode node) {
				return false;
			}
			
			@Override
			public boolean matches(PackageNode node) {
				return false;
			}
			
			@Override
			public boolean isMatchingPackages() {
				return false;
			}
			
			@Override
			public boolean isMatchingFeatures() {
				return true;
			}
			
			@Override
			public boolean isMatchingClasses() {
				return false;
			}
		};
		
		CodeDependencyCollector collector = new CodeDependencyCollector(factory, sc);
		collector.addDependencyListener(new MyDependencyListener());
	    ClassfileLoader loader = new TransientClassfileLoader();
	    loader.addLoadListener(new LoadListenerVisitorAdapter(collector));
	    loader.load(files);
		
	    
		return null;
	}
	
	static class MyDependencyListener implements DependencyListener{

		@Override
		public void beginClass(DependencyEvent event) {
		}

		@Override
		public void beginSession(DependencyEvent event) {
		}

		@Override
		public void dependency(DependencyEvent event) {
			String src = event.getDependent().getName().replaceAll(" ", "");
			String dst = event.getDependable().getName().replaceAll(" ", "");
			
			try {
				ProcessorCommunicator.addEdgeIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.METHOD_CALL, null);
			} catch (MalformedSourcePositionException e) {
				// TODO Auto-generated catch block
				// Never occurs
				e.printStackTrace();
			}
		}

		@Override
		public void endClass(DependencyEvent event) {
		}

		@Override
		public void endSession(DependencyEvent event) {
		}
		
	}

	@Override
	public String formatAtom(String atom) {
		return atom.replaceAll("#", ".");
	}
}
