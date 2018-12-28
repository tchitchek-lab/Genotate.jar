package objects;

import java.util.List;

/**
 * Contains the information of one contig.
 */
public class WorkerResults {
	public String service;
	public List<String> options;
	public List<objects.Annotation> annotations;
	
	/**
	 * Set WorkerResults attributes.
	 * @param service name of the service
	 * @param options options of the service
	 */
	public WorkerResults(String service, List<String> options){
		this.service = service;
		this.options = options;
	}
}
