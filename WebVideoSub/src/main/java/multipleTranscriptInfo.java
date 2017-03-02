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

//class made for when we're transcribing multiple files
public class multipleTranscriptInfo extends transcriptInfo {
	public List<File> audioFilePaths;	
	
	public void audioOnDisk(Configuration config, boolean launchDebug, String dir){
		
		if(launchDebug == true)
    		System.out.println("::Opening file::");
    	
    	try {
    		convert(launchDebug, dir);
			transcribe(config, launchDebug);
		} catch (IOException e) {
			System.out.println("::Failed running transcribe method::");
			e.printStackTrace();
		}
	}
	
	public void audioOnLink(Configuration config, boolean launchDebug, String url, String name, String dir){
		String in;
		Scanner input = new Scanner(System.in); 
    	ByteBuffer audioBuffer = null;
    	byte[] audioBytes = null;
		
		
    	fileName = name;
    	
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
        File naf = new File(current+ dir + fileName + ".mp3");
        
        //checking to see if name already exists
        int j = 1;
    	String temp = fileName;
        while(naf.exists()){
        	if(launchDebug == true)
     			 System.out.println("::name already exists, finding new name::");
        	j++;
        	naf = new File(current+ dir + temp + j + ".mp3");
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
			convert(launchDebug, dir);
			transcribe(config, launchDebug);
		} catch (IOException e) {
			System.out.println("::Failed running transcribe method::");
			e.printStackTrace();
		}
	}
	

	public void convert(boolean launchDebug, String dir){
		String[] pureFileName;

		//checking to see if audio file was saved correctly
       //splitting from file extention if it's in the fileName to use for command
       if(launchDebug == true) 
       	System.out.println("::Checking current directory for file:"+current+ dir +fileName+"::");
       audioFilePath = new File(current + dir + fileName);
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
	       audioFilePath = new File(current + dir + "\\Converted\\" + fileName); 
	       if(!audioFilePath.exists()){
	       	if(launchDebug == true) 
	       		System.out.println("::converting audio file::");
		        try{
		            Runtime runTime = Runtime.getRuntime();
		        	Process conProc = runTime.exec("\""+current+"\\ffmpeg\\bin\\ffmpeg.exe\" -i \""+current+ dir + fileName + "\" -acodec pcm_s16le -ac 1 -ar 16000 \""+current+dir+"Converted\\" + pureFileName[0] + ".wav\"");
		        	if(launchDebug == true) 
		        		System.out.println("::running command: \""+current+"\\ffmpeg\\bin\\ffmpeg.exe\" -i \""+current+ dir + fileName + "\" -acodec pcm_s16le -ac 1 -ar 16000 \""+current+dir+"Converted\\" + pureFileName[0] + ".wav\"");	
		        	 
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
	   	audioFilePath = new File(current + dir + "Converted\\" + fileName); 
	}
	
	//transcribing code provided from CMU Sphinx's website http://cmusphinx.sourceforge.net/wiki/tutorialsphinx4
	public void transcribeHelper(Configuration config, boolean launchDebug, String commandFile) throws IOException{
		String dir = "\\AudioFiles\\";
		
		//see if we are running the test commands
		if(commandFile.equals("TestFiles\\testList.txt")){
			if(launchDebug == true)
	    		System.out.println("::Reading from test command file::");
			dir = "\\TestFiles\\";
		}
		else{
			if(launchDebug == true)
	    		System.out.println("::Reading from user command file::");
		}
		
		
		List<String> audioFiles = readFiles(commandFile);
		
		//goes through command list one line at a time
		//each line should either be a link or a file name
		//making assumption that only lines with https://
		for(String line: audioFiles){
			if(line.contains("https://")){
				if(launchDebug == true)
		    		System.out.println("::Read link from command file,  " + line +" ::");
				
		       	audioOnLink(config, launchDebug, line, "audio", dir);
			}
			else{
				if(launchDebug == true)
		    		System.out.println("::Read file from command file,  " + line +" ::");
				fileName = line;
		       	audioOnDisk(config, launchDebug, dir);
			}
		}
	}
}
