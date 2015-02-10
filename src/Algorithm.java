import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

public class Algorithm {
	private final String[] ignoreFiles = new String[]{".DS_Store"};
	private String filesPathEN = "files/en";
	private String filesPathPL = "files/pl";
	private String sentencesPath = "sentences";
	private String berkeleyAlignerPath = "berkeleyaligner";
	private String alignedPath = "aligned";
	private String taggerPath = "treetagger";
	private FilenameFilter filenameFilter = new FilenameFilter() {
	    public boolean accept(File folder, String name) {
	        return !Arrays.asList(ignoreFiles).contains(name);
	    }
	};
	String[] lemmas;
	int lemmasIndex;
		
	public Algorithm(){
		filesPathEN = Algorithm.class.getResource(filesPathEN).getPath().replaceAll("%20"," ");
		filesPathPL = Algorithm.class.getResource(filesPathPL).getPath().replaceAll("%20"," ");
		sentencesPath = Algorithm.class.getResource(sentencesPath).getPath().replaceAll("%20"," ");
		berkeleyAlignerPath = Algorithm.class.getResource(berkeleyAlignerPath).getPath().replaceAll("%20"," ");
		alignedPath = Algorithm.class.getResource(alignedPath).getPath().replaceAll("%20"," ");
		taggerPath = Algorithm.class.getResource(taggerPath).getPath().replaceAll("%20"," ");
	}
	
	public void alignSentences() throws Exception{
		System.out.println("Aligning sentences...");
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
						 malignaPath + "/maligna format -c txt "+sentencesPath+"/"+fileName+".en "+sentencesPath+"/"+fileName+".pl";
		String[] command = {"sh", "-c", pipe};
		
		return Runtime.getRuntime().exec(command);	
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
	
	public void generateConfigFile(){
		try {
			System.out.println("Generating config file..."+alignedPath);
			String[] config = new String[]{
					"forwardModels	MODEL1 HMM",
					"reverseModels	MODEL1 HMM",
					"mode	JOINT JOINT",
					"iters	2 2",
					"execDir	"+alignedPath,
					"create",
					"saveParams	true",
					"numThreads	1",
					"msPerLine	10000",
					"alignTraining",
					"foreignSuffix	pl",
					"englishSuffix	en",
					"lowercase",
					"trainSources	"+sentencesPath,
					"sentences	MAX",
					"testSources	"+sentencesPath,
					"maxTestSentences	MAX",
					"offsetTestSentences	0",
					"competitiveThresholding	true",
					"overwriteExecDir	true"
			};
			PrintWriter out = new PrintWriter(berkeleyAlignerPath+"/en-pl.conf");
			for(int i=0; i<config.length; i++){
				out.println(config[i]);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void alignWords(){
		System.out.println("Aligning words...");
		try {
			String cmd = "java -server -mx1000m -jar "+berkeleyAlignerPath+"/berkeleyaligner.jar ++"+berkeleyAlignerPath+"/en-pl.conf";
			String[] command = {"sh", "-c", cmd};
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	public List<String[]> generateLemmats(String alignedFilePath, String modelPath){
		System.out.println("Generating lemmats...");
		List<String[]> result = new ArrayList<String[]>();
		try {
			File file = new File(alignedPath+"/"+alignedFilePath);
			Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line;
			List<String> words = new ArrayList<String>();
			List<Integer> lengths = new ArrayList<Integer>();
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split("[., :;!?]+");
				words.addAll(Arrays.asList(splitted));
				lengths.add(splitted.length);
			}
			br.close();
			String[] lemmats = lemmatize(words,modelPath);
			int i = 0;
			while(lemmats.length>0){
				String[] lemmatsLine = Arrays.copyOfRange(lemmats, 0, lengths.get(i));
				result.add(lemmatsLine);
				lemmats = Arrays.copyOfRange(lemmats, lengths.get(i), lemmats.length);
				i++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}
	
	public Hashtable<String,Hashtable<String,Integer>> getTranslations(List<String[]> lemmats1, List<String[]> lemmats2, boolean reverse){
		System.out.println("Preparing translations from aligned files...");
		Hashtable<String,Hashtable<String,Integer>> translations = new Hashtable<String,Hashtable<String,Integer>>();
		try {
			File alignFile = new File(alignedPath+"/training.align");
			Reader alignReader = new InputStreamReader(new FileInputStream(alignFile), "UTF-8");
			BufferedReader alignBR = new BufferedReader(alignReader);
			String alignLine;
			int i = 0;
			while ((alignLine = alignBR.readLine()) != null) {
			   String[] aligns = alignLine.split(" ");
			   String[] words1 = lemmats1.get(i);
			   String[] words2 = lemmats2.get(i);
			   for(int j=0; j<aligns.length; j++){
				   String[] pair = aligns[j].split("-");
				   String word1, word2;
				   if(reverse){
					   word2 = words2[Integer.parseInt(pair[1])];
					   word1 = words1[Integer.parseInt(pair[0])];
				   }
				   else{
					   word2 = words2[Integer.parseInt(pair[0])];
					   word1 = words1[Integer.parseInt(pair[1])];
				   }
				   if(translations.containsKey(word1)){
					   int value = 0;
					   if(translations.get(word1).containsKey(word2)){
						   value = translations.get(word1).get(word2) + 1;
					   }
					   translations.get(word1).put(word2, value);
				   }
				   else{
					   Hashtable<String,Integer> translation = new Hashtable<String,Integer>();
					   translation.put(word2, 1);
					   translations.put(word1, translation);
				   }
			   }
			   i++;
			}
			alignBR.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return translations;
	}
	
	public Hashtable<String,String> createDictionary(Hashtable<String,Hashtable<String,Integer>> translations){
		System.out.println("Creating dictionary...");
		Hashtable<String,String> dictionary = new Hashtable<String,String>();
		for (String enWord : translations.keySet()) {
			int maxTranslations = 0;
			String bestTranslation = "";
			for (String plWord : translations.get(enWord).keySet()) {
				if(translations.get(enWord).get(plWord)>maxTranslations){
					maxTranslations = translations.get(enWord).get(plWord);
					bestTranslation = plWord;
				}
			}
			dictionary.put(enWord, bestTranslation);
		}
		return dictionary;
	}
	
	public void printDictionary(Hashtable<String,String> dictionary){
		System.out.println("Result:");
		for (String word : dictionary.keySet()) {
			System.out.println(word + " - " + dictionary.get(word));
		}
	}
	
	public String[] lemmatize(List<String> words, String modelPath){
		//more doc on the tagger at https://code.google.com/p/tt4j/
		lemmas = new String[words.size()];
		lemmasIndex = 0;
		System.setProperty("treetagger.home", taggerPath);
	    TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
	    try {
	    	tt.setModel(modelPath);
	    	tt.setHandler(new TokenHandler<String>() {
		        public void token(String token, String pos, String lemma) {
		        	lemmas[lemmasIndex] = lemma;
		        	lemmasIndex++;
		        }
		    });
    		tt.process(words);
	    } catch(IOException e){
	    	e.printStackTrace();
	    } catch(TreeTaggerException e) {
			e.printStackTrace();
		}
	    finally {
	    	tt.destroy();
	    }
	    return lemmas;
	}
	
	public Hashtable<String,String> filterDictionary(Hashtable<String,String> dictionary, Hashtable<String,String> reverseDictionary){
		System.out.println("Removing incorrect translations from dictionary...");
		Hashtable<String,String> dictionaryCopy = new Hashtable<String,String>(dictionary);
		int n1 = dictionary.size();
		for (String wordEN : dictionaryCopy.keySet()) {
			String wordPL = dictionaryCopy.get(wordEN);
			if(wordEN != reverseDictionary.get(wordPL)){
				dictionary.remove(wordEN);
			}
		}
		int n2 = dictionary.size();
		System.out.println("Removed "+(n1-n2)+" words, left "+n2+" words.");
		return dictionary;
	}
	
}
