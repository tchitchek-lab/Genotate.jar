package utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.File;
import java.nio.channels.FileChannel;

/**
 * Provide methods to manage files.
 */
public class FileUtils {

	/**
	 * Concat all text file in a directory in another file
	 * @param directory directory that must contains only text file
	 * @param outputcombinefastafile is the result File merged from the directory 
	 * @throws Exception TASK
	 */
	public static void combinedirectory(File directory,File outputcombinefastafile) throws Exception{
		String [] file=directory.list();
		BufferedWriter writer = new BufferedWriter(new PrintWriter(outputcombinefastafile, "UTF-8"));
		for(int i=0;i<file.length;i++){
			BufferedReader reader = new BufferedReader(new FileReader(directory.getAbsolutePath()+"/"+file[i]));
			String line;
			while((line=reader.readLine())!=null){
				writer.write(line+"\n");
			}
			reader.close();
		}
		writer.close();
	}

	/**
	 * Compress a folder in a .zip archive
	 * @param srcFolder path of the folder to compress
	 * @param zip_path path of the compressed folder archive in format .zip 
	 * @throws Exception TASK
	 */
	static public void zipFolder(String srcFolder, String zip_path) throws Exception {
		FileOutputStream fileoutputstream = new FileOutputStream(zip_path);
		ZipOutputStream zip = new ZipOutputStream(fileoutputstream);
		addFolderToZip("", srcFolder, zip);
		zip.flush();
		zip.close();
		fileoutputstream.close();
	}

	/**
	 * Add a File to a zip archive
	 * @param path TASK
	 * @param srcFile file to add 
	 * @param zip current open ZipOutputstream. 
	 * @throws Exception TASK
	 */
	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)throws Exception {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buffer = new byte[1024];
			int length;
			FileInputStream inputstream = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((length = inputstream.read(buffer)) > 0) {
				zip.write(buffer, 0, length);
			}
			inputstream.close();
		}
	}

	/**
	 * Create Zip from a directory
	 * @param path TASK
	 * @param srcFolder folder to add 
	 * @param zip current open ZipOutputstream.
	 * @throws java.lang.Exception TASK
	 */
	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
			}
		}
	}
	
	/**
	 * Copy a file to a specified destination wit a new name.
	 * @param source TASK
	 * @param dest TASK
	 * @throws java.lang.Exception TASK
	 */
	public static void copy(File source, File dest) throws Exception {
	    FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    FileInputStream fis = new FileInputStream(source);
	    FileOutputStream fos = new FileOutputStream(dest);
	    sourceChannel = fis.getChannel();
	    destChannel = fos.getChannel();
	    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	    sourceChannel.close();
	    destChannel.close();
	    fis.close();
	    fos.close();
	}
}
