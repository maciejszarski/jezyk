import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Algorithm {
	private final String[] ignoreFiles = new String[]{".DS_Store"};
	private String filesPathEN = "files/en";
	private String filesPathPL = "files/pl";
	private String sentencesPath = "sentences";
	private String berkeleyAlignerPath = "berkeleyaligner";
	private String alignedPath = "aligned";
	private FilenameFilter filenameFilter = new FilenameFilter() {
	    public boolean accept(File folder, String name) {
	        return !Arrays.asList(ignoreFiles).contains(name);
	    }
	};
		
	public Algorithm(){
		filesPathEN = Algorithm.class.getResource(filesPathEN).getPath().replaceAll("%20"," ");
		filesPathPL = Algorithm.class.getResource(filesPathPL).getPath().replaceAll("%20"," ");
		sentencesPath = Algorithm.class.getResource(sentencesPath).getPath().replaceAll("%20"," ");
		berkeleyAlignerPath = Algorithm.class.getResource(berkeleyAlignerPath).getPath().replaceAll("%20"," ");
		alignedPath = Algorithm.class.getResource(alignedPath).getPath().replaceAll("%20"," ");
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
}
