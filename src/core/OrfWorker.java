package core;

import java.io.*;
import java.util.*;
import objects.Transcript;
import objects.Region;

/**
 * Run the services on a region fasta file
 */
public class OrfWorker {
    private File working_dir = utils.Path.temp_directory;
    private String name = "ORFWorker";
    public static double cpat_threshold = 0.5;
    public static boolean use_cpat = false;

    public class ReadStream implements Runnable {
        String name;
        InputStream is;
        Thread thread;

        public ReadStream(String name, InputStream is) {
            this.name = name;
            this.is = is;
        }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                while (true) {
                    String s = br.readLine();
                    if (s == null) break;
                    System.out.println("[" + name + "] " + s);
                }
                is.close();
            } catch (Exception ex) {
                System.out.println("Problem reading stream " + name + "... :" + ex);
                ex.printStackTrace();
            }
        }
    }

    public void parse(File result_file) throws Exception {
        utils.Path.initializeWriters();
        utils.Path.writer_region_info.write("orf_id\t"+"begin\t"+"end\t"+"size\t"+"strand\t"+"coding\t"+"type\t"+"transcript_id\n");
        utils.Path.writer_transcript_info.write("transcript_id\t"+"name\t"+"description\t"+"size\n");
        int idx_region_id =0;
        int idx_score = 5;
        BufferedReader reader = new BufferedReader(new FileReader(result_file));
        String line;
        while((line=reader.readLine())!=null) {
            //System.out.println(line);
            if(line.startsWith("mRNA_size") ){
                continue;
            }
            String[] splittab = line.split("\t");
            if(splittab.length < 5){
                continue;
            }
            int region_id = Integer.parseInt(splittab[idx_region_id]);
            float identity = Float.parseFloat(splittab[idx_score]);
            Region region_cur = FindOrf.region_map.get(region_id);
            Transcript transcript_cur = FindOrf.transcript_map.get(region_cur.transcript_id);
            if(region_cur.coding == "noncoding") {
                continue;
            }
            else if(identity < cpat_threshold){
                FindOrf.region_map.remove(region_cur.id);
                transcript_cur.nb_orf--;
                if(transcript_cur.nb_orf == 0 && FindOrf.ncrna == false){
                    FindOrf.transcript_map.remove(transcript_cur.id);
                }
                if(transcript_cur.nb_orf == 0 && FindOrf.ncrna == true){
                    Region new_nc_region = new Region(FindOrf.region_id_increment, 0, transcript_cur.seq.length()-1, transcript_cur.seq.length(), '+', "noncoding", "",transcript_cur.seq, transcript_cur.id);
                    FindOrf.region_map.put(new_nc_region.id, new_nc_region);
                    FindOrf.region_id_increment++;
                    if (FindOrf.reverse){
                        String seq_reverse = new StringBuffer(transcript_cur.seq).reverse().toString();
                        Region new_nc_region_rev = new Region(FindOrf.region_id_increment, 0, seq_reverse.length()-1, seq_reverse.length(), '+', "noncoding", "",seq_reverse, transcript_cur.id);
                        FindOrf.region_map.put(new_nc_region.id, new_nc_region_rev);
                        FindOrf.region_id_increment++;
                    }
                }
            }
        }
        reader.close();

        //Refresh the Transcript ids
        Hashtable<Integer, objects.Transcript> transcript_map_new = new Hashtable<Integer, objects.Transcript>();
        Hashtable<Integer, Integer> transcript_id_map = new Hashtable<Integer, Integer>();
        int new_transcript_id = 0;
        List<Integer> transcript_map_keys = new ArrayList<Integer>(FindOrf.transcript_map.keySet());
        Collections.sort(transcript_map_keys);
        for (int key : transcript_map_keys) {
            Transcript current_transcript = FindOrf.transcript_map.get(key);
            transcript_id_map.put(current_transcript.id, new_transcript_id);
            current_transcript.id = new_transcript_id;
            transcript_map_new.put(new_transcript_id, current_transcript);
            new_transcript_id++;
        }
        FindOrf.transcript_map =  transcript_map_new;

        //Refresh the ORF ids
        int new_region_id = 0;
        Hashtable<Integer, objects.Region> region_map_new = new Hashtable<Integer, objects.Region>();
        List<Integer> region_map_keys = new ArrayList<Integer>(FindOrf.region_map.keySet());
        Collections.sort(region_map_keys);
        for (int key : region_map_keys) {
            Region current_region = FindOrf.region_map.get(key);
            new_transcript_id = transcript_id_map.get(current_region.transcript_id);
            current_region.transcript_id = new_transcript_id;
            current_region.id = new_region_id;
            region_map_new.put(new_region_id, current_region);
            new_region_id++;
        }
        FindOrf.region_map = region_map_new;

        //Write filtered ORF
        region_map_keys = new ArrayList<Integer>(FindOrf.region_map.keySet());
        Collections.sort(region_map_keys);
        for (int key : region_map_keys) {
            Region current_region = FindOrf.region_map.get(key);
            Transcript transcript_current = FindOrf.transcript_map.get(current_region.transcript_id);
            utils.Path.writer_region_info.write(current_region.id+"\t"+current_region.begin+"\t"+current_region.end+"\t"+current_region.size+"\t"+current_region.strand+"\t"+current_region.coding+"\t"+current_region.type+"\t"+current_region.transcript_id+"\n");
            if(current_region.coding == "coding") {
                utils.Path.writer_nucl_id.write(">" + current_region.id + "\n");
                utils.Path.writer_nucl_id.write(current_region.seq + "\n");
                utils.Path.writer_prot_id.write(">" + current_region.id + "\n");
                utils.Path.writer_prot_id.write(core.FindOrf.translate(current_region.seq) + "\n");
                utils.Path.writer_nucl.write(">" + transcript_current.name + "_" + current_region.id + "\n");
                utils.Path.writer_nucl.write(current_region.seq + "\n");
                utils.Path.writer_prot.write(">" + transcript_current.name + "_" + current_region.id + "\n");
                utils.Path.writer_prot.write(core.FindOrf.translate(current_region.seq) + "\n");
            }else if(FindOrf.ncrna) {
                utils.Path.writer_nc_id.write(">" + current_region.id + "\n"+ current_region.seq + "\n");
            }
        }
        transcript_map_keys = new ArrayList<Integer>(FindOrf.transcript_map.keySet());
        Collections.sort(transcript_map_keys);
        for (int key : transcript_map_keys) {
            Transcript transcript_current = FindOrf.transcript_map.get(key);
            utils.Path.writer_transcript_info.write(transcript_current.id+"\t"+transcript_current.name+"\t"+transcript_current.desc+"\t"+transcript_current.size+"\n");
            utils.Path.writer_transcript_clean.write(">"+transcript_current.id+"_"+transcript_current.name+"\n"+transcript_current.seq+"\n");
        }
        utils.Path.closeWriters();
    }

    /**
     * Launch the service, get the result file, parse the result file, display the annotations, write the annotations.
     */
    public void Launcher() throws Exception {
        Process p = null;
        try {
            File result_file = new File(working_dir + "/" + name + "_complete.txt");
            String command = "cpat.py -g "+utils.Path.file_region_nucl_id.getAbsolutePath()+"  -d /var/www/genotate.life/services/cpat/dat/Human_logitModel.RData -x /var/www/genotate.life/services/cpat/dat/Human_Hexamer.tsv -o "+result_file.getAbsolutePath();
            System.out.println("Message from ORF Worker bash -c " + command);
            p = Runtime.getRuntime().exec(command, null, working_dir);
            ReadStream s1 = new ReadStream("stdin", p.getInputStream());
            ReadStream s2 = new ReadStream("stderr", p.getErrorStream());
            if(AnnotRegion.services_messages) {
                s1.start();
                s2.start();
            }
            p.waitFor();
            parse(result_file);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null)
                p.destroy();
        }
    }
}
