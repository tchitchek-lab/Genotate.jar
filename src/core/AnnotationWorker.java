package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

import objects.WorkerResults;

/**
 * Run the services on a region fasta file
 */
public  class AnnotationWorker implements Callable<Object> {

	private WorkerResults worker_results;
	private File working_dir;
	private String name;
	private File fasta_sample_nucl;
	private File fasta_sample_nc;
	private File fasta_sample_prot;
	private Boolean services_messages;

	/**
	 * Set the worker attributes.
	 * @param increment number of the actual service
	 * @param services_messages enable the output of services messages
	 * @param service the name of a annotation tool
	 * @param options the options for the annotation tool
	 * @param working_dir directory in which the service is executed
	 * @param file_nucl fasta nucleic file
	 * @param file_nc fasta nc file
	 * @param file_prot fasta prot file
	 * @throws Exception TASK
	 */
	public AnnotationWorker(int increment, Boolean services_messages, String service, List<String> options, File working_dir, File file_nucl, File file_nc, File file_prot) throws Exception {
		//System.out.println("Initialize "+fasta_sample_prot+" for "+service);
		worker_results = new WorkerResults(service, options);
		this.services_messages = services_messages;
		this.name = increment+"_"+service;
		this.working_dir = working_dir;
		this.fasta_sample_nucl = new File(working_dir + "/" + this.name + "_" + utils.Path.file_name_region_nucl_split);
		this.fasta_sample_nc   = new File(working_dir + "/" + this.name + "_" + utils.Path.file_name_region_nc_split);
		this.fasta_sample_prot = new File(working_dir + "/" + this.name + "_" + utils.Path.file_name_region_prot_split);
		utils.FileUtils.copy(file_nucl, this.fasta_sample_nucl);
		utils.FileUtils.copy(file_nc, this.fasta_sample_nc);
		utils.FileUtils.copy(file_prot, this.fasta_sample_prot);
	}

	public class ReadStream implements Runnable {
		String name;
		InputStream is;
		Thread thread;      
		public ReadStream(String name, InputStream is) {
			this.name = name;
			this.is = is;
		}       
		public void start () {
			thread = new Thread (this);
			thread.start ();
		}       
		public void run () {
			try {
				InputStreamReader isr = new InputStreamReader (is);
				BufferedReader br = new BufferedReader (isr);   
				while (true) {
					String s = br.readLine ();
					if (s == null) break;
					System.out.println ("[" + name + "] " + s);
				}
				is.close ();    
			} catch (Exception ex) {
				System.out.println ("Problem reading stream " + name + "... :" + ex);
				ex.printStackTrace ();
			}
		}
	}

	/**
	 * Launch the service, get the result file, parse the result file, display the annotations, write the annotations.
	 */
	@Override
	public WorkerResults call() throws Exception {
		Process p = null;
		try {
			services.SERVICE prog = (services.SERVICE) Class.forName("services."+worker_results.service).newInstance();
			File computing_file = new File(working_dir+"/"+name+"_computing.txt");
			File result_file = new File(working_dir+"/"+name+"_complete.txt");
			String command=prog.run(computing_file,result_file,fasta_sample_nucl,fasta_sample_nc,fasta_sample_prot,worker_results.options,utils.Path.path_services);
			//String line;
			if(services_messages){
				System.out.println("Message from "+worker_results.service+" bash -c "+command);
			}
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command },null,working_dir);
			ReadStream s1 = new ReadStream("stdin", p.getInputStream ());
			ReadStream s2 = new ReadStream("stderr", p.getErrorStream ());
			s1.start ();
			s2.start ();
			p.waitFor();
			worker_results.annotations = prog.parse(result_file);
		} catch (Exception e) {  
			e.printStackTrace();  
		} finally {
			if(p != null)
				p.destroy();
		}
		return (worker_results);
	}
}
