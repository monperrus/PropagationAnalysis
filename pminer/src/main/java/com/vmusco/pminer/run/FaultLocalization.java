package com.vmusco.pminer.run;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.analyze.MutantTestProcessingListener;
import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.pminer.exceptions.MissingDataException;
import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocalizationStatsWithMutantIfos;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.persistance.GraphML;
import com.vmusco.softminer.utils.SteimannDatasetTools;

/**
 * See http://www.feu.de/ps/prjs/EzUnit/eval/ISSTA13/ for dataset
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class FaultLocalization {

	private boolean first = true;
	protected int maxsize;
	protected MutationStatistics ms;

	protected FaultLocalization() {
	}
	
	protected static Options prepareOptions(){
		Options options = new Options();

		Option opt;

		opt = new Option("g", "graph", false, "Compute the wasted effort based on the graph interesection (default: false)");
		options.addOption(opt);

		opt = new Option(null, "no-tarantula", false, "Do not compute the tarantula wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-ochiai", false, "Do not compute the ochiai wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-naish", false, "Do not compute the naish wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-tarantulastar", false, "Do not compute the tarantula* wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-zoltar", false, "Do not compute the zoltar wasted effort (default: true)");
		options.addOption(opt);

		opt = new Option(null, "no-tarantula-graph", false, "Do not compute the tarantula wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-ochiai-graph", false, "Do not compute the ochiai wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-naish-graph", false, "Do not compute the naish wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-tarantulastar-graph", false, "Do not compute the tarantula* wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-zoltar-graph", false, "Do not compute the zoltar wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "steimann-dataset", false, "Indicates the data comes from Steimann dataset (as they requires preprocessing -- default: false)");
		options.addOption(opt);


		opt = new Option("F", "no-fallback", false, "If no path is found with the graph, do not use simple metric (default: false)");
		options.addOption(opt);

		opt = new Option("h", "help", false, "print this message");
		options.addOption(opt);

		opt = new Option("g", "graph", false, "Compute the wasted effort based on the graph interesection (default: false)");
		options.addOption(opt);

		opt = new Option(null, "no-tarantula", false, "Do not compute the tarantula wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-ochiai", false, "Do not compute the ochiai wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-naish", false, "Do not compute the naish wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-tarantulastar", false, "Do not compute the tarantula* wasted effort (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-zoltar", false, "Do not compute the zoltar wasted effort (default: true)");
		options.addOption(opt);

		opt = new Option(null, "no-tarantula-graph", false, "Do not compute the tarantula wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-ochiai-graph", false, "Do not compute the ochiai wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-naish-graph", false, "Do not compute the naish wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-tarantulastar-graph", false, "Do not compute the tarantula* wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "no-zoltar-graph", false, "Do not compute the zoltar wasted effort filtered by software graph (default: true)");
		options.addOption(opt);
		opt = new Option(null, "steimann-dataset", false, "Indicates the data comes from Steimann dataset (as they requires preprocessing -- default: false)");
		options.addOption(opt);


		opt = new Option("F", "no-fallback", false, "If no path is found with the graph, do not use simple metric (default: false)");
		options.addOption(opt);

		opt = new Option("h", "help", false, "print this message");
		options.addOption(opt);
		
		return options;
	}
	
	public void run(CommandLine cmd) throws Exception{
		this.ms = MutationStatistics.loadState(cmd.getArgs()[0]);	

		Graph base = null;

		if(cmd.getArgList().size() > 1){
			base = giveMeAGraph();

			GraphML gml = new GraphML(base);
			gml.load(new FileInputStream(new File(cmd.getArgs()[1])));
		}

		if(cmd.hasOption("steimann-dataset")){
			SteimannDatasetTools.adaptGraph(base);
		}

		FaultLocalizationStatsWithMutantIfos stats = new FaultLocalizationStatsWithMutantIfos(ms, base);
		List<DisplayData> notifier = parseArgsForNotifiers(cmd, stats, base, !cmd.hasOption("no-fallback"));

		MutationStatisticsCollecter mscIntersect  = new MutationStatisticsCollecter();
		for(MutantTestProcessingListener<MutationStatisticsCollecter> l : notifier){
			mscIntersect.addListener(l);
		}

		int cpt = 0;
		String result = prepareHeader(notifier);
		
		preinit(base);
		
		while(moreToProceed()){
			final String[] all = giveMeSomeData(base);
			int nbedges = base.getNbEdges();
			
			for(String m : all){
				MutantIfos mi = ms.getExternalDeepLoaded(m);

				stats.changeMutantIdentity(mi);

				Graph g = stats.getLastIntersectedGraph();

				String[] interNodesList = new String[0];
				if(g != null)
					interNodesList = g.getNodesNames();
				
				mscIntersect.intersectionFound(mi, new String[]{mi.getMutationIn()}, interNodesList);

				Iterator<MutantTestProcessingListener<MutationStatisticsCollecter>> listenerIterator = mscIntersect.listenerIterator();

				boolean abort = false;
				String intermBuffer = "";
				while(listenerIterator.hasNext()){
					DisplayData next = (DisplayData)listenerIterator.next();
					if(next.getLastOutline() == null){
						abort = true;
						break;
					}
						
					intermBuffer += ";"+next.getLastWastedEffort();
				}
				

				if(!abort){
					result += printEntry(++cpt, nbedges, interNodesList.length, m, intermBuffer);
				}
			}
			mscIntersect.executionEnded();
		}
		
		System.out.println(result);
	}
	
	protected String printEntry(int cpt, int nbedges, int interNodesList, String m, String intermBuffer) {
		String result = String.format("%d;%d;%s;%d", cpt, maxsize, m, nbedges);
		result += ";"+interNodesList;
		result += intermBuffer;
		result += '\n';
		
		return result;
	}

	protected String[] giveMeSomeData(Graph base) throws Exception {
		String[] all = ms.listViableAndRunnedMutants(false);
		maxsize = all.length;
		return all;
	}

	protected boolean moreToProceed() {
		if(first ){
			first = false;
			return true;
		}else{
			return false;
		}
	}

	protected void preinit(Graph base) throws Exception {
		// Nothing to do here...
	}

	protected String prepareHeader(List<DisplayData> notifier) {
		String result = "count;max;mutid;#E";

		result += String.format(";#inter");

		for(DisplayData dd : notifier){
			result += String.format(";%s", dd.getHeader().split(";")[1]);
		}
		result += '\n';
		return result;
	}

	protected Graph giveMeAGraph() {
		return new GraphStream();
	}

	public static void main(String[] args) throws Exception {
		Options options = prepareOptions();

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if( cmd.getArgs().length < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();

			String head = "Compute the wasted effort for each mutation in <mutation-file>. If a metric require graph assistance, use the graph in <graph-file>.";
			String foot = "";

			formatter.printHelp("[options] <mutation-file> [<graph-file>]", head, options, foot);
			System.exit(0);
		}
		
		FaultLocalization fl = new FaultLocalization();
		fl.run(cmd);
	}




	private static List<DisplayData> parseArgsForNotifiers(CommandLine cmd, FaultLocalizationStatsWithMutantIfos stats, Graph base, boolean fallback) throws MissingDataException {
		List<DisplayData> notifier = new ArrayList<DisplayData>();

		if(cmd.hasOption("graph")){
			notifier.add(new DisplayData(FaultLocators.getIntersectionMaxSizeApporach(stats), base, fallback, true, "G"));
		}

		if(!cmd.hasOption("no-tarantula")){
			notifier.add(new DisplayData(FaultLocators.getTarantula(stats), "T"));
		}


		if(!cmd.hasOption("no-tarantula-graph")){
			if(base == null)
				throw new MissingDataException("A graph is required.");
			notifier.add(new DisplayData(FaultLocators.getTarantula(stats), base, fallback, "TG"));
		}

		if(!cmd.hasOption("no-ochiai")){
			notifier.add(new DisplayData(FaultLocators.getOchiai(stats), "O"));
		}


		if(!cmd.hasOption("no-ochiai-graph")){
			if(base == null)
				throw new MissingDataException("A graph is required.");
			notifier.add(new DisplayData(FaultLocators.getOchiai(stats), base, fallback, "OG"));
		}


		if(!cmd.hasOption("no-naish")){
			notifier.add(new DisplayData(FaultLocators.getNaish(stats), "N"));
		}


		if(!cmd.hasOption("no-naish-graph")){
			if(base == null)
				throw new MissingDataException("A graph is required.");
			notifier.add(new DisplayData(FaultLocators.getNaish(stats), base, fallback, "NG"));
		}


		if(!cmd.hasOption("no-tarantulastar")){
			notifier.add(new DisplayData(FaultLocators.getTarantulaStar(stats), "T*"));
		}


		if(!cmd.hasOption("no-tarantulastar-graph")){
			if(base == null)
				throw new MissingDataException("A graph is required.");
			notifier.add(new DisplayData(FaultLocators.getTarantulaStar(stats), base, fallback, "T*G"));
		}


		if(!cmd.hasOption("no-zoltar")){
			notifier.add(new DisplayData(FaultLocators.getZoltar(stats), "Z"));
		}


		if(!cmd.hasOption("no-zoltar-graph")){
			if(base == null)
				throw new MissingDataException("A graph is required.");
			notifier.add(new DisplayData(FaultLocators.getZoltar(stats), base, fallback, "ZG"));
		}

		return notifier;
	}




	protected static class DisplayData implements MutantTestProcessingListener<MutationStatisticsCollecter> {

		private Graph base;
		private MutationStatisticsCollecter a;
		private String headCol;
		private FaultLocalizationScore fls;
		private String outline;
		private boolean fallback;
		private boolean fbminus;

		public DisplayData(FaultLocalizationScore fls, Graph base, boolean fallback, boolean fbminus, String headCol) {
			this.fls = fls;
			this.fallback = fallback;
			this.fbminus = fbminus;
			this.base = base;
			this.headCol = headCol;
		}
		
		public DisplayData(FaultLocalizationScore fls, Graph base, boolean fallback, String headCol) {
			this(fls, base, fallback, false, headCol);
		}
		
		public DisplayData(FaultLocalizationScore fls, String headCol) {
			this(fls, null, false, false, headCol);
		}
		

		public String getHeader(){
			String ret = "";
			ret += "+S"+headCol+";+W"+headCol;
			return ret;
		}

		@Override
		public void aMutantHasBeenProceeded(MutationStatisticsCollecter a) {
			this.a = a;

			//SOUDStatistics soud = a.getSoud();

			String outline = "";

			try{
				outline = String.format(";%.2f;%d", score(), wastedEffort());
				this.outline = outline;
			}catch(TargetNotFoundException e){
				this.outline = null;
				return;
			} catch (BadStateException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		public int wastedEffort() throws BadStateException, TargetNotFoundException{
			if(base != null){
				String[] graphCases = a.getLastGraphDetermined();
				try{
					return fls.wastedEffort(graphCases, a.getLastMutantIfos().getMutationIn());
				}catch(TargetNotFoundException e){
					if(fallback){
						if(fbminus){
							return -1;
						}else{
							return fls.wastedEffort(a.getLastMutantIfos().getMutationIn());
						}
					}else{
						throw e;
					}
				}
			}else{
				return fls.wastedEffort(a.getLastMutantIfos().getMutationIn());
			}
		}

		public String getLastOutline(){
			return outline;
		}

		public String getLastWastedEffort(){
			return outline.split(";")[2];
		}

		public String getLastScore(){
			return outline.split(";")[1];
		}

		/**
		 * Not working with graphs
		 * @return
		 * @throws BadStateException 
		 */
		public double score() throws BadStateException{
			MutantIfos mi = a.getLastMutantIfos();
			fls.computeScore(mi.getMutationIn());
			return fls.getScore();
		}

		static interface HeaderPrinter{
			String headerText();
		}
	}

}
