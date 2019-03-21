package objects;

import java.util.List;

/**
 * Contig contains the information for one Contig.
 */
public class WorkerResults {
	public String service;
	public List<String> options;
	public List<objects.Annotation> annotations;
	
	/**
	 * Set WorkerResults attributes.
	 * @param service service
	 * @param options options
	 */
	public WorkerResults(String service, List<String> options){
		this.service = service;
		this.options = options;
	}
}
