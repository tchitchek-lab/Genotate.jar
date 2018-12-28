package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import objects.Annotation;
import objects.Transcript;
import objects.Region;

/**
 * Create the SVG files for each region
 */
public class SVG {
	/**
	 * Create the SVG associated to a region 
	 * @throws Exception TASK
	 */
	public static void createSvgFiles() throws Exception {
		Enumeration<Integer> keys = FindOrf.region_map.keys();
		while(keys.hasMoreElements()){
			int key = keys.nextElement();
			Region current_region = FindOrf.region_map.get(key);
			ArrayList<Annotation> annotation_list = new ArrayList<Annotation>();
			if(AnnotRegion.annotation_map.containsKey(current_region.id)){
				annotation_list = AnnotRegion.annotation_map.get(current_region.id);
			}
			createsvg(current_region.id, annotation_list);
		}
	}

	/**
	 * Create the SVG using a list of annotations.
	 * @param region_id id of the region
	 * @param annots annotations on the region
	 * @throws Exception TASK
	 */
	private static void createsvg(int region_id, ArrayList<Annotation> services_annot) throws Exception {
		Region region_cur = FindOrf.region_map.get(region_id);
		Transcript transcript_cur = FindOrf.transcript_map.get(region_cur.transcript_id);

		//START
		int region_size        = region_cur.size;
		int region_begin       = region_cur.begin+1;
		int region_end         = region_cur.end+1;
		int transcript_size    = transcript_cur.size;
		int display_begin = 1;
		int display_end = transcript_size;
		int svg_default_width = 1000;
		int transcript_displayed_size = display_end - display_begin +1;
		float zoom_ratio = (float) svg_default_width / transcript_displayed_size;
		int offset_x = 0;
		int pos_y = 10;
		int height_rect=8;
		int offset_inter_annotation=2;
		String svgtext = "\n<svg id='svg_"+region_id+"' width='"+svg_default_width+"' height='100%' version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' >";
		svgtext += "\n<text  x='"+offset_x+"' y='"+pos_y+"' font-family='Verdana' font-size='10' style='fill:rgb(0,0,0)' >";
		svgtext += region_cur.coding+" region ["+region_begin+","+region_end+"] ("+region_cur.strand+") on transcript "+transcript_cur.name+" "+transcript_cur.desc+"</text>";
		pos_y = pos_y + 12;
		
		//SCALE
		int step = 1000;
		if(transcript_displayed_size < 2000){
			step = 100;
		}
		if(transcript_displayed_size < 200){
			step = 10;
		}
		for(int scale=0; scale<display_end; scale+=step){
			svgtext+="\n<text  x='"+(offset_x+(zoom_ratio*(scale))-2)+"' ";
			svgtext+="y='"+pos_y+"' ";
			svgtext+="font-family='Verdana' font-size='10'  ";
			svgtext+="style='fill:rgb(0,0,0)' >";
			NumberFormat nf = NumberFormat.getInstance();
			svgtext+="|"+nf.format(display_begin*1+scale)+"";
			svgtext+="</text>";
		}
		pos_y = pos_y + 2;

		//transcript RECTANGLE
		svgtext += "\n<rect x='"+offset_x+"' y='"+pos_y+"'  width='"+zoom_ratio*transcript_displayed_size+"' height='"+height_rect+"' style='stroke: black;' fill='DeepSkyBlue' fill-opacity='0.8'>";
		svgtext += "<title>transcript 1-"+transcript_size+"</title>";
		svgtext += "</rect>";
		int region_displayed_begin = 0;
		int region_displayed_size = 0;
		if((region_begin <= display_begin && region_end >= display_begin) || (region_begin >= display_begin && region_end <= display_end) || (region_begin <= display_end && region_end >= display_end)){
			region_displayed_size = Math.min(region_end,display_end) - Math.max(region_begin,display_begin) + 1 ;
			if(region_begin > display_begin){
				region_displayed_begin = region_begin-display_begin+1;
			}else{
				region_displayed_begin = 0;
			}
			svgtext += "\n<rect x='"+(region_displayed_begin*zoom_ratio+offset_x)+"' y='"+pos_y+"' width='"+zoom_ratio*region_displayed_size+"' height='"+height_rect+"' style='stroke: black;' fill='orange' fill-opacity='0.8'>";
			svgtext += "<title>region "+region_begin+"-"+region_end+"</title>";
			svgtext += "</rect>";
		}

		//ANNOTATIONS
		if(true){
			int begin_tmp=0;
			int end_tmp=region_size;
			String service_tmp="";
			pos_y = pos_y + 6;
			height_rect=8;
			for (Annotation annotation: services_annot) {
				String service = annotation.service;
				String name = annotation.name;
				String description = annotation.description;
				int begin = annotation.begin + region_begin;
				int end = annotation.end + region_begin;
				if((begin <= display_begin && end >= display_begin) || (begin >= display_begin && end <= display_end) || (begin <= display_end && end >= display_end)){
					if((service != service_tmp) || (begin <= begin_tmp && end >= begin_tmp) || (begin >= begin_tmp && end <= end_tmp) || (begin <= end_tmp && end >= end_tmp)){
						pos_y = pos_y + height_rect + offset_inter_annotation;
					}
					String color = annotation.color;
					service_tmp=service;
					begin_tmp=begin;
					end_tmp=end;
					int annot_displayed_size = (int) (zoom_ratio * (Math.min(end,display_end) - Math.max(begin,display_begin) + 1)) ;
					int annot_displayed_begin = 0;
					if(begin > display_begin){
						annot_displayed_begin = begin-display_begin;
					}
					annot_displayed_begin = (int) (annot_displayed_begin * zoom_ratio);
					if(annot_displayed_size < 4){
						annot_displayed_size = 4;
					}
					//HYPERLINK
					svgtext+="\n<a target='_blank' href='https://www.google.fr/search?q="+name+" "+service+"'>";
					//RECTANGLE OF THE ANNOTATION
					svgtext+="\n<rect x='"+annot_displayed_begin+"' y='"+pos_y+"' width='"+annot_displayed_size+"'";
					svgtext+=" height='"+height_rect+"' fill='"+color+"' fill-opacity='0.8'>";
					svgtext+="<title>["+begin+","+end+"] "+name+" "+service+" "+description+"</title> ";
					svgtext+="</rect>";
					//TEXT OF THE ANNOTATION
					String nametmp = name+" "+service;
					String namecut = nametmp.substring(0,Math.min(annot_displayed_size/5, nametmp.length()));
					int left_margin = 2;
					if((annot_displayed_size/2 - namecut.length()*3) > namecut.length()){
						left_margin = annot_displayed_size/2 - name.length()*3;
					}
					if(annot_displayed_size < 15){
						namecut = "";
					}
					svgtext+="\n<text   x='"+(annot_displayed_begin+left_margin)+"' y='"+(pos_y+height_rect-1)+"' ";
					svgtext+="font-family='Verdana' font-size='"+height_rect+"'  style='fill:rgb(0,0,0);'>";
					svgtext+=namecut;
					svgtext+="<title>["+begin+","+end+"] "+name+" "+service+" "+description+"</title> ";
					svgtext+="</text>";
					svgtext+="</a>";
				}
			}
		}
		svgtext += "\n</svg>";

		File outputsvg= new File(utils.Path.svg_directory+"/"+transcript_cur.name+"_"+region_cur.id+".svg");
		BufferedWriter writer = new BufferedWriter(new PrintWriter(outputsvg, "UTF-8"));
		writer.write(svgtext);
		writer.close();
	}

}
