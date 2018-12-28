package objects;

/**
 * Contig contains the information for one contig.
 */
public class Transcript {
	public int id;
	public String name;
	public String desc;
	public int size;
	
	/**
	 * Initialize Contig attributes.
	 */
	public Transcript(){
		this.id=0;
		this.name="init";
		this.desc="init";
		this.size=0;
	}
	
	/**
	 * Set Contig attributes.
	 * @param id ID of the contig in the database
	 * @param name Name of the contig
	 * @param desc Description of the contig
	 * @param size Length of the contig
	 */
	public Transcript(int id, String name, String desc, int size){
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.size = size;
	}
}
