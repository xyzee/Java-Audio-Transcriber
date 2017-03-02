# Java-Audio-Transcriber
Transcribing and converting audio from a URL to a local text file in Java using, Jsoup, ffmpeg, and CMU Sphinx API

### What does this project do? ###

This program loads audio files from disk or from a link using Jsoup API, convert them to the proper .wav format via command line using ffmpeg windows 64 bit version, translates them using the CMU Sphinx API in a text document to view. This project is run on the console and provides test cases to run and debug options as well to the user in case issues arise. It is highly recommended that user input is correct as not every feasible exception is being caught in this program. (NOTE test cases can take a long time as one test downloads, converts, and translates an entire lecture via link).

This project was made in Java v1.8 using the Eclipse IDE as a gradle project on Windows 7 64 bit.
### How to make/set-up project ###

IMPORTANT: If you are not using a windows 64 bit machine this program will not work as it uses the windows 64 release of ffmpeg. You'll need to download the appropriate version off of https://www.ffmpeg.org/, put the executable in the ffmpeg/bin folder, and try to rename it to ffmpeg.exe if you can. if you can't you'll need to rename the command line prompt in my code at lines 18 in class transcribeInfo and line 147 in class multipleTranscriptInfo (there's also debug output that outputs the command you use, probably a good idea to rename them too).

### Citations (also listed in code) ###

readFile function code provided from http://alvinalexander.com/blog/post/java/how-open-read-file-java-string-array-list
code to prevent hanging: http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
transcribing code provided from CMU Sphinx's website http://cmusphinx.sourceforge.net/wiki/tutorialsphinx4
