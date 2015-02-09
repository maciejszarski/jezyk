import java.util.Hashtable;
import java.util.List;

public class Main {

	public static void main(String[] args){
		Algorithm algorithm = new Algorithm();
		try{
			algorithm.alignSentences();
			algorithm.generateConfigFile();
			algorithm.alignWords();
	//		algorithm.createDictionary();
	//		algorithm.printResults();
			System.out.println("Done.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
