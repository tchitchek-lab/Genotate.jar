package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import objects.Annotation;
import objects.WorkerResults;
import utils.Path;

/**
 * Annotate a region
 */
public class AnnotRegion {
	
	public static int refresh_time = 10;
	public static int nb_region_by_run = 100;
	public static int nb_concurent_thread = 8;
	public static Boolean services_messages = false;
	public static Hashtable<Integer, ArrayList<Annotation>> annotation_map = new Hashtable<Integer, ArrayList<Annotation>>();
	private static List<Callable<Object>> array_thread = new ArrayList<Callable<Object>>();
	
	/**
	 * Take the full files of region. Create new files of [N] region. Initialize a worker for each.
	 * Launch the computation of all the workers.
	 * @param services TASK
	 * @throws java.lang.Exception TASK
	 */
	public static void sample_and_annot(Hashtable<String, List<String>> services) throws Exception {
		BufferedReader reader_nucl = new BufferedReader(new FileReader(utils.Path.file_region_nucl_id));
		BufferedReader reader_nc = new BufferedReader(new FileReader(utils.Path.file_region_nc_id));
		BufferedReader reader_prot = new BufferedReader(new FileReader(utils.Path.file_region_prot_id));
		
		File sample_dir = new File(utils.Path.temp_directory+"/0");
		Path.buildDir_withpermission(sample_dir);
		File fasta_sample_nucl     = new File(sample_dir+"/"+utils.Path.file_name_region_nucl_split);
		PrintWriter writer_sample_nucl = new PrintWriter(fasta_sample_nucl);
		File fasta_sample_nc     = new File(sample_dir+"/"+utils.Path.file_name_region_nc_split);
		PrintWriter writer_sample_nc = new PrintWriter(fasta_sample_nc);
		File fasta_sample_prot     = new File(sample_dir+"/"+utils.Path.file_name_region_prot_split);
		PrintWriter writer_sample_prot = new PrintWriter(fasta_sample_prot);
		
		int seq_increment=0;
		String id_seq_nucl = reader_nucl.readLine();
		String seq_nucl = reader_nucl.readLine();
		String id_seq_nc = reader_nc.readLine();
		String seq_nc = reader_nc.readLine();
		String id_seq_prot = reader_prot.readLine();
		String seq_prot = reader_prot.readLine();
		if(id_seq_nucl.equals("")){
			System.out.println("No sequence to annotate.");
		}else{
			while(id_seq_nucl!=null){
				writer_sample_nucl.write(id_seq_nucl+"\n");
				writer_sample_nucl.write(seq_nucl+"\n");
				id_seq_nucl = reader_nucl.readLine();
				seq_nucl = reader_nucl.readLine();
				if(id_seq_nc!=null){
					writer_sample_nc.write(id_seq_nc+"\n");
					writer_sample_nc.write(seq_nc+"\n");
					id_seq_nc = reader_nc.readLine();
					seq_nc = reader_nc.readLine();
				}
				if(id_seq_prot!=null){
					writer_sample_prot.write(id_seq_prot+"\n");
					writer_sample_prot.write(seq_prot+"\n");
					id_seq_prot = reader_prot.readLine();
					seq_prot = reader_prot.readLine();
				}
				seq_increment++;
				if(id_seq_nucl == null || seq_increment % nb_region_by_run == 0){
					writer_sample_nucl.close();
					writer_sample_nc.close();
					writer_sample_prot.close();
					if (seq_increment > 0) {
						create_workers(services, sample_dir, fasta_sample_nucl, fasta_sample_nc, fasta_sample_prot);
					}
					if(id_seq_nucl != null){
						sample_dir = new File(utils.Path.temp_directory+"/"+seq_increment);
						Path.buildDir_withpermission(sample_dir);
						fasta_sample_nucl = new File(sample_dir+"/"+utils.Path.file_name_region_nucl_split);
						writer_sample_nucl = new PrintWriter(fasta_sample_nucl);
						fasta_sample_nc = new File(sample_dir+"/"+utils.Path.file_name_region_nc_split);
						writer_sample_nc = new PrintWriter(fasta_sample_nc);
						fasta_sample_prot = new File(sample_dir+"/"+utils.Path.file_name_region_prot_split);
						writer_sample_prot = new PrintWriter(fasta_sample_prot);
					}
				}
			}
			reader_nucl.close();
			reader_nc.close();
			reader_prot.close();
			
			//LAUNCH WORKERS
			runThreadsArray(array_thread);
			
			//WRITE ANNOTATIONS
			FileWriter writer_annotation = new FileWriter(utils.Path.file_all_annotations);
			Enumeration<Integer> keys = annotation_map.keys();
			while(keys.hasMoreElements()){
				int key = keys.nextElement();
				ArrayList<Annotation> annotation_list = annotation_map.get(key);
				for (Annotation annotation: annotation_list){
					writer_annotation.write(annotation.toString()+"\n");
				}
			}
			writer_annotation.close();
		}
	}
	
	/**
	 * Create a worker for each service
	 * @param services service list
	 * @param sample_dir sample directory
	 * @param file_nucl nucleic sequences
	 * @param file_nc non-coding sequences
	 * @param file_prot proteic sequences
	 * @throws java.lang.Exception TASK
	 */
	public static void create_workers(Hashtable<String, List<String>> services,File sample_dir,File file_nucl,File file_nc,File file_prot) throws Exception {
		Enumeration<String> services_enum = services.keys();
		int worker_increment = 0;
		while(services_enum.hasMoreElements()){
			String service=services_enum.nextElement();
			List<String> options = services.get(service);
			if(service.contains("BLASTN")){
				service = "BLASTN";
			}
			if(service.contains("BLASTP")){
				service = "BLASTP";
			}
			Callable<Object> worker= new AnnotationWorker(worker_increment,services_messages,service,options,sample_dir,file_nucl,file_nc,file_prot);
			array_thread.add(worker);
			worker_increment++;
		}
		worker_increment=0;
	}
	
	/**
	 * Manage threads in an array of threads (class Runnable)
	 * @param array_thread array of threads form the class Runnable
	 */
	public static void runThreadsArray(List<Callable<Object>> array_thread) {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		ExecutorService executor = Executors.newFixedThreadPool(nb_concurent_thread);
		int current_worker = 1 ;
		WorkerResults worker_results;
		Boolean wait = false;
		try {
			for(Callable<Object> worker : array_thread){
				futures.add(executor.submit(worker));
			}
			executor.shutdown();
			while (!futures.isEmpty()){
				if(wait){TimeUnit.SECONDS.sleep(refresh_time);}
				wait = true;
				int future_finished = -1;
		        for (Future<?> future : futures) {
		            if (future.isDone()) {
		            	worker_results = (WorkerResults) future.get();
	            		for (Annotation annotation: worker_results.annotations) {
	            			int key = annotation.region_id;
	            			ArrayList<Annotation> annotation_list = new ArrayList<Annotation>();
	            			if(annotation_map.containsKey(key)){
	            				annotation_list = annotation_map.get(key);
	            			}
	            			annotation_list.add(annotation);
	            			annotation_map.put(key, annotation_list);
	            		}
		            	System.out.println("Done " + current_worker + " / " + array_thread.size());
		            	current_worker++;
		            	future_finished = futures.indexOf(future);
		            	wait = false;
		            	break;
		            }
		        }
		        if(future_finished != -1){futures.remove(future_finished);}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}

