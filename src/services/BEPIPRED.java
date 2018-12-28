package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * BEPIPRED
 */
public class BEPIPRED extends SERVICE  {
	public static String path;
	private static double score_threshold=0.5;

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
		if(path_services.containsKey("BEPIPRED")){
			path = path_services.get("BEPIPRED");
		}else{
			throw new Exception("Path to BEPIPRED not configured");
		}
		if(options.size()>0){
			score_threshold = Double.parseDouble(options.get(0));
		}
		String command = path + " "+fastatempprot.getAbsolutePath()+" > "+computing_file.getAbsolutePath();
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
		int idx_posbegin=3;
		int idx_shortid = 0;
		int idx_score = 5;
		int epitope_begin = -1;
		int previous_pos = -1;
		int previous_region_id = -1;
		Annotation annot = null;
		String line;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("#") ) {
					continue;
				}
				String[] splittab = line.replaceAll("\\s+", " ").trim().split(" ");
				int region_id = Integer.parseInt(splittab[idx_shortid]);
				int pos=Integer.parseInt(splittab[idx_posbegin])*3;
				Double score =Double.parseDouble(splittab[idx_score]);
				if(epitope_begin == -1 && score >= score_threshold){
					epitope_begin = pos;
					continue;
				}
				if(region_id == previous_region_id && epitope_begin != -1 && score < score_threshold){
					if((previous_pos - epitope_begin) > 9){
						String description = "";
						String color = "(64, 0, 64)";
						String service = "BEPIPRED";
						String name = "B-cell linear epitope";
						annot = new Annotation(service,name,description,epitope_begin,previous_pos,color,region_id);
						annotations.add(annot);
					}
					epitope_begin = -1;
				}
				if(region_id != previous_region_id && epitope_begin != -1){
					if((previous_pos - epitope_begin) > 9){
						String description = "";
						String color = "(64, 0, 64)";
						String service = "BEPIPRED";
						String name = "B-cell linear epitope";
						annot = new Annotation(service,name,description,epitope_begin,previous_pos,color,previous_region_id);
						annotations.add(annot);
					}
					epitope_begin = -1;
				}
				previous_region_id = region_id;
				previous_pos = pos;
			}catch (Exception ex) {
				continue;
			}
		}
		reader.close();
		return annotations;
	}
}
