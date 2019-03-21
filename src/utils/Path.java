package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Hashtable;

import core.OptionsParser;

/**
 * Set files and folders names.
 */
public class Path {
	public static String dir_path_project;
	
	public static String dir_path_database;
	public static String blastall_path;
	public static String priam_db_path;
	public static String java_path;

	public static String file_name_region_nucl_split  = "temp_regions_nucl.fasta";
	public static String file_name_region_nc_split    = "temp_regions_nc.fasta";
	public static String file_name_region_prot_split  = "temp_regions_prot.fasta";
	
	public static File file_db_config;
	public static File file_services_config;
	public static File inputtranscriptfastafile;
	public static File svg_directory;
	public static File file_transcript_raw;
	public static File file_transcript_clean;
	public static File file_region_prot_id;
	public static File file_region_nucl_id;
	public static File file_region_prot;
	public static File file_region_nucl;
	public static File file_region_nc_id;
	public static File temp_directory;
	public static File file_all_annotations;
	public static File file_transcript_info;
	public static File file_region_info;
	
	public static BufferedWriter writer_transcript_clean;
	public static BufferedWriter writer_prot_id;
	public static BufferedWriter writer_nucl_id;
	public static BufferedWriter writer_prot;
	public static BufferedWriter writer_nucl;
	public static BufferedWriter writer_nc_id;
	public static BufferedWriter writer_region_info;
	public static BufferedWriter writer_transcript_info;
	
	public static Hashtable<String, String> path_services = new Hashtable<String, String>();
	
	public static void set_config_file() throws Exception{
		String path=Path.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		File bin_folder = new File(path);
		path=bin_folder.getParentFile().getAbsolutePath();
		file_db_config       = new File(path+"/database.config");
		file_services_config = new File(path+"/genotate.config");
	}

	/**
	 * Initialize the folders and files
	 * @throws Exception TASK
	 */
	public static void create_output_path() throws Exception{
		String output_directory_name;
		if(OptionsParser.path_output_dir == ""){
			String database_name = utils.Path.inputtranscriptfastafile.getName().replace(".fasta", "");
			output_directory_name = "out_genotate_"+database_name;
		}else{
			output_directory_name = OptionsParser.path_output_dir;
		}
		File output_directory = new File(output_directory_name);
		int dir_increment = 0;
		while(output_directory.exists()) {
			dir_increment++;
			output_directory = new File(output_directory_name+"_"+dir_increment);
		}
		
		temp_directory			= new File(output_directory + "/" + "temporary");
		svg_directory           = new File(output_directory + "/" + "svg");
		file_transcript_raw 	= new File(output_directory + "/" + "raw_transcripts.fasta");
		file_transcript_clean	= new File(output_directory + "/" + "transcripts.fasta");
		file_region_prot_id	    = new File(output_directory + "/" + "regions_prot_id.fasta");
		file_region_nucl_id 	= new File(output_directory + "/" + "regions_nucl_id.fasta");
		file_region_prot	    = new File(output_directory + "/" + "regions_prot.fasta");
		file_region_nucl 	    = new File(output_directory + "/" + "regions_nucl.fasta");
		file_region_nc_id	    = new File(output_directory + "/" + "regions_nc_id.fasta");
		file_transcript_info    = new File(output_directory + "/" + "transcripts_info.tab");
		file_region_info		= new File(output_directory + "/" + "regions_info.tab");
		file_all_annotations    = new File(output_directory + "/" + "all_annotations.tab");
		
		buildDir_withpermission(output_directory);
		buildDir_withpermission(temp_directory);
		buildDir_withpermission(svg_directory);
	}
	
	/**
	 * this method build a directory and set executable,writable,readable true for all users
	 * @param dir directory to build
	 */
	public static void buildDir_withpermission(File dir){
		dir.mkdirs();
		dir.setExecutable(true, false);
		dir.setWritable(true, true);
		dir.setReadable(true, true);
	}
	
	public static void initializeWriters() throws Exception {
		writer_transcript_clean = new BufferedWriter(new PrintWriter(file_transcript_clean, "UTF-8"));
		writer_prot_id          = new BufferedWriter(new PrintWriter(file_region_prot_id, "UTF-8"));
		writer_nucl_id          = new BufferedWriter(new PrintWriter(file_region_nucl_id, "UTF-8"));
		writer_prot             = new BufferedWriter(new PrintWriter(file_region_prot, "UTF-8"));
		writer_nucl             = new BufferedWriter(new PrintWriter(file_region_nucl, "UTF-8"));
		writer_nc_id            = new BufferedWriter(new PrintWriter(file_region_nc_id, "UTF-8"));
		writer_region_info      = new BufferedWriter(new PrintWriter(file_region_info, "UTF-8"));
		writer_transcript_info  = new BufferedWriter(new PrintWriter(file_transcript_info, "UTF-8"));
	}
	
	public static void closeWriters() throws Exception {
		writer_transcript_clean.close();
		writer_prot_id.close();
		writer_nucl_id.close();
		writer_prot.close();
		writer_nucl.close();
		writer_nc_id.close();
		writer_region_info.close();
		writer_transcript_info.close();
	}
	
	public static void load_path_config() throws Exception {
		System.out.println("Parsing genotate config file "+file_services_config);
		BufferedReader reader = new BufferedReader(new FileReader(file_services_config));
		String line=reader.readLine();
		while(line!=null){
			line = line.replace("\n", "").replace("\r", "").replace(" ", "");
			String[] tab_split = line.split(":");
			if(tab_split.length>1){
				String service = line.split(":")[0];
				String path = line.split(":")[1];
				path_services.put(service, path);
			}
			line=reader.readLine();
		}
		reader.close();
	}
}
