import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

public class Algorithm {
	private final String modelPathEN = "en.bin";
	private final String modelPathPL = "pl.bin";
	private String taggerPath = "treetagger/";
	private String filesPathEN = "files/en/";
	private String filesPathPL = "files/pl/";
	
	private List<String[]> textsEN;
	private List<String[]> textsPL;
	
	public Algorithm(){
		taggerPath = Algorithm.class.getResource(taggerPath).getPath().replaceAll("%20"," ");
		filesPathEN = Algorithm.class.getResource(filesPathEN).getPath().replaceAll("%20"," ");
		filesPathPL = Algorithm.class.getResource(filesPathPL).getPath().replaceAll("%20"," ");
	}

	public void readTexts(){
		textsEN = readTexts(filesPathEN);
		textsPL = readTexts(filesPathPL);
	}
	
	private List<String[]> readTexts(String filesPath){
		List<String[]> texts = new ArrayList<String[]>();
		File folder = new File(filesPath);
		try{
			for (File file : folder.listFiles()){
				texts.add(segmentate(new Scanner(file).useDelimiter("\\Z").next()));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return texts;
	}
	
	public void tagTexts(){
		tagTexts(textsEN, modelPathEN);
		tagTexts(textsPL, modelPathPL);
	}
	
	private String[] segmentate(String text){
		String[] segmentatedText = text.split(" ");
		return segmentatedText;
	}
	
	private void tagTexts(List<String[]> texts, String modelPath){
		
		//more doc on the tagger at https://code.google.com/p/tt4j/
		System.setProperty("treetagger.home", taggerPath);
	    TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
	    try {
	    	tt.setModel(modelPath);
	    	tt.setHandler(new TokenHandler<String>() {
		        public void token(String token, String pos, String lemma) {
		        	System.out.println(token + "\t" + pos + "\t" + lemma);
		        }
		    });
	    	for(int i=0; i<texts.size(); i++){
	    		tt.process(Arrays.asList(texts.get(i)));
	    	}
	    } catch(IOException e){
	    	e.printStackTrace();
	    } catch(TreeTaggerException e) {
			e.printStackTrace();
		}
	    finally {
	    	tt.destroy();
	    }
	}
}
