package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objects.Transcript;
import objects.Region;

/**
 * Search the ORF in the input Sequences
 */
public class FindOrf {

	public static String[] start_codons = {"ATG"};
	public static String[] stop_codons = {"TAG","TGA","TAA"};
	public static int orf_min_size = 100;
	public static boolean accept_inner_orf=false;
	public static boolean accept_outside_orf=false;
	public static boolean reverse=true;
	public static boolean ncrna=true;
	public static int region_id_increment = 0;
	public static int transcript_id_increment = 0;
	public static Hashtable<Integer, objects.Region> region_map = new Hashtable<Integer, objects.Region>();
	public static Hashtable<Integer, objects.Transcript> transcript_map = new Hashtable<Integer, objects.Transcript>();
	private static Set<Character> false_nucleotide_set = new HashSet<Character>();
	private static int nb_refused_seq = 0;
	public static int nb_identified_orf = 0;

	/**
	 * Codon to Amino Acid Genetic Code
	 */
	private static final Map<String, String> genetic_code;
	static
	{
		genetic_code = new HashMap<String, String>();
		genetic_code.put("GCA", "A");genetic_code.put("GCC", "A");genetic_code.put("GCG", "A");genetic_code.put("GCT", "A");
		genetic_code.put("CGA", "R");genetic_code.put("CGC", "R");genetic_code.put("CGG", "R");genetic_code.put("CGT", "R");
		genetic_code.put("AGG", "R");genetic_code.put("AGA", "R");genetic_code.put("GAC", "D");genetic_code.put("GAT", "D");
		genetic_code.put("AAC", "N");genetic_code.put("AAT", "N");genetic_code.put("TGC", "C");genetic_code.put("TGT", "C");
		genetic_code.put("GAA", "E");genetic_code.put("GAG", "E");genetic_code.put("CAA", "Q");genetic_code.put("CAG", "Q");
		genetic_code.put("GGA", "G");genetic_code.put("GGC", "G");genetic_code.put("GGG", "G");genetic_code.put("GGT", "G");
		genetic_code.put("CAC", "H");genetic_code.put("CAT", "H");genetic_code.put("ATA", "I");genetic_code.put("ATC", "I");
		genetic_code.put("ATT", "I");genetic_code.put("CTA", "L");genetic_code.put("CTC", "L");genetic_code.put("CTG", "L");
		genetic_code.put("CTT", "L");genetic_code.put("TTA", "L");genetic_code.put("TTG", "L");genetic_code.put("AAA", "K");
		genetic_code.put("AAG", "K");genetic_code.put("ATG", "M");genetic_code.put("TTC", "F");genetic_code.put("TTT", "F");
		genetic_code.put("CCA", "P");genetic_code.put("CCC", "P");genetic_code.put("CCG", "P");genetic_code.put("CCT", "P");
		genetic_code.put("TCA", "S");genetic_code.put("TCC", "S");genetic_code.put("TCG", "S");genetic_code.put("TCT", "S");
		genetic_code.put("AGC", "S");genetic_code.put("AGT", "S");genetic_code.put("ACT", "T");genetic_code.put("ACC", "T");
		genetic_code.put("ACG", "T");genetic_code.put("ACA", "T");genetic_code.put("TGG", "W");genetic_code.put("TAC", "Y");
		genetic_code.put("TAT", "Y");genetic_code.put("GTA", "V");genetic_code.put("GTC", "V");genetic_code.put("GTG", "V");
		genetic_code.put("GTT", "V");genetic_code.put("TAG", "");genetic_code.put("TAA", "");genetic_code.put("TGA", "");
	}

	/**
	 * Read a fasta file and verify and format it to create a compatible fasta file .
	 * This method return a format Fasta file given in parameter.
	 * only sequences which are a combination of 'A' 'T' 'C' 'G' are accepted.
	 * @throws Exception IO error
	 */
	public static void search_orf() throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(utils.Path.inputtranscriptfastafile));
		utils.Path.initializeWriters();
		utils.Path.writer_region_info.write("orf_id\t"+"begin\t"+"end\t"+"size\t"+"strand\t"+"coding\t"+"type\t"+"transcript_id\n");
		utils.Path.writer_transcript_info.write("transcript_id\t"+"name\t"+"description\t"+"size\n");
		String seq = "";
		String seqid = "";
		String line = reader.readLine();
		if(line == null){
			System.out.println("Error: Your input nucleic sequences file is empty !");
			System.exit(0);
		}
		while(line != null){
			line = line.replace("\n", "").replace("\r", "");
			if(line.startsWith(">")){
				if(seq != "" && checkseq(seq)){
					String transcript_name=seqid.split(" ")[0].substring(1);
					String transcript_desc=seqid.substring(transcript_name.length()+1);
					int transcript_size= seq.length();
					Transcript transcript_current = new Transcript(-1, transcript_name, transcript_desc, transcript_size, seq);
					find_all_ORF(transcript_current, seq);
				}
				seqid = line;
				seqid=seqid.replace(":", "");
				seqid=seqid.replace("|", "");
				seqid=seqid.replace("/", "");
				seqid=seqid.replace("\"", "");
				seqid=seqid.replace("'", "");
				seq="";
			}else{
				seq+=line.toUpperCase();
			}
			line=reader.readLine();
		}
		if(seq != "" && checkseq(seq) ){
			String transcript_name=seqid.split(" ")[0].substring(1);
			String transcript_desc=seqid.substring(transcript_name.length()+1);
			int transcript_size= seq.length();
			Transcript transcript_current = new Transcript(-1, transcript_name, transcript_desc, transcript_size, seq);
			find_all_ORF(transcript_current, seq);
		}
		reader.close();
		utils.Path.closeWriters();
		if(false_nucleotide_set.size()>0){
			System.out.println(nb_refused_seq+" sequences refused. Nucleotide not allowed: "+false_nucleotide_set.toString());
		}
	}
	
	/**
	 * return the reverse_complement strand for a sequence
	 * @param seq nucleic sequence
	 * @return seq nucleic reverse_complement sequence
	 */
	public static String reverse_complement(String seq){
		seq = new StringBuffer(seq).reverse().toString();
	    StringBuilder builder = new StringBuilder();
	    for(int i=0;i<seq.length();i++){
	        char base = seq.charAt(i);
	        if(base == 'T'){
	            builder.append('A');
	        }
	        if(base == 'A'){
	            builder.append('T');
	        }
	        if(base == 'C'){
	            builder.append('G');
	        }
	        if(base == 'G'){
	            builder.append('C');
	        }
	        if(base == 'N'){
	            builder.append('N');
	        }
	    }
	    return builder.toString();
	}
	
	/**
	 * This method translate DNA sequences  in protein sequences with the translation table code_transcripts
	 * @param seqnucleic nucleic sequence
	 * @return proteic sequence
	 */
	public static String translate(String seqnucleic){
		String seq_amino_acid="";
		for(int i=0; i < seqnucleic.length()-2; i += 3){
			String codon = seqnucleic.substring(i, i+3);
			if(codon.contains("N")){
				seq_amino_acid += "X";
			}else{
				seq_amino_acid += genetic_code.get(codon);
			}
		}
		return seq_amino_acid;
	}
	
	/**
	 * Check if a sequence contains only [ATGC]
	 * @param seq nucleic sequence
	 * @return true if the sequence is valid
	 */
	private static Boolean checkseq(String seq){
		Boolean isacceptedsequence = true;
		for (int x=0; x<seq.length(); x++){
			char c = seq.charAt(x);
			if(c!='A' && c!='T'&& c!='G' && c!='C' && c!='N' ){
				false_nucleotide_set.add(c);
				isacceptedsequence=false;
			}
		}
		if(!isacceptedsequence){
			nb_refused_seq++;
		}
		return isacceptedsequence;
	}
	
	/**
	 * Search the ORF and write them in the nucl and prot files.
	 * @param transcript_current transcript to parse
	 * @param seq_strand_1 Nucleic sequence of the transcript to parse
	 * @throws Exception IO error
	 */
	public static void find_all_ORF(Transcript transcript_current, String seq_strand_1) throws Exception{
		String seq_strand_2 = seq_strand_1.substring(1);
		String seq_strand_3 = seq_strand_1.substring(2);
		findORF(seq_strand_1,'+', 0, transcript_current);
		findORF(seq_strand_2,'+', 1, transcript_current);
		findORF(seq_strand_3,'+', 2, transcript_current);
		if (reverse){
			String seq_strand_reverse_1 = reverse_complement(seq_strand_1);
			String seq_strand_reverse_2 = seq_strand_reverse_1.substring(1);
			String seq_strand_reverse_3 = seq_strand_reverse_1.substring(2);
			findORF(seq_strand_reverse_1,'-', 0, transcript_current);
			findORF(seq_strand_reverse_2,'-', 1, transcript_current);
			findORF(seq_strand_reverse_3,'-', 2, transcript_current);
		}
		//WRITE NON CODING RNA
		if(ncrna && transcript_current.nb_orf == 0){
			Region ncrna = new Region(-1, 0, seq_strand_1.length()-1, seq_strand_1.length(), '+', "noncoding", "",seq_strand_1, -1);
			write_ncrna(transcript_current,  ncrna);
			if (reverse){
				String seq_strand_reverse_1 = new StringBuffer(seq_strand_1).reverse().toString();
				ncrna = new Region(-1, 0, seq_strand_reverse_1.length()-1, seq_strand_reverse_1.length(), '-', "noncoding", "", seq_strand_reverse_1, -1);
				write_ncrna(transcript_current, ncrna);
			}
		}
	}

	/**
	 * Search all possibles ORF
	 * @param seq nucleic sequence of the transcript
	 * @param sens strand either forward or reverse
	 * @param transcript_current Computed transcript
	 * @param frame Computed frame
	 * @throws Exception IO error
	 */
	private static void findORF(String seq, char sens, int frame, Transcript transcript_current) throws Exception {
		List<Region> orf_list = new ArrayList<Region>();
		List<Integer> start_list = new ArrayList<Integer>();
		int first_stop = -1;
		String type = "";
		for (int i=0; i<seq.length()-2; i+=3){
			String codon = seq.substring(i,i+3);
			if(codon.length()!=3){
				System.out.println("error codon length"+codon);
				System.exit(0);
				}
			if(Arrays.asList(start_codons).contains(codon)){
				start_list.add(i);
			}
			if(Arrays.asList(stop_codons).contains(codon) && start_list.size() > 0){
				if (first_stop == -1){first_stop = i+3;}
				for (int start_position : start_list) {
					int stop_position = i+3;
					String orf_seq = seq.substring(start_position, stop_position);
					if(type == "inner" && accept_inner_orf && orf_seq.length() > orf_min_size){
						Region orf = new Region(-1, start_position+frame, stop_position+frame-1, orf_seq.length(), sens, "coding", type, orf_seq, -1);
						orf_list.add(orf);
					}
					if(type == "" && orf_seq.length() > orf_min_size){
						Region orf = new Region(-1, start_position+frame, stop_position+frame-1, orf_seq.length(), sens, "coding", type, orf_seq, -1);
						orf_list.add(orf);
						type = "inner";
					}
				}
				start_list = new ArrayList<Integer>();
				type = "";
			}
		}
		
		//CREATE outside ORF
		if(accept_outside_orf){
			type = "outside";
			if(first_stop != -1){
				int start_position = 0;
				int stop_position = first_stop;
				String orf_seq = seq.substring(start_position, stop_position);
				if(orf_seq.length() > orf_min_size){
					Region orf = new Region(-1, start_position+frame, stop_position+frame-1, orf_seq.length(), sens, "coding", type, orf_seq, -1);
					orf_list.add(orf);
				}
			}
			if(start_list.size() > 0){
				for (int start_position : start_list) {
					int stop_position = seq.length();
					String orf_seq = seq.substring(start_position, stop_position);
					if(orf_seq.length() > orf_min_size){
						Region orf = new Region(-1, start_position+frame, stop_position+frame-1, orf_seq.length(), sens, "coding", type, orf_seq, -1);
						orf_list.add(orf);
					}
				}
			}
		}
		//WRITE ORF
		for (Region orf : orf_list) {
			write_orf(transcript_current, orf);
		}
	}
	
	/**
	 * Add ncrna in memory map and in output files
	 * @param transcript_current Computed transcript
	 * @param ncrna ncrna to add in memory map and in output files
	 * @throws Exception IO error
	 */
	private static void write_ncrna(Transcript transcript_current, Region ncrna) throws Exception{
		if (ncrna.strand == '-'){
			ncrna.begin = transcript_current.size - ncrna.end - 1;
			ncrna.end = transcript_current.size - ncrna.begin - 1;
		}
		if (transcript_current.id == -1){
			transcript_current.id = transcript_id_increment;
			transcript_map.put(transcript_current.id, transcript_current);
			utils.Path.writer_transcript_info.write(transcript_current.id+"\t"+transcript_current.name+"\t"+transcript_current.desc+"\t"+transcript_current.size+"\n");
			transcript_id_increment++;
		}
		ncrna.transcript_id = transcript_current.id;
		ncrna.id = region_id_increment;
		region_map.put(ncrna.id, ncrna);
		utils.Path.writer_region_info.write(ncrna.id+"\t"+ncrna.begin+"\t"+ncrna.end+"\t"+ncrna.size+"\t"
		+ncrna.strand+"\t"+ncrna.coding+"\t"+ncrna.type+"\t"+ncrna.transcript_id+"\n");
		//utils.Path.writer_nucl_id.write(">"+ncrna.id+"\n");
		//utils.Path.writer_nucl_id.write(ncrna.seq+"\n");
		//utils.Path.writer_nucl.write(">"+transcript_current.name+"_"+ncrna.id+"\n");
		//utils.Path.writer_nucl.write(ncrna.seq+"\n");
		utils.Path.writer_nc_id.write(">"+ncrna.id+"\n");
		utils.Path.writer_nc_id.write(ncrna.seq+"\n");
		region_id_increment++;
	}
	
	/**
	 * Add ORF in memory map and in output files
	 * @param transcript_current Computed transcript
	 * @param orf ORF to add in memory map and in output files
	 * @throws Exception IO error
	 */
	private static void write_orf(Transcript transcript_current, Region orf) throws Exception{
		transcript_current.nb_orf++;
		if (orf.strand == '-'){
			int tmpbegin = transcript_current.size - orf.end - 1;
			orf.end = transcript_current.size - orf.begin - 1;
			orf.begin = tmpbegin;
		}
		if (transcript_current.id == -1){
			transcript_current.id = transcript_id_increment;
			transcript_map.put(transcript_current.id, transcript_current);
			utils.Path.writer_transcript_info.write(transcript_current.id+"\t"+transcript_current.name+"\t"+transcript_current.desc+"\t"+transcript_current.size+"\n");
			transcript_id_increment++;
		}
		orf.transcript_id = transcript_current.id;
		orf.id = region_id_increment;
		region_map.put(orf.id, orf);
		utils.Path.writer_region_info.write(orf.id+"\t"+orf.begin+"\t"+orf.end+"\t"+orf.size+"\t"
		+orf.strand+"\t"+orf.coding+"\t"+orf.type+"\t"+orf.transcript_id+"\n");
		utils.Path.writer_nucl_id.write(">"+orf.id+"\n");
		utils.Path.writer_nucl_id.write(orf.seq+"\n");
		utils.Path.writer_prot_id.write(">"+orf.id+"\n");
		utils.Path.writer_prot_id.write(translate(orf.seq)+"\n");
		utils.Path.writer_nucl.write(">"+transcript_current.name+"_"+orf.id+"\n");
		utils.Path.writer_nucl.write(orf.seq+"\n");
		utils.Path.writer_prot.write(">"+transcript_current.name+"_"+orf.id+"\n");
		utils.Path.writer_prot.write(translate(orf.seq)+"\n");
		region_id_increment++;
		nb_identified_orf++;
	}
}
