package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * PROP
 */
public class PROP extends SERVICE {
	public static String path;
	private static double score_threshold=0.2;

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
		if(path_services.containsKey("PROP")){
			path = path_services.get("PROP");
		}else{
			throw new Exception("Path to PROP not configured");
		}
		if(options.size()>0){
			score_threshold = Double.parseDouble(options.get(0));
		}
		String command = path+" -v "+fastatempprot.getAbsolutePath()+" > "+computing_file.getAbsolutePath();
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
		int idx_pos_cleavage_site=1;
		int idx_shortid = 0;
		int orf_id = 0;
		int idx_score = 3;

		int nbline_header_resfile=8;
		for(int i=0;i<nbline_header_resfile;i++){
			reader.readLine();
		}
		Annotation annot = null;
		String line;
		while((line=reader.readLine())!=null){
			try {
				String[] splittab = line.replaceAll("\\s+", " ").trim().split(" ");
				if(splittab.length<10 || splittab[10].equals(".")){
					continue;
				}
				Double score =Double.parseDouble(splittab[idx_score]);
				if(score_threshold > score){
					continue;
				}
				orf_id = Integer.parseInt(splittab[idx_shortid]);
				int position_cleavage = Integer.parseInt(splittab[idx_pos_cleavage_site]);
				String cleavage_seq = splittab[11].replace("|", "").replace("-", "X");
				String name = "cleavage site: "+cleavage_seq;
				int seq_size_before = splittab[11].split("\\|")[0].length();
				int seq_size_after = splittab[11].split("\\|")[1].length();
				int pos_begin = (position_cleavage - seq_size_before)*3;
				int pos_end = (position_cleavage + seq_size_after)*3-1;
				String description = "";
				String color = "(0, 64, 64)";
				String service = "PROP";
				annot = new Annotation(service,name,description,pos_begin,pos_end,color,orf_id);
				annotations.add(annot);
			}catch (Exception ex) {
				continue;
			}
		}
		reader.close();
		return annotations;

	}
}
