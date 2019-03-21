package core;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class OptionsParser {
	
	public static String path_input_file;
	public static String path_output_dir;
	public static String services_raw;
	private static List<String> interproscan_services_available;
	private static String help = "\n\n"
			+ "\n======================================="
			+ "\nWelcome in genotate annotation platform"
			+ "\n======================================="
			+ "\nPlease ensure the file genotate.config is at the same location that genotate.jar"
			+ "\n"
			+ "\nUsage : genotate.jar -in nucleic_sequences.fasta -out test -services service1,service2[score]"
			+ "\n"
			+ "\nExemple : genotate.jar -in nucleic_sequences.fasta -out test -services PRIAM,SABLE,CDD,COILS,BLASTN[db1,85,50,50],BLASTP[db2,85,50,50] "
			+ "\n-find_inner_region -find_outside_region -region_min_size 300 -region_by_run 100 -threads 8 -start_codon ATG -stop_codon TAG,TGA,TAA"
			+ "\n"
			+ "\nServices availables:"
			+ "\nBLASTN          [database,identity,query cover,subject cover] by default 85,50,50 (min 0 to max 100)"
			+ "\nBLASTP          [database,identity,query cover,subject cover] by default 85,50,50 (min 0 to max 100)"
			+ "\nBEPIPRED        [score]  by default threshold = 0.5 (min 0 to max 1)"
			+ "\nMHCI            [score]  by default threshold = 1   (min 0 to max 2)"
			+ "\nMHCII           [score]  by default threshold = 1   (min 0 to max 2)"
			+ "\nNETCGLYC        [score]  by default threshold = 0.5 (min 0 to max 1)"
			+ "\nNETNGLYC        [score]  by default threshold = 0.5 (min 0 to max 1)"
			+ "\nPROP            [score]  by default threshold = 0.2 (min 0 to max 1)"
			+ "\nRNAMMER         [score]  by default threshold = 0.5 (min 0 to max 1)"
			+ "\nSIGNALP         [score]  by default threshold = 0.45(min 0 to max 1)"
			+ "\nTRNASCANSE      [score]  by default threshold = 0.5 (min 0 to max 1)"
			+ "\nCDD             [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nCOILS           [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nGENE3D          [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nHAMAP           [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nMOBIDBLITE      [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPANTHER         [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPFAM            [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPIRSF           [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPRINTS          [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPRODOM          [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPROSITEPATTERNS [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nPROSITEPROFILES [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nSFLD            [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nSMART           [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nSUPERFAMILY     [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nTIGRFAM         [evalue] by default evalue = 0.05 (min 0 to max 1)"
			+ "\nTMHMM           no scores availables"
			+ "\n"
			+ "\nOptions availables:"
			+ "\n-input                         Input nucleic fasta file path"
			+ "\n-output                        Output folder path"
			+ "\n-services                      Services to run"
			+ "\n-services_messages             Display the services messages"
			+ "\n-inner_orf                     Allows orf contained in larger ones."
			+ "\n-outside_orf                   Allows partial orf lacking either a codon stop or a codon start."
			+ "\n-orf_min_size      100         Filter orf to keep only those long enough. The size is in nucleic bases"
			+ "\n-checkORF                      Use CPAT to check the ORF coding potential."
			+ "\n-checkORF_threshold 0.5        Set CPAT threshold."
			+ "\n-region_by_run     100         Number of region computed together"
			+ "\n-refresh_time      10          Waiting time in seconds between each results check"
			+ "\n-threads           8           Number of jobs computed at the same time"
			+ "\n-blast_threads     4           Number of BLAST workers computed at the same time"
			+ "\n-start_codon       ATG         Start codon(s) used to search for orf"
			+ "\n-stop_codon        TAG,TGA,TAA Stop codon(s) used to search for orf"
			+ "\n-ignore_reverse                Do not compute the annotation on the reverse strand"
			+ "\n-ignore_ncrna                  Do not compute the annotation of ncrna"
			+ "\n-kingdom           eukaryote   For signalp, compute for either the eukaryote or the prokaryote kingdom"
			+ "\n\n";

	/**
	 * This method intitialize_interproscan_services
	 */
	private static void intitialize_interproscan_services(){
		interproscan_services_available = new ArrayList<String>();
		interproscan_services_available.add("CDD");
		interproscan_services_available.add("COILS");
		interproscan_services_available.add("GENE3D");
		interproscan_services_available.add("HAMAP");
		interproscan_services_available.add("MOBIDBLITE");
		interproscan_services_available.add("PANTHER");
		interproscan_services_available.add("PFAM");
		interproscan_services_available.add("PIRSF");
		interproscan_services_available.add("PRINTS");
		interproscan_services_available.add("PRODOM");
		interproscan_services_available.add("PROSITEPATTERNS");
		interproscan_services_available.add("PROSITEPROFILES");
		interproscan_services_available.add("SFLD");
		interproscan_services_available.add("SMART");
		interproscan_services_available.add("SUPERFAMILY");
		interproscan_services_available.add("TIGRFAM");
	}
	
	/**
	 * This method parse the arguments given to genotate and set the parameters
	 * @param args genotate arguments
	 */
	public static void options_parser(String[] args){
		path_input_file = "";
		path_output_dir = "";
		services_raw = "";
		for(int i=0; i<args.length;i++){
			switch (args[i]){
			case "-input":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				path_input_file = args[i+1];
				System.out.println("Set input file: "+path_input_file);
				continue;
			case "-output":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				path_output_dir = args[i+1];
				System.out.println("Set output directory: "+path_output_dir);
				continue;
			case "-services":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				services_raw=args[i+1];
				services_raw = services_raw.replace(" ", "");
				System.out.println("Set services: "+services_raw);
				continue;
			case "-kingdom":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				String kingdom = args[i+1];
				kingdom = kingdom.replace(" ", "").toLowerCase();
				if(kingdom.equals("prokaryote")){
					services.RNAMMER.kingdom = "bac";
					services.SIGNALP.kingdom = "prokaryote";
				}
				System.out.println("Set kingdom: "+kingdom);
				continue;
			case "-services_messages":
				AnnotRegion.services_messages=true;
				System.out.println("Set services messages: "+AnnotRegion.services_messages);
				continue;
			case "-inner_orf":
				FindOrf.accept_inner_orf=true;
				System.out.println("Set inner orf: "+FindOrf.accept_inner_orf);
				continue;
			case "-outside_orf":
				FindOrf.accept_outside_orf=true;
				System.out.println("Set outside orf: "+FindOrf.accept_outside_orf);
				continue;
			case "-orf_min_size":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				FindOrf.orf_min_size=Integer.parseInt(args[i+1]);
				System.out.println("Set orf min size: "+FindOrf.orf_min_size);
				continue;
			case "-region_by_run":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				AnnotRegion.nb_region_by_run=Integer.parseInt(args[i+1]);
				System.out.println("Set number of region by run: "+AnnotRegion.nb_region_by_run);
				continue;
			case "-refresh_time":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				AnnotRegion.refresh_time=Integer.parseInt(args[i+1]);
				System.out.println("Set refresh time: "+AnnotRegion.refresh_time);
				continue;
			case "-threads":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				AnnotRegion.nb_concurent_thread=Integer.parseInt(args[i+1]);
				System.out.println("Set number of threads: "+AnnotRegion.nb_concurent_thread);
				continue;
			case "-blast_threads":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				services.BLASTN.threads=Integer.parseInt(args[i+1]);
				services.BLASTP.threads=Integer.parseInt(args[i+1]);
				System.out.println("Set number of BLAST threads: "+services.BLASTN.threads);
				continue;
			case "-start_codon":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				FindOrf.start_codons=args[i+1].split(",");
				System.out.println("Set start codon(s): "+args[i+1]);
				continue;
			case "-stop_codon":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				FindOrf.stop_codons=args[i+1].split(",");
				System.out.println("Set stop codon(s): "+args[i+1]);
				continue;
			case "-ignore_reverse":
				FindOrf.reverse=false;
				System.out.println("Set reverse region search: "+FindOrf.reverse);
				continue;
			case "-ignore_ncrna":
				FindOrf.ncrna=false;
				System.out.println("Set ncrna region annotation: "+FindOrf.reverse);
				continue;
			case "-checkORF":
				OrfWorker.use_cpat=true;
				System.out.println("Use CPAT to check ORF coding potential: "+OrfWorker.use_cpat);
				continue;
			case "-checkORF_threshold":
				if(args.length<i+2){
					System.out.println("Error while parsing options. Please use -h for help.");
					System.exit(0);
				}
				OrfWorker.cpat_threshold=Double.parseDouble(args[i+1]);
				System.out.println("CPAT threshold: "+OrfWorker.cpat_threshold);
				continue;
			case "-help":
				System.out.println(help);
				System.exit(0);
			case "-h":
				System.out.println(help);
				System.exit(0);
			}
		}
		if(path_input_file==""){
			System.out.println("Error: invalid options. Please use -h to see the documentation.");
			System.exit(0);
		}else{
			utils.Path.inputtranscriptfastafile = new File(path_input_file);
			if(!utils.Path.inputtranscriptfastafile.exists() || utils.Path.inputtranscriptfastafile.isDirectory()) {
				System.out.println("Input fasta file not found !");
				System.exit(0);
			}
		}
	}

	/**
	 * This method parse the services and return a map service to option
	 * @return services map service to option
	 */
	public static Hashtable<String, List<String>> services_parser(){
		intitialize_interproscan_services();
		Hashtable<String, List<String>> services_map = new Hashtable<String, List<String>>();
		String[] services_raw_split = services_raw.split(",");
		String interproscan_option = "";
		String cut_service = "";
		int nbblast = 1;
		List<String> options = new ArrayList<String>();
		for (String service : services_raw_split) {
			if(cut_service == ""){
				options = new ArrayList<String>();
			}
			if (cut_service != "" && service.contains("]")){
				String options_raw = service.substring(0,service.length()-1);
				options.add(options_raw);
				service = cut_service;
				cut_service = "";
			}
			if (cut_service != "" && !service.contains("]")){
				String options_raw = service;
				options.add(options_raw);
				continue;
			}
			if (service.contains("[") && !service.contains("]")){
				cut_service = service.substring(0,service.indexOf("["));;
				String options_raw = service.substring(service.indexOf("[")+1,service.length());
				options.add(options_raw);
				continue;
			}
			if (service.contains("[") && service.contains("]")){
				String options_raw = service.substring(service.indexOf("[")+1,service.length()-1);
				options.add(options_raw);
				service = service.substring(0,service.indexOf("["));
			}
			if (interproscan_services_available.contains(service)){
				Double evalue_threshold=0.05;
				if (options.size()>0) {
					try{
						evalue_threshold = Double.parseDouble(options.get(0));
					}catch (NumberFormatException e) {
						System.out.println("Error: option not valid: "+options.get(0));
						System.exit(0);
					}
				}
				services.INTERPROSCAN.services.put(service, evalue_threshold);
				interproscan_option+=service+", ";
			}else{
				for(int i = 0; i<options.size(); i++) {
					String option = options.get(i);
					if(i == 0 && service.equals("BLASTN")){
						continue;
					}
					if(i == 0 && service.equals("BLASTP")){
						continue;
					}
					try{
						Double.parseDouble(option);
					}catch (NumberFormatException e) {
						System.out.println("Error: for "+service+" option not valid: "+options.get(0));
						System.exit(0);
					}
				}
				if(service.contains("BLAST")){
					service=service+nbblast;
					nbblast++;
				}
				services_map.put(service, options);
			}
		}
		if(interproscan_option != ""){
			String service="INTERPROSCAN";
			List<String> interproscan_options = new ArrayList<String>();
			interproscan_option = interproscan_option.replaceAll(", $", "");
			interproscan_options.add(interproscan_option);
			services_map.put(service, interproscan_options);
		}
		return services_map;
	}
}
