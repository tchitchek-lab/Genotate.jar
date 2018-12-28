package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * SABLE
 */
public class SABLE extends SERVICE {
	public static String path;
	private static double score_threshold=5;

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
		if(path_services.containsKey("SABLE")){
			path = path_services.get("SABLE");
		}else{
			throw new Exception("Path to SABLE not configured");
		}
		if(options.size()>0){
			score_threshold = Double.parseDouble(options.get(0));
		}
		String command=" cp "+fastatempprot.getAbsolutePath()+" data.seq && " + path + " && mv OUT_SABLE_graph "+result_file.getAbsolutePath();
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
		
		//1st line -> query name (Query: [name])
		//2st line -> query sequence
		//3nd line -> predicted secondary structure (H -> helix, E -> beta strand, C -> )coil)
		//4rd line -> confidence level (from low confidence=3 to high confidence=9)
		//5rd line -> Space split Relative solvent accessibility prediction (fully Buried=00, fully Exposed=90)
		//6rd line -> confidence level (from low confidence=3 to high confidence=9)

		int region_id = 0;
		String name = null;
		int pos_begin = 0;
		int pos_end = 0;
		int current_begin = 0;
		int current_line = 1;
		String line;
		while((line=reader.readLine())!=null){
			try {
				if (current_line == 7){
					current_line = 1;
				}
				if (current_line == 1){
					region_id = Integer.parseInt(line.replace("Query: ",""));
					current_begin = 0;
				}
				if (current_line == 3){
					char structure_tmp = line.charAt(0);
					String line_score;
					String scores = "";
					line_score=reader.readLine();
					current_line++;
					for (int i = 0 ; i < line.length() ; i++) {
						char structure = line.charAt(i);
						if(structure == structure_tmp){
							scores += line_score.charAt(i);
						}else{
							pos_begin =current_begin*3;
							pos_end   =(i-1)*3-1;
							name="" + structure;
							int sum = 0;
						    for (int pos_score = 0; pos_score < scores.length(); pos_score++) {
						        sum += Integer.parseInt(""+scores.charAt(pos_score));
						    }
							float score = (float)sum / scores.length();
							if(structure == 'C'){
								name = "coil";
							}
							if(structure == 'H' && score > score_threshold){
								name = "alpha helix";
								String description = "";
								String color = "(255, 0, 0)";
								String service = "SABLE";
								Annotation annot = new Annotation(service,name,description,pos_begin,pos_end,color,region_id);
								annotations.add(annot);
							}
							if(structure == 'E' && score > score_threshold){
								name = "beta sheet";
								String description = "";
								String color = "(0, 255, 0)";
								String service = "SABLE";
								Annotation annot = new Annotation(service,name,description,pos_begin,pos_end,color,region_id);
								annotations.add(annot);
							}
							current_begin = i;
							structure_tmp = structure;
							scores = ""+line_score.charAt(i);
						}
					}
				}
			}catch (Exception ex) {
				//System.out.println(ex);
			}
			current_line++;
		}
		reader.close();
		return annotations;
	}
}
