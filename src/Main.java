import java.util.Hashtable;
import java.util.List;

public class Main {

	public static void main(String[] args){
		Algorithm algorithm = new Algorithm();
		try{
			algorithm.alignSentences();
			algorithm.generateConfigFile();
			algorithm.alignWords();
			Hashtable<String,Hashtable<String,Integer>> translations = algorithm.getTranslations();
			Hashtable<String,String> dictionary = algorithm.createDictionary(translations);
			algorithm.printDictionary(dictionary);
			System.out.println("Done.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
