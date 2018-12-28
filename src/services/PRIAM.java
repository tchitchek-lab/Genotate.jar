package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * PRIAM
 */
public class PRIAM extends SERVICE {
	public static String path;
	private static double evalue_threshold=0.05;

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
		String blastall = "path to blastall not configured";
		String java = "path to java not configured";
		String priam_db = "path to priam_db not configured";
		if(path_services.containsKey("JAVA")){
			java = path_services.get("JAVA")+" -jar ";
		}else{
			throw new Exception("Path to JAVA not configured");
		}
		if(path_services.containsKey("BLASTALL")){
			blastall = path_services.get("BLASTALL");
		}else{
			throw new Exception("Path to BLASTALL not configured");
		}
		if(path_services.containsKey("PRIAM")){
			path = path_services.get("PRIAM");
		}else{
			throw new Exception("Path to PRIAM not configured");
		}
		if(path_services.containsKey("PRIAM_DB")){
			priam_db = path_services.get("PRIAM_DB");
		}else{
			throw new Exception("Path to PRIAMDB not configured");
		}
		if(options.size()>0){
			evalue_threshold = Double.parseDouble(options.get(0));
		}
		String command=java+" "+path+" -bd "+blastall
				+ " -np 32 -i "+fastatempprot.getAbsolutePath()
				+ " -p "+priam_db+" -o "+result_file.getParent()+"/PRIAM_OUT -n priam_out "
				+ " && mv "+result_file.getParent()+"/PRIAM_OUT/PRIAM_priam_out/TMP/sequenceHits.tab "+result_file.getAbsolutePath();
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

		ArrayList<Annotation> annotations_array=new ArrayList<Annotation>();
		BufferedReader reader = new BufferedReader(new FileReader(result_file));

		int idx_region_id=0;
		int idx_name=15;
		int idx_posbegin=5;
		int idx_posend=6;
		int idx_evalue=11;

		//0:query_ID
		//1:profile_ID
		//2:query_length
		//3:profile_length
		//4:align_length
		//5:query_from
		//6:query_to
		//7:profile_from
		//8:profile_to
		//9:profile_proportion
		//10:bit_score
		//11:e-value
		//12:positive_hit_probability
		//13:is_best_overlap
		//14:found_catalytic_pattern
		//15:profile_linked_ECs

		Annotation annot = null;
		String line;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("query_ID") ){
					continue;
				}
				String[] splittab= (line.replaceAll("\\s+", " ").trim()).split(" ");
				int region_id = Integer.parseInt(splittab[idx_region_id]);
				int pos_begin=Integer.parseInt(splittab[idx_posbegin])*3;
				int pos_end=Integer.parseInt(splittab[idx_posend])*3-1;
				Double evalue=1.0;
				if(splittab[idx_evalue].contains("0E0")){
					evalue=0.0;
				}else{
					evalue=Double.parseDouble(splittab[idx_evalue]);
				}
				if(evalue > evalue_threshold){
					continue;
				}
				String name=splittab[idx_name].replace("(", "").replace(")", "").replace(";", ",");
				String description = "";
				String color = "(64, 64, 0)";
				String service = "PRIAM";
				String[] name_split = name.split(",");
				for (String name_tmp : name_split) {
					annot = new Annotation(service,name_tmp,description,pos_begin,pos_end,color,region_id);
					annotations_array.add(annot);
				}
			}catch (Exception ex) {
				continue;
			}
		}
		reader.close();
		return annotations_array;
	}
}
