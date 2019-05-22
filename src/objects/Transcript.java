package objects;

/**
 * Contig contains the information for one Contig.
 */
public class Transcript {
	public int id;
	public String name;
	public String desc;
	public int size;
	public int cpat;
	public int nb_orf;
	public String seq;
	
	/**
	 * Initialize Contig attributes.
	 */
	public Transcript(){
		this.id=0;
		this.name="init";
		this.desc="init";
		this.size=0;
		this.cpat=1;
		this.nb_orf=0;
		this.seq="init";
	}
	
	/**
	 * Set Contig attributes.
	 * @param id ID of the contig in the database
	 * @param name Name of the contig
	 * @param desc Description of the contig
	 * @param size Length of the contig
	 */
	public Transcript(int id, String name, String desc, int size, int cpat, String seq){
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.size = size;
		this.cpat = cpat;
		this.seq = seq;
	}
}
