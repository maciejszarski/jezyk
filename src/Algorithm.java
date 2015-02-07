import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	private String filesPathEN = "files/en/";
	private String filesPathPL = "files/pl/";
		
	public Algorithm(){
		filesPathEN = Algorithm.class.getResource(filesPathEN).getPath().replaceAll("%20"," ");
		filesPathPL = Algorithm.class.getResource(filesPathPL).getPath().replaceAll("%20"," ");
	}

//	public Hashtable<String,List<String>> readTexts(){
//		Hashtable<String,List<String>> texts = new Hashtable<String,List<String>>();
//		texts.put("en", readTexts(filesPathEN));
//		texts.put("pl", readTexts(filesPathPL));
//		return texts;
//	}
//	
//	private List<String> readTexts(String filesPath){
//		List<String> texts = new ArrayList<String>();
//		File folder = new File(filesPath);
//		try{
//			for (File file : folder.listFiles()){
//				texts.add(new Scanner(file).useDelimiter("\\Z").next());
//			}
//		}
//		catch(IOException e){
//			e.printStackTrace();
//		}
//		return texts;
//	}	
	
	public void alignSentences() throws Exception{
		File folderEN = new File(filesPathEN);
		File folderPL = new File(filesPathPL);
		File[] folderListEN = folderEN.listFiles();
		File[] folderListPL = folderPL.listFiles();
		
		if(folderListEN.length == folderListPL.length){
			for (File file : folderListEN){
				alignSentences(file.getName());
			}
		}
		else{
			throw new Exception();
		}
	}
	
	private void alignSentences(String fileName) throws Exception{
		String malignaPath = Algorithm.class.getResource("maligna/bin").getPath().replaceAll("%20"," ");
		String resultsPath = Algorithm.class.getResource("maligna/results").getPath().replaceAll("%20"," ");
		String pipe = malignaPath + "/maligna parse -c txt "+filesPathEN+"/"+fileName+" "+filesPathPL+"/"+fileName+" | " +
						 malignaPath + "/maligna modify -c split-sentence | " +
						 malignaPath + "/maligna modify -c trim | " +
						 malignaPath + "/maligna align -c viterbi -a poisson -n word -s iterative-band | " + 
						 malignaPath + "/maligna select -c one-to-one | " +
						 malignaPath + "/maligna format -c txt "+resultsPath+"/en/"+fileName+" "+resultsPath+"/pl/"+fileName;
		String[] command = {
				"sh",
				"-c",
				pipe
				};
		
		Runtime.getRuntime().exec(command);	
	}
}
