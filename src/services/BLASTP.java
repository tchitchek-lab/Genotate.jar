package services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import objects.Annotation;

/**
 * BLASTP
 */
public class BLASTP extends SERVICE{
	public static int threads = 4;
	public static String path;
	public static String dir_path_database;
	public static int identity_threshold=85;
	public static int query_cover_threshold=50;
	public static int subject_cover_threshold=50;

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
		String output =" -outfmt \"6 pident qstart qend qlen sstart send slen bitscore qacc sacc salltitles\" -out ";
		String db = "No db path set";
		if(path_services.containsKey("BLAST")){
			path = path_services.get("BLAST")+"/blastp";
		}else{
			throw new Exception("Path to BLAST not configured");
		}
		if(path_services.containsKey("BLASTDB")){
			dir_path_database = path_services.get("BLASTDB");
		}else{
			throw new Exception("Path to BLASTDB not configured");
		}
		if(options.size()>0){
			db = options.get(0);
			if(options.size()>1){
				identity_threshold = Integer.parseInt(options.get(1));
				query_cover_threshold = Integer.parseInt(options.get(2));
				subject_cover_threshold = Integer.parseInt(options.get(3));
			}
		}
		String cmd = path+" -num_threads "+threads+" -query "+fastatempprot.getAbsolutePath()+output+computing_file.getAbsolutePath()+" -db "+dir_path_database+"/"+db;
		cmd += " && mv "+computing_file.getAbsolutePath()+" "+result_file.getAbsolutePath()+" ";
		return cmd;
	}
	
	/**
	 * parse
	 * @param result_file the result file
	 * @throws Exception TASK
	 * @return Annotations array
	 */
	@Override
	public  ArrayList<Annotation>  parse(File result_file) throws Exception{

		//executeCommand("sort -t$'\t' -k1,1 -k3,3 -k12,12 -n -r "+inputfilename+" > "+inputfilename+"_sorted.txt && mv "+inputfilename+"_sorted.txt "+inputfilename);
		ArrayList<Annotation> annotations_array=new ArrayList<Annotation>();
		BufferedReader reader = new BufferedReader(new FileReader(result_file));
		
		//0. percentage of identical subjectes
		//1. start of alignment in query
		//2. end of alignment in query
		//3. Query sequence length
		//4. start of alignment in subject
		//5. end of alignment in subject
		//6. Subject sequence length
		//7. bit score
		//8. Query accesion
		//9. Subject accession

		int idx_identity =0;
		int idx_posbegin =1;
		int idx_posend =2;
		int idx_length =3;
		int idx_subject_posbegin =4;
		int idx_subject_posend =5;
		int idx_subject_length =6;
		int idx_bitscore =7;
		int idx_region_id =8;
		int idx_name =9;
		int idx_description = 10;
		
		String line;
		Annotation annot = null;
		while((line=reader.readLine())!=null){
			try {
				if(line.startsWith("#") ){
					continue;
				}
				String[] splittab= line.split("\t");
				if(splittab.length < 11){
					continue;
				}
				int region_id = Integer.parseInt(splittab[idx_region_id]);
				String name = splittab[idx_name];
				float identity = Float.parseFloat(splittab[idx_identity]);
				int pos_begin = (Integer.parseInt(splittab[idx_posbegin])-1)*3;
				int pos_end = (Integer.parseInt(splittab[idx_posend]))*3-1;
				int length = Integer.parseInt(splittab[idx_length])*3;
				int pos_subject_begin = Integer.parseInt(splittab[idx_subject_posbegin]);
				int pos_subject_end = Integer.parseInt(splittab[idx_subject_posend]);
				int pos_subject_length = Integer.parseInt(splittab[idx_subject_length]);
				float bitscore = Float.parseFloat(splittab[idx_bitscore]);
				String description = "";
				for(int i = idx_description; i < splittab.length; i++){
					description += splittab[i] + ", ";
				}
				float query_cover = (((float)pos_end - pos_begin + 1)/length)*100;
				float subject_cover = (((float)pos_subject_end - pos_subject_begin + 1)/pos_subject_length)*100;
				description += "identity:"+identity+", query_cover:"+query_cover+", subject_cover:"+subject_cover+", bitscore:"+bitscore;
				String color = "(0, 64, 0)";
				String service = "BLASTP";
				if(query_cover < query_cover_threshold || subject_cover < subject_cover_threshold || identity < identity_threshold){
					continue;
				}
				annot = new Annotation(service,name,description,pos_begin,pos_end,color,region_id);
				annotations_array.add(annot);
			}catch (Exception ex) {
				continue;
			}
		}
		reader.close();
		return annotations_array;
	}
}
