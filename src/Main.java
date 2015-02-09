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
//			algorithm.alignSentences();
//			algorithm.generateConfigFile();
//			algorithm.alignWords();
			List<String[]> lemmatsEN = algorithm.generateLemmats(alignedFileEN,modelPathEN);
			List<String[]> lemmatsPL = algorithm.generateLemmats(alignedFilePL,modelPathPL);
			Hashtable<String,Hashtable<String,Integer>> translations = algorithm.getTranslations(lemmatsEN,lemmatsPL);
			Hashtable<String,String> dictionary = algorithm.createDictionary(translations);
			algorithm.printDictionary(dictionary);
			System.out.println("Done.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
