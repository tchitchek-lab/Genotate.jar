package core;

import java.io.*;

/**
 * Run the services on a transcript fasta file
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
        //int idx_region_id =0;
        int idx_score = 5;
        int transcript_id = 0;
        BufferedReader reader = new BufferedReader(new FileReader(result_file));
        String line;
        while((line=reader.readLine())!=null) {
            System.out.println(line);
            if(line.startsWith("mRNA_size") ){
                continue;
            }
            String[] splittab = line.split("\t");
            if(splittab.length < 5){
                continue;
            }
            //int region_id = Integer.parseInt(splittab[idx_region_id]);
            float identity = Float.parseFloat(splittab[idx_score]);
            if(identity < cpat_threshold){
                core.FindOrf.transcript_cpat.put(transcript_id, 0);
            }else {
                core.FindOrf.transcript_cpat.put(transcript_id, 1);
            }
            transcript_id++;
        }
        reader.close();
    }

    /**
     * Launch the service, get the result file, parse the result file, set cpat score.
     */
    public void Launcher() throws Exception {
        Process p = null;
        try {
            File result_file = new File(working_dir + "/" + name + "_complete.txt");
            String command = "cpat.py -g "+utils.Path.inputtranscriptfastafile.getAbsolutePath()+"  -d /var/www/genotate.life/services/cpat/dat/Human_logitModel.RData -x /var/www/genotate.life/services/cpat/dat/Human_Hexamer.tsv -o "+result_file.getAbsolutePath();
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
