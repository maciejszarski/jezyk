import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import net.sourceforge.align.*;
import net.sourceforge.align.coretypes.Alignment;
import net.sourceforge.align.ui.console.Maligna;
import net.sourceforge.align.ui.console.command.Command;
import net.sourceforge.align.ui.console.command.CommandFactory;

public class Algorithm {
	private final String[] ignoreFiles = new String[]{".DS_Store"};
	private String filesPathEN = "files/en";
	private String filesPathPL = "files/pl";
	private String unformattedSentencesPathEN = "sentences/unformatted/en";
	private String unformattedSentencesPathPL = "sentences/unformatted/pl";
	private String formattedSentencesPathEN = "sentences/formatted/en";
	private String formattedSentencesPathPL = "sentences/formatted/pl";
	private FilenameFilter filenameFilter = new FilenameFilter() {
	    public boolean accept(File folder, String name) {
	        return !Arrays.asList(ignoreFiles).contains(name);
	    }
	};
		
	public Algorithm(){
		filesPathEN = Algorithm.class.getResource(filesPathEN).getPath().replaceAll("%20"," ");
		filesPathPL = Algorithm.class.getResource(filesPathPL).getPath().replaceAll("%20"," ");
		unformattedSentencesPathEN = Algorithm.class.getResource(unformattedSentencesPathEN).getPath().replaceAll("%20"," ");
		unformattedSentencesPathPL = Algorithm.class.getResource(unformattedSentencesPathPL).getPath().replaceAll("%20"," ");
		formattedSentencesPathEN = Algorithm.class.getResource(formattedSentencesPathEN).getPath().replaceAll("%20"," ");
		formattedSentencesPathPL = Algorithm.class.getResource(formattedSentencesPathPL).getPath().replaceAll("%20"," ");
	}
	
	public void formatSentences(){
		formatSentences(unformattedSentencesPathEN,formattedSentencesPathEN);
		formatSentences(unformattedSentencesPathPL,formattedSentencesPathPL);	
		System.out.println("ok");
	}
	
	private void formatSentences(String unformattedSentencesPath, String formattedSentencesPath){
		try{
			File folder = new File(unformattedSentencesPath);
			for (File file : folder.listFiles(filenameFilter)){
				List<String> sentences = new ArrayList<String>();
				FileInputStream in = new FileInputStream(unformattedSentencesPath+"/"+file.getName());			    
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
			    String line;
			    while ((line = br.readLine()) != null) {
			    	sentences.add(line);			    	
			    }
			    br.close();
			    
			    BufferedWriter output = new BufferedWriter(new FileWriter(formattedSentencesPath+"/"+file.getName())); output.close(); //clear file
				output = new BufferedWriter(new FileWriter(formattedSentencesPath+"/"+file.getName(), true));
				for(int i=0; i<sentences.size(); i++){
					System.out.println(i+ " " + formattedSentencesPath+"/"+file.getName()+" "+sentences.get(i));
					output.append("<s snum="+formatIndex(i+1)+"> " + sentences.get(i) + " </s>");
					output.newLine();
				}
				output.close();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private String formatIndex(int index){
		int integerLength = (int)Math.ceil(Math.log10(index));
		int zeros = 4 - integerLength;
		String formattedIndex = "";
		for(int i=0; i<zeros; i++){
			formattedIndex += "0"; 
		}
		formattedIndex += index;
		return formattedIndex;
		
	}
	
	public void alignSentences() throws Exception{
		File folderEN = new File(filesPathEN);
		File folderPL = new File(filesPathPL);
				
		File[] folderListEN = folderEN.listFiles(filenameFilter);
		File[] folderListPL = folderPL.listFiles(filenameFilter);
		
		if(folderListEN.length == folderListPL.length){
			for (File file : folderListEN){
				Process process = alignSentences(file.getName());
				process.waitFor();
			}
		}
		else{
			throw new Exception();
		}
	}
	
	private Process alignSentences(String fileName) throws Exception{
		String malignaPath = Algorithm.class.getResource("maligna/bin").getPath().replaceAll("%20"," ");
		String pipe = malignaPath + "/maligna parse -c txt "+filesPathEN+"/"+fileName+" "+filesPathPL+"/"+fileName+" | " +
						 malignaPath + "/maligna modify -c split-sentence | " +
						 malignaPath + "/maligna modify -c trim | " +
						 malignaPath + "/maligna align -c viterbi -a poisson -n word -s iterative-band | " + 
						 malignaPath + "/maligna select -c one-to-one | " +
						 malignaPath + "/maligna format -c txt "+unformattedSentencesPathEN+"/"+fileName+" "+unformattedSentencesPathPL+"/"+fileName;
		String[] command = {
				"sh",
				"-c",
				pipe
				};
		
		return Runtime.getRuntime().exec(command);	
	}
}
