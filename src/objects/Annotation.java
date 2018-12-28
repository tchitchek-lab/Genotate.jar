package objects;

import core.FindOrf;

/**
 * Contains the information for one annotation feature on a region.
 */
public class Annotation  {

	public String service;
	public String name;
	public String description;
	public int begin;
	public int end;
	public String color;
	public int region_id;

	/**
	 * Set the Annotation attributes.
	 * @param service name of the annotation service
	 * @param name name of the annotation
	 * @param description description of the annotation
	 * @param begin start of the position on the sequence
	 * @param end end of the position on the sequence
	 * @param color color of the annotation in the SVG file
	 * @param region_id ID of the region in the database
	 */
	public Annotation(String service, String name, String description, int begin,int end, String color, int region_id){
		Region region_cur = FindOrf.region_map.get(region_id);
		if (region_cur.strand == '-'){
			int tmpbegin = region_cur.size - end - 1;
			end = region_cur.size - begin - 1;
			begin = tmpbegin;
		}
		name = name.replace("'", "");
		name = name.replace("\"", "");
		name = name.trim();
		description = description.replace("'", "");
		description = description.replace("\"", "");
		description = description.replace("\t", ", ");
		description = description.trim();
		description = description.replaceAll(",$", "");
		this.service=service;
		this.name=name;
		this.description=description;
		this.begin=begin;
		this.end=end;
		this.color=color;
		this.region_id=region_id;
	}
	
	/**
	 * Initialize the Annotation attributes.
	 */
	public Annotation(){
		this.service="init";
		this.name="init";
		this.description="init";
		this.begin=0;
		this.end=0;
		this.color="init";
		this.region_id=0;
	}
	
	/**
	 * Check the equality with another Annotation.
	 * @param annot Annotation to compare with
	 * @return true if the annotations are identical
	 */
	public boolean equals(Annotation annot){
		boolean bool = true;
		if(!this.service.equals(annot.service)){bool = false;}
		if(!this.name.equals(annot.name)){bool = false;}
		if(!this.description.equals(annot.description)){bool = false;}
		if(this.begin != annot.begin){bool = false;}
		if(this.end != annot.end){bool = false;}
		if(!this.color.equals(annot.color)){bool = false;}
		if(this.region_id != annot.region_id){bool = false;}
		return bool;
	}
	
	/**
	 * Create a formated string to display
	 * @return Annotation as String
	 */
	public String toString(){
		String sep="\t";
		String txt = this.region_id+"";
		txt += sep + this.service;
		txt += sep + this.begin;
		txt += sep + this.end;
		txt += sep + this.name;
		txt += sep + this.description;
		return txt;
	}
}

