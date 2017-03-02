import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.jsoup.Jsoup;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.jsoup.*;
import org.jsoup.Connection.Response;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import static org.jsoup.Jsoup.parse;

public class transcriptInfo {

	public File audioFilePath;	
	public String fileName;
	public String current;
	
	//readFile function code provided from http://alvinalexander.com/blog/post/java/how-open-read-file-java-string-array-list
	public List<String> readFiles(String fileN)
	{
	  List<String> records = new ArrayList<String>();
	  try
	  {
	    BufferedReader reader = new BufferedReader(new FileReader(fileN));
	    String line;
	    while ((line = reader.readLine()) != null)
	      records.add(line);
	    reader.close();
	    return records;
	  }
	  catch (Exception e)
	  {
	    System.err.format("Exception occurred trying to read '%s'.", fileN);
	    e.printStackTrace();
	    return null;
	  }
	}
	
	public void audioOnDisk(Configuration config, boolean launchDebug){
		String in;
    	Scanner input = new Scanner(System.in); 
    	
		if(launchDebug == true)
    		System.out.println("::Opening file::");
    	System.out.println("Please enter file name with extention, example: file.mp3 (file must exist in the \"AudioFiles\" folder)"); 
    	in = input.nextLine();
    	fileName = in;
    	//fileName = "ArtOfWarTrack2.mp3";
    	
    	try {
    		convert(launchDebug);
			transcribe(config, launchDebug);
		} catch (IOException e) {
			System.out.println("::Failed running transcribe method::");
			e.printStackTrace();
		}
	}
	
	public void audioOnLink(Configuration config, boolean launchDebug){
		String in;
		Scanner input = new Scanner(System.in); 
    	ByteBuffer audioBuffer = null;
    	byte[] audioBytes = null;
		
		System.out.println("Please enter URL. Make sure the URL is the source link that ends with \".mp3\""); 
    	in = input.nextLine();
    	String url = in;
    	//String url = "https://cc-eweb2.server.uic.edu:8443/echocontent/1648/4/5d980905-2267-41f0-96db-43e4c23c5a9d/audio.mp3";
    	
    	System.out.println("What would you like to name the file? (do not give file extention with name and make sure name is unique)"); 
    	in = input.nextLine();
    	fileName = in;
    	
    	if(launchDebug == true)
    		System.out.println("::Downloading " + url +" file::");

    	//Response audioData = Jsoup.connect(url)
    	//		.ignoreContentType(true)
    	//		.execute();
    	//making connection to download file using Jsoup
		try {
			audioBytes = Jsoup.connect(url)
						.ignoreContentType(true)
						//.userAgent("Mozilla") //to prevent 404
			            .maxBodySize(0)
			            .timeout(700500)
			    		.execute()
			    		.bodyAsBytes();
		} catch (IOException e) {
			System.out.println("::Failed making connection to URL::");
			e.printStackTrace();
		}
    	audioBuffer = ByteBuffer.wrap(audioBytes);
    	
    	//saving it in the AudioFiles folder
        File naf = new File(current+"\\AudioFiles\\" + fileName + ".mp3");
        
        //checking to see if name already exists
        int j = 1;
    	String temp = fileName;
        while(naf.exists()){
        	if(launchDebug == true)
     			 System.out.println("::name already exists, finding new name::");
        	naf = new File(current+"\\AudioFiles\\" + temp + j + ".mp3");
        	j++;
        	fileName = temp + j;
        }
        if(launchDebug == true)
        	System.out.println("::saving audio file at "+naf+" as " + fileName + ".mp3::");
        
        //saving
    	FileOutputStream output;
		try {
			output = (new FileOutputStream (naf));
	    	output.write(audioBytes);
	    	output.close();
		} catch (IOException e) {
			System.out.println("::Failed saving audio file's bytes::");
			e.printStackTrace();
		}
		fileName = fileName + ".mp3";
		
		try {
			convert(launchDebug);
			transcribe(config, launchDebug);
		} catch (IOException e) {
			System.out.println("::Failed running transcribe method::");
			e.printStackTrace();
		}
	}
	

	public void convert(boolean launchDebug){
		String[] pureFileName;

		//checking to see if audio file was saved correctly
       //splitting from file extention if it's in the fileName to use for command
       if(launchDebug == true) 
       	System.out.println("::Checking current directory for file:"+current+ "\\AudioFiles\\" +fileName+"::");
       audioFilePath = new File(current + "\\AudioFiles\\" + fileName);
       if( audioFilePath.exists() && !audioFilePath.isDirectory()) { 
       	if(launchDebug == true) 
       		System.out.println("::"+fileName + " exists in directory " + audioFilePath+ "::");
       }
       else{
       	System.out.println("::"+fileName + " does not exists in directory " + audioFilePath+ "::");
   	    System.exit(0);
       }

	   	//NEEDS MODIFYING IF YOUR MACHINE ISN'T WINDOWS 64 BIT
	       //running ffmpeg for windowos 7 64 bit to convert audio file to correct .wav format. Even .wav files will be converted
	   	//will be put in the AudioFile folder
	   	pureFileName = fileName.split("\\.");	//splitting from file extension to create name
	   	if(launchDebug == true) 
	   		System.out.println("::pure file name = "+pureFileName[0]+"::");
	       audioFilePath = new File(current + "\\AudioFiles\\Converted\\" + fileName); 
	       if(!audioFilePath.exists()){
	       	if(launchDebug == true) 
	       		System.out.println("::converting audio file::");
		        try{
		            Runtime runTime = Runtime.getRuntime();
		            Process conProc =runTime.exec("\""+current+"\\ffmpeg\\bin\\ffmpeg.exe\" -i \""+current+"\\AudioFiles\\" + fileName + "\" -acodec pcm_s16le -ac 1 -ar 16000 \""+current+"\\AudioFiles\\Converted\\" + pureFileName[0] + ".wav\"");
		        	if(launchDebug == true) 
		        		System.out.println("::running command: \""+current+"\\ffmpeg\\bin\\ffmpeg.exe\" -i \""+current+"\\AudioFiles\\" + fileName + "\" -acodec pcm_s16le -ac 1 -ar 16000 \""+current+"\\AudioFiles\\Converted\\" + pureFileName[0] + ".wav\"");	
		        	//code to prevent hanging from http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
		        	InputStream stderr = conProc.getErrorStream();
		             InputStreamReader isr = new InputStreamReader(stderr);
		             BufferedReader br = new BufferedReader(isr);
		             String line = null;
		             System.out.println("<ERROR>");
		             while ( (line = br.readLine()) != null)
		                 System.out.println(line);
		             isr.close();
		        	 int exitVal = conProc.waitFor();
		             System.out.println("Process exitValue: " + exitVal);
		        } 
		        catch (IOException | InterruptedException e) {
		        	System.out.println("::command failed::");
			        e.printStackTrace();
			    }
	       }
	       // setting filename to new converted type
	   	fileName = pureFileName[0] + ".wav";
	   	audioFilePath = new File(current + "\\AudioFiles\\Converted\\" + fileName); 
	}
	
	//declaring method so it can be used when we downcast in mainInterface
	public void transcribeHelper(Configuration config, boolean launchDebug, String commandFile) throws IOException{}
	
	//transcribing code provided from CMU Sphinx's website http://cmusphinx.sourceforge.net/wiki/tutorialsphinx4
	public void transcribe(Configuration config, boolean launchDebug) throws IOException{
		File aF = audioFilePath;
		int i = 1; //counter
		String[] pureFileName;
		
		if(launchDebug == true) 
			System.out.println("::In transcriptInfo's transcribe method::");
		

	       // String current = new java.io.File( "." ).getCanonicalPath();
	        if(launchDebug == true) 
	        	System.out.println("::Current dir:"+current+"::");
		
        
        if( audioFilePath.exists()) { 
        	if(launchDebug == true) 
        		System.out.println("::"+fileName + " exists in directory " + audioFilePath+ "::");
        }
        else
        {
        	System.out.println("::converted " + fileName + " does not exists in directory " + audioFilePath+ "::");
    	    System.exit(0);
        }
        
        
        
        //creating transcript file by associating it with it's name + number
        if (!fileName.contains(".")) 
            throw new IllegalArgumentException("::String " + fileName + " does not contain \".\"::");

    	pureFileName = fileName.split("\\.");	//splitting from file extension to create name
        System.out.println("::file="+pureFileName[0]+"::");
        pureFileName[0] = pureFileName[0] + ".txt";
        if(launchDebug == true) 
        	System.out.println("::Checking current directory for file:"+current+"\\Transcripts\\"+pureFileName[0]+"::");
        
        
        //seeing if transcript file already exists, if so, check again with name + (number++)
        audioFilePath = new File(current + "\\Transcripts\\" + pureFileName[0]); 
        while(audioFilePath.exists()){
        	System.out.println("::"+ pureFileName[0] + " exists in directory " + aF + "::");
        	i++;
        	pureFileName = fileName.split("\\.");
        	pureFileName[0] = pureFileName[0] + i + ".txt";
        	audioFilePath = new File(current + "\\Transcripts\\" + pureFileName[0]);
        }
        if(launchDebug == true) 
        	System.out.println("::"+ pureFileName[0] + " does not exists in directory, creating transcript file " + audioFilePath+ "::");
        
        
		StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(config);
        InputStream inStream = new FileInputStream(aF);

        recognizer.startRecognition(inStream);
        SpeechResult result;
        
    	FileWriter fw = new FileWriter("Transcripts\\" + pureFileName[0], true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    PrintWriter out = new PrintWriter(bw);
        try
    	{
			while ((result = recognizer.getResult()) != null) {
				if(launchDebug == true)
					System.out.println("::Translated section to: " + result.getHypothesis() + "::");
			   // out.print(" " + result.getHypothesis() + " ");
			    bw.write(result.getHypothesis() + " ");
			}
    	} catch (IOException e) {
    		System.err.format("::Exception occurred trying to write to transcriptfile::");
    		e.printStackTrace();
    	}
        out.close();
        bw.close();
        fw.close();
        recognizer.stopRecognition();
	}
}
