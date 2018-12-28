package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * MHCII
 */
public class MHCII extends SERVICE  {
	public static String path;
	private static double score_threshold=1;
	Hashtable<Integer, Integer> map_seq_to_id = new Hashtable<Integer, Integer>();

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
		if(path_services.containsKey("MHCII")){
			path = path_services.get("MHCII");
		}else{
			throw new Exception("Path to MHCII not configured");
		}
		if(options.size()>0){
			score_threshold = Double.parseDouble(options.get(0));
		}
		BufferedReader reader = new BufferedReader(new FileReader(fastatempprot));
		String line;
		int nbseq=1;
		while((line=reader.readLine())!=null){
			int region_id = Integer.parseInt(line.replace(">", ""));
			reader.readLine();//skip the sequence
			map_seq_to_id.put(nbseq, region_id);
			nbseq++;
		}
		reader.close();
		String command = path + " IEDB_recommended HLA-DRB1*03:01 "+fastatempprot.getAbsolutePath()+" >> "+computing_file.getAbsolutePath();
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
		int idx_posbegin=2;
		int idx_posend=3;
		int idx_shortid = 1;
		int idx_score = 6;
		Annotation annot = null;
		String description = "";
		String color = "(64, 0, 64)";
		String service = "MHCII";
		String name = "MHC-II binding site";
		int tmp_region_id = -1;
		int tmp_begin = -1;
		int tmp_end = -1;
		String line;
		while((line=reader.readLine())!=null){
			try {
				String[] splittab = line.trim().split("\t");
				Double score =Double.parseDouble(splittab[idx_score]);
				if(score_threshold < score){
					continue;
				}
				int region_id = map_seq_to_id.get(Integer.parseInt(splittab[idx_shortid]));
				int pos_begin=Integer.parseInt(splittab[idx_posbegin])*3;
				int pos_end=Integer.parseInt(splittab[idx_posend])*3;
				if(pos_begin == pos_end){
					pos_end = pos_end+2;
				}
				if(tmp_region_id != -1){
					if(tmp_region_id == region_id && tmp_end >= pos_begin){
						pos_begin = tmp_begin;
					}else{
						annot = new Annotation(service,name,description,tmp_begin,tmp_end,color,tmp_region_id);
						annotations.add(annot);
					}
				}
				tmp_region_id = region_id;
				tmp_begin = pos_begin;
				tmp_end = pos_end;
			}catch (Exception ex) {
				continue;
			}
		}
		if(tmp_region_id != -1){
			annot = new Annotation(service,name,description,tmp_begin,tmp_end,color,tmp_region_id);
			annotations.add(annot);
		}
		reader.close();
		return annotations;
	}
}
