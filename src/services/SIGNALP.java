package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import objects.Annotation;

/**
 * SIGNALP
 */
public class SIGNALP extends SERVICE  {
	public static String path;
	public static String kingdom = "eukaryote";
	private static double score_threshold=0.45;

	/**
	 * Create the shell command for the tool execution 
	 * @param result_file the annotation service output file
	 * @param fastatempnucl the region nucleic fasta file
	 * @param fastatempprot the region proteic fasta file
	 * @param options the options
	 * @param path_services a map with the executable path for each service
	 * @return command
	 * @throws Exception TASK
	 */
	@Override
	public String run(File computing_file,File result_file,File fastatempnucl,File fastatempnc,File fastatempprot,List<String> options,Hashtable<String, String> path_services) throws Exception{
		if(path_services.containsKey("SIGNALP")){
			path = path_services.get("SIGNALP");
		}else{
			throw new Exception("Path to SIGNALP not configured");
		}
		if(options.size()>0){
			score_threshold = Double.parseDouble(options.get(0));
		}
		String command = path + " -t euk -f short "+fastatempprot.getAbsolutePath()+" > "+computing_file.getAbsolutePath();
		if(kingdom == "prokaryote"){
			command = path + " -t gram+ -f short "+fastatempprot.getAbsolutePath()+" > "+computing_file.getAbsolutePath();
			command += " && "+path + " -t gram- -f short "+fastatempprot.getAbsolutePath()+" >> "+computing_file.getAbsolutePath();
		}
		command += " && mv "+computing_file.getAbsolutePath()+" "+result_file.getAbsolutePath()+" ";
		return command;
	}

	/**
	 * Parse the result file and return an array of annotations
	 * @param result_file the result file
	 * @throws Exception TASK
	 * @return Annotations array
	 */
	@Override
	public ArrayList<Annotation> parse(File result_file) throws Exception{
		ArrayList<Annotation> annotations =new ArrayList<Annotation>();
		BufferedReader reader = new BufferedReader(new FileReader(result_file));
		int idx_posend=4;
		int idx_shortid = 0;
		int idx_score = 5;
		Annotation annot = null;
		String line;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("#") ) {
					continue;
				}
				String[] splittab = line.replaceAll("\\s+", " ").trim().split(" ");
				Double score =Double.parseDouble(splittab[idx_score]);
				if(score_threshold > score){
					continue;
				}
				int region_id = Integer.parseInt(splittab[idx_shortid]);
				int pos_begin=0;
				int pos_end=((Integer.parseInt(splittab[idx_posend])-1)*3)-1;
				String description = "";
				String color = "(64, 0, 64)";
				String service = "SIGNALP";
				String name = "signal peptide";
				annot = new Annotation(service,name,description,pos_begin,pos_end,color,region_id);
				annotations.add(annot);
			}catch (Exception ex) {
				continue;
			}
		}
		reader.close();
		return annotations;
	}
}
