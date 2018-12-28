package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import objects.Annotation;

/**
 * INTERPROSCAN
 */
public class INTERPROSCAN extends SERVICE{
	public static String path;
	public static Hashtable<String, Double> services = new Hashtable<String, Double>();
	private static Map<String, String> name_to_color = new HashMap<String, String>();
	static{
		name_to_color.put("CDD",             "(255, 0, 0)");
		name_to_color.put("COILS",           "(0, 255, 0)");
		name_to_color.put("GENE3D",          "(0, 0, 255)");
		name_to_color.put("HAMAP",           "(0, 255, 255)");
		name_to_color.put("MOBIDBLITE",      "(255, 0, 255)");
		name_to_color.put("PANTHER",         "(255, 255, 0)");
		name_to_color.put("PFAM",            "(192, 0, 0)");
		name_to_color.put("PIRSF",           "(0, 192, 0)");
		name_to_color.put("PRINTS",          "(0, 0, 192)");
		name_to_color.put("PRODOM",          "(0, 192, 192)");
		name_to_color.put("PROSITEPATTERNS", "(192, 0, 192)");
		name_to_color.put("PROSITEPROFILES", "(192, 192, 0)");
		name_to_color.put("SFLD",            "(0, 128, 128)");
		name_to_color.put("SMART",           "(128, 0, 128)");
		name_to_color.put("SUPERFAMILY",     "(128, 128, 0)");
		name_to_color.put("TIGRFAM",         "(128, 128, 128)");
	}

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
		if(path_services.containsKey("INTERPROSCAN")){
			path = path_services.get("INTERPROSCAN");
		}else{
			throw new Exception("Path to INTERPROSCAN not configured");
		}
		String command = path + " -iprlookup -goterms --pathways -ms 30 ";
		String commandoutput	= " -f tsv -o ";
		String commandinput	= " -i ";
		command += commandoutput+computing_file.getAbsolutePath()+commandinput+fastatempprot.getAbsolutePath()+" -appl "+options.get(0);
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
		ArrayList<Annotation> annotations_array=new ArrayList<Annotation>();
		BufferedReader reader = new BufferedReader(new FileReader(result_file));
		
		int idx_region_id=0;
		int idx_servicesip=3;
		int idx_id=4;
		int idx_name=5;
		int idx_pos_begin =6 ;
		int idx_pos_end =7 ;
		int idx_evalue = 8;
		int idx_description = 11;
		Annotation annot = null;
		
		String line;
		while((line=reader.readLine())!=null){
			try {
				String[] splitline= line.split("\t");
				int region_id = Integer.parseInt(splitline[idx_region_id]);
				int pos_begin = Integer.parseInt(splitline[idx_pos_begin])*3;
				int pos_end = Integer.parseInt(splitline[idx_pos_end])*3-1;
				String name = splitline[idx_id] + " " + splitline[idx_name];
				String service = splitline[idx_servicesip].toUpperCase();
				String evaluestring = splitline[idx_evalue];
				double evalue;
				if(evaluestring.contains("E")){
					evalue=new BigDecimal(evaluestring).doubleValue();
				}else if(!evaluestring.equals("-")){
					evalue=Double.parseDouble(evaluestring);
				}else{
					evalue=0.0;
				}
				String description = "";
				for(int i = idx_description; i < splitline.length; i++){
					description += splitline[i] + ", ";
				}
				if(evalue > services.get(service)){
					continue;
				}
				annot = new Annotation(service,name,description,pos_begin,pos_end,name_to_color.get(service),region_id);
				annotations_array.add(annot);
			}catch (Exception ex) {
				continue;
			}
		}
		reader.close();
		return annotations_array;
	}
}
