import java.util.Hashtable;
import java.util.List;

public class Main {
	private final static String modelPathEN = "en.bin";
	private final static String modelPathPL = "pl.bin";
	private final static String alignedFileEN = "training.en";
	private final static String alignedFilePL = "training.pl";

	public static void main(String[] args){
		Algorithm algorithm = new Algorithm();
		try{
			algorithm.alignSentences();
			algorithm.generateConfigFile();
			algorithm.alignWords();
			List<String[]> lemmatsEN = algorithm.generateLemmats(alignedFileEN,modelPathEN);
			List<String[]> lemmatsPL = algorithm.generateLemmats(alignedFilePL,modelPathPL);
			Hashtable<String,Hashtable<String,Integer>> translations = algorithm.getTranslations(lemmatsEN,lemmatsPL,false);
			Hashtable<String,Hashtable<String,Integer>> reverseTranslations = algorithm.getTranslations(lemmatsPL,lemmatsEN,true);
			Hashtable<String,String> dictionary = algorithm.createDictionary(translations);
			Hashtable<String,String> reverseDictionary = algorithm.createDictionary(reverseTranslations);
			dictionary = algorithm.filterDictionary(dictionary,reverseDictionary);
			algorithm.printDictionary(dictionary);
			System.out.println("Done.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
