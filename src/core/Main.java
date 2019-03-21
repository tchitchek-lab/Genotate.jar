package core;

import java.util.Hashtable;
import java.util.List;

/**
 * Launcher of genotate.
 */
public class Main {
	/**
	 * Launcher of genotate. Parse the arguments and compute the annotation.
	 * @param args Genotate options
	 * @throws Exception TASK
	 */
	public static void main(String[] args) throws Exception {
		utils.Path.set_config_file();
		OptionsParser.options_parser(args);
		
		System.out.println("Create output directory");
		utils.Path.create_output_path();
		utils.FileUtils.copy(utils.Path.inputtranscriptfastafile, utils.Path.file_transcript_raw);
		
		Hashtable<String, List<String>> services_map = new Hashtable<String, List<String>>();
		if (OptionsParser.services_raw.length()<3){
			System.out.println("no annotations services will be used");
		}else{
			System.out.println("annotation will be computed for: "+OptionsParser.services_raw);
			services_map = OptionsParser.services_parser();
			System.out.println("Load genotate configuration file");
			utils.Path.load_path_config();
		}

		System.out.println("Parse input sequences and search for open reading frame");
		FindOrf.search_orf();
		if(OrfWorker.use_cpat && FindOrf.nb_identified_orf > 0) {
			System.out.println("Check coding potential of each ORF");
			OrfWorker MyOrfWorker = new OrfWorker();
			MyOrfWorker.Launcher();
		}

		if (services_map.size()>0){
			System.out.println("Launch the services on the region");
			AnnotRegion.sample_and_annot(services_map);
			System.out.println("Create Svg Files");
			SVG.createSvgFiles();
		}else{
			System.out.println("Create Svg Files");
			SVG.createSvgFiles();
		}
	}
}
