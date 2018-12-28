package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * TRNASCANSE
 */
public class TRNASCANSE extends SERVICE {
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
		if(path_services.containsKey("TRNASCANSE") && path_services.containsKey("TRNASCANSE_ENV")){
			path = " source " + path_services.get("TRNASCANSE_ENV") + " ; " + path_services.get("TRNASCANSE");
		}else{
			throw new Exception("Path to TRNASCANSE not configured");
		}
		if(options.size()>0){
			score_threshold = Double.parseDouble(options.get(0));
		}
		String command = path + " "+fastatempnc.getAbsolutePath()+" > "+computing_file.getAbsolutePath();
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
	public ArrayList<Annotation>  parse(File result_file) throws NumberFormatException, Exception{

		ArrayList<Annotation> annotations=new ArrayList<Annotation>();
		BufferedReader reader = new BufferedReader(new FileReader(result_file));

		int idx_shortid=0;
		int idx_name=4;
		int idx_posbegin=2;
		int idx_posend=3;
		int idx_score = 8;
		Annotation annot = null;
		String line;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("#") ){
					continue;
				}
				String[] splittab= (line.replaceAll("\\s+", " ").trim()).split(" ");
				Double score = Double.parseDouble(splittab[idx_score]) / 100.0;
				if(score_threshold > score){
					continue;
				}
				int region_id = Integer.parseInt(splittab[idx_shortid]);
				int pos_begin=Integer.parseInt(splittab[idx_posbegin]);
				int pos_end=Integer.parseInt(splittab[idx_posend]);
				String name = "tRNA " + splittab[idx_name];
				String description = "";
				String color = "(64, 64, 64)";
				String service = "TRNASCANSE";
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
