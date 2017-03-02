//Lee Zimmerman
//Audio Transcriber using CMU Sphinx Java API
//CS474 Final Project
//download files online using JSOUP
//uses ffmpeg executable to convert audio to correct format if needed
//reformating wav file that has already been formated seems to have no adverse affects

// 8-30 lecture: https://uic-cc-ess1.ad.uic.edu:8443/ess/echo/presentation/7892f91c-8ace-4af7-915e-3ca45f1b6efb?ec=true
// 8-30  lecture audio link: https://cc-eweb2.server.uic.edu:8443/echocontent/1635/2/7892f91c-8ace-4af7-915e-3ca45f1b6efb/audio.mp3

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import org.jsoup.*;
import org.jsoup.Connection.Response;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//import org.apache.commons.codec.binary.Base64;
import static org.jsoup.Jsoup.parse;

public class mainInterface {       
	
	//readFile function code provided from http://alvinalexander.com/blog/post/java/how-open-read-file-java-string-array-list
	private List<String> readFile(String filename)
	{
	  List<String> records = new ArrayList<String>();
	  try
	  {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = reader.readLine()) != null)
	      records.add(line);
	    reader.close();
	    return records;
	  }
	  catch (Exception e)
	  {
	    System.err.format("Exception occurred trying to read '%s'.", filename);
	    e.printStackTrace();
	    return null;
	  }
	}
               
  
	 private static void print(String msg, Object... args) {
	        System.out.println(String.format(msg, args));
	    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }

    
    public static void main(String[] args) throws Exception {
    	String[] pureFileName = "demo.wav".split("\\."); //initializing default value to make compiler happy
    	boolean launchDebug = false;
    	Scanner input = new Scanner(System.in); 
    	File SpeechDir = new File("src/main/java/Transcripts");
    	transcriptInfo tInfo;
        String fileName = "out.wav";
        File audioFilePath; 
    	    	
    	//self explanitory
        System.out.println("Would you like to have debug output? (yes/no)");
        String in = input.nextLine();
        if(in.equals("yes"))
       	 launchDebug = true;    	
        
        //getting project directory
        String current = new java.io.File( "." ).getCanonicalPath();
        if(launchDebug == true) 
        	System.out.println("::Current dir:"+current+"::");
        
        //creating folder to hold transcripts
    	if(!SpeechDir.exists()){
    		if(launchDebug == true)
    	    		System.out.println("::creating Transcripts directory::");
    		try{
    		        SpeechDir.mkdir();
		    } 
		    catch(SecurityException se){
		    	System.err.format("::Unable to make transcript directory::");
	    	    se.printStackTrace();
	    	    System.exit(0);
		    } 
    	}
    	else{
    		 if(launchDebug == true)
    	    		System.out.println("::Transcripts directory exists::");
    	} 
    	 
    	//Setting CMU Sphinx Configuration for translating
        Configuration configuration = new Configuration();
        if(launchDebug == true)
    		System.out.println("::Setting CMU Sphinx Configuration::");       
        try{
        	if(launchDebug == true)
        		System.out.println("::Setting CMU Sphinx Acustic Model::");
        	configuration
            	.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        	if(launchDebug == true)
        		System.out.println("::Setting CMU Sphinx Dictionary::");
		    configuration
		        .setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		    if(launchDebug == true)
        		System.out.println("::Setting CMU Sphinx ModelPath::");
		    configuration
            	.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        }
        catch(Exception e){
        	System.err.format("::Exception occurred trying to configure CMU Sphinx resources::");
    	    e.printStackTrace();
    	    System.exit(0);
        }
        
        //for running test files to show program works WARNING: MAY TAKE SOME TIME SINCE ONE TEST DOWNLOADS, CONVERTS, AND TRANSCRIBES AN ENTIRE LECTURE
    	System.out.println("Would you like to run example tests");
    	in = input.nextLine();
    	if(in.equals("yes")){
    		if(launchDebug == true) 
            	System.out.println("::Running tests from list document::");
    		tInfo = new multipleTranscriptInfo();
	       	tInfo.current = current;
    		tInfo.transcribeHelper(configuration, launchDebug, "TestFiles\\testList.txt");
    	}
    	tInfo = new transcriptInfo();
    	
    	//asks if you want the program to read a text document with a list of files/links to convert/transcribe or do you just want to do one URL or file via user input
    	System.out.println("Would you like to open files from a list document? (yes/no)");
    	in = input.nextLine();
    	if(in.equals("yes")){
    		if(launchDebug == true) 
            	System.out.println("::Running files from list document::");
    		
    		System.out.println("Please enter list document name with file extention. Example: list.txt (make sure the text document is in the project directory, the links are valid, and the files listed are in the AudioFiles folder)");
        	in = input.nextLine();
    		
    		tInfo = new multipleTranscriptInfo();
	       	tInfo.current = current;
    		tInfo.transcribeHelper(configuration, launchDebug, in);
    	}//otherwise we just load one audio file specified from the user from a link or in our AudioFiles directory
    	else{
    		if(launchDebug == true) 
    			System.out.println("::Running off of user input::");

	        System.out.println("Would you like to open an audio file from a link or in the \"AudioFiles\" folder? (press 1 for link, 2 for folder)");
	    	int i = input.nextInt(); 
	    	in = input.nextLine();
	    	
	        
	        
	       if(i == 1){
	
	       	tInfo.current = current;
	       	tInfo.audioOnLink(configuration, launchDebug);
	    	   
	       }
	       if(i == 2){
	
	       	tInfo.current = current;
	       	tInfo.audioOnDisk(configuration, launchDebug);
	    	   
	       }
	        
    	}
        if(launchDebug == true) 
        	System.out.println("--==END OF LINE==--");
    }
    
    
    
    
}