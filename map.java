package tweet_trend;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 *
 * @author piyali mukherjee pm2678@columbia.edu This program reads the local tweet_data file, and then proceeds to build an "DICTIONARY" of all the words in it,
 * with their frequencies. Then it opens the list of common English words, and eliminates those. The final list the top 20 are fed into the reduce program It
 * also ensures that the returned count does not exceed 20
 */
public class map extends Mapper<LongWritable, Text, Text, IntWritable> {
	/*
	 key : We will not use the key valye - so we leave it as the index of the twet being processed
	 value : the tweet text
	 output key : a word used in the tweet
	 output value : frequency of the word
	 */
	/*
	 We keep the common word Set as a static data, so that we dont have to build it all over again
	 */
	Set<String> common_words = new HashSet<>();	//Set to store all commonly used words in English
	private static int max_records = 200;		//we do not send more than 20 records

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		//We first check if the common_words Set has been built. This is required only once
		if (common_words.isEmpty()) {
			String a_line;
			String uri = "s3://piubucket/data/commonwords.txt";
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			InputStream in = null;
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				in = fs.open(new Path(uri));
				IOUtils.copyBytes(in, baos, 4096); //read 4096 bytes
				String the_file = baos.toString();
				//Now we break down the read in string into tokens
				String[]tokens = the_file.split("\n");
				for (int i = 0; i < tokens.length; i++) {
					String this_token = tokens[i].trim().substring(3);
					if (!(common_words.contains(this_token))) {
						common_words.add(this_token);
					}
				}
			} finally {
				IOUtils.closeStream(in);
			}
		} //end of creating has map	
		System.out.println("Size of common words : "+common_words.size());
		//Now we process the input
		String line = value.toString();	//convert the input line into the string
		System.out.println("Line length : "+line.length());
		String[] words = line.split("\\s+");	//split along ALL whitespaces
		Map<String, Integer> hgram = new HashMap<>();	//create a collection of key value pairs to store the token frequency
		//Now we do a check if the words of the line given match common words, or 
		//contains merely a punctuation or a non-cpitalized word of length less than 3
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() < 3) {	//too small a word
				continue;
			}
			if (!(words[i].matches("\\A\\p{ASCII}*\\z"))) {
				continue;		//ignore if contains non-ascii characters
			}			//we delete the trailing punctuations
			char[] test_word = (words[i].trim()).toCharArray();
			int test_word_len = test_word.length;
			do {
				if ((test_word[test_word_len - 1] >= 'a') && (test_word[test_word_len - 1] <= 'z')) {
					break;
				}
				if ((test_word[test_word_len - 1] >= 'A') && (test_word[test_word_len - 1] <= 'Z')) {
					break;
				}
				test_word[test_word_len - 1] = ' ';
				test_word_len--;
			} while (test_word_len > 0);
			//next we drop the words that contain intermdiate punctuation
			boolean is_a_word = true;
			for (int j = 0; j < test_word.length; j++) {
				if (test_word[j] == ' ') {
					continue;
				}
				if ((test_word[j] >= 'a') && (test_word[j] <= 'z')) {
					continue;
				}
				if ((test_word[j] >= 'A') && (test_word[j] <= 'Z')) {
					continue;
				}
				is_a_word = false;
				break;
			}
			if (!is_a_word) {
				continue;	//go to next word
			}
			String the_full_word = new String(test_word);
			String the_word = the_full_word.trim();
			if (the_word.startsWith("http")) {
				continue;	//we skip the extension http addresses
			}
			if (common_words.contains(the_word)) {		//very common word
				continue;
			}
			//we can add more filtering criteria here...
			//Next we check if the word is already in hgram. If it is, then we just increment, else we add
			
			//System.out.print("Analyzing word : "+words[i]+" : "+the_word+" : ");
			if (hgram.containsKey(the_word)) {
				int val = hgram.get(the_word) + 1;
				hgram.put(the_word, val);
				//System.out.println(val);
			} else {
				hgram.put(the_word, 1);
				//System.out.println(1);
			}
		} //end of all words... 
		//Now we publish the histogram to the reduce
		int max_rec = Math.min(hgram.size(), max_records);	//we do not send more than max_records
		Iterator it = hgram.entrySet().iterator();
		while ((it.hasNext()) && (max_rec > 0)) {
			Map.Entry a_hist = (Map.Entry<String, Integer>) it.next();
			context.write(new Text(a_hist.getKey().toString()), new IntWritable((int) a_hist.getValue()));
			max_rec--;
		} //end of while
	} //end of constructor map()
} //end of class map

