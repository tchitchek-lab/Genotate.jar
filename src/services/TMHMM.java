package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * TMHMM
 */
public class TMHMM extends SERVICE {
	public static String path;

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
		if(path_services.containsKey("TMHMM")){
			path = path_services.get("TMHMM");
		}else{
			throw new Exception("Path to TMHMM not configured");
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
	public ArrayList<Annotation>  parse(File result_file) throws NumberFormatException, Exception{

		ArrayList<Annotation> annotations=new ArrayList<Annotation>();
		BufferedReader reader = new BufferedReader(new FileReader(result_file));

		int idx_shortid=0;
		int idx_name=2;
		int idx_posbegin=3;
		int idx_posend=4;
		Annotation annot = null;
		String line;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("#") ){
					continue;
				}
				String[] splittab= (line.replaceAll("\\s+", " ").trim()).split(" ");
				int region_id = Integer.parseInt(splittab[idx_shortid]);
				int pos_begin=Integer.parseInt(splittab[idx_posbegin])*3;
				int pos_end=Integer.parseInt(splittab[idx_posend])*3-1;
				String name=splittab[idx_name];
				String description = "";
				String color = "(64, 64, 64)";
				String service = "TMHMM";
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
