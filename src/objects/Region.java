package objects;

/**
 * region contains the information for one region.
 */
public class Region {
	public int id;
	public int begin;
	public int end;
	public int size;
	public char strand;
	public String coding;
	public String type;
	public String seq;
	public int transcript_id;
	
	/**
	 * Initialize region attributes.
	 */
	public Region(){
		this.id=0;
		this.begin=0;
		this.end=0;
		this.size=0;
		this.strand='+';
		this.coding="";
		this.type="";
		this.seq="";
		this.transcript_id=0;
	}
	
	/**
	 * Set ORF attributes.
	 * @param id ID of the region in the database
	 * @param begin Start position on the transcript
	 * @param end Stop position on the transcript
	 * @param size Length of the ORF
	 * @param strand Either forward or reverse
	 * @param coding coding or noncoding
	 * @param type inner or outter or none
	 * @param seq region sequence
	 * @param transcript_id ID of the transcript in the database
	 */
	public Region(int id, int begin, int end, int size, char strand, String coding, String type, String seq, int transcript_id){
		this.id = id;
		this.begin = begin;
		this.end = end;
		this.size = size;
		this.strand = strand;
		this.coding = coding;
		this.type = type;
		this.seq = seq;
		this.transcript_id = transcript_id;
	}
}
