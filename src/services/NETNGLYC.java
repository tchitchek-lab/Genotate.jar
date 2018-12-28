package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * NETNGLYC
 */
public class NETNGLYC extends SERVICE  {
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
		if(path_services.containsKey("NETNGLYC")){
			path = path_services.get("NETNGLYC");
		}else{
			throw new Exception("Path to NETNGLYC not configured");
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
		int idx_posbegin=1;
		int idx_shortid = 0;
		int idx_score = 3;
		Annotation annot = null;
		String line;
		Boolean skip = true;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("---------") && !skip) {
					skip=true;
					continue;
				}
				if(line.startsWith("---------") && skip) {
					skip=false;
					line=reader.readLine();//SeqName        Position      Potential          Jury          N-Glyc
					line=reader.readLine();//				              agreement       result
					line=reader.readLine();//----------------------------------------------------------------------//
					continue;              //sp|Q12882|DPYD_HUMAN   173 NPSL		0.6728		(9/9)		  ++
				}
				if(skip) {
					continue;
				}
				line = line.replaceAll("\\t+", " ").replaceAll("\\s+", " ").trim();
				String[] splittab = line.split(" ");
				Double score =Double.parseDouble(splittab[idx_score]);
				if(score_threshold > score){
					continue;
				}
				int region_id = Integer.parseInt(splittab[idx_shortid]);
				int pos_begin=Integer.parseInt(splittab[idx_posbegin])*3;
				int pos_end=pos_begin+2;
				String description = "";
				String color = "(64, 0, 64)";
				String service = "NETNGLYC";
				String name = "N-linked glycosylation site";
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
