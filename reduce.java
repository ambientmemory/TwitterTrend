package tweet_trend;

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 *
 * @author piyali mukherjee pm2678@columbia.edu
 * This program receives the key (which is a word) and its frequencies from each
 * distributed processing source
 * 
 */
public class reduce extends Reducer<Text, IntWritable, Text, IntWritable> {

	@Override
	public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
		
		//Next we analyze each <key, IntWritable value array> to analyze the maxima
		//corresponding to this key value
		int max_for_the_key = Integer.MIN_VALUE;
		for (IntWritable value : values) {
			max_for_the_key = Math.max(max_for_the_key, value.get());
		}
		context.write(key, new IntWritable(max_for_the_key));
	} //end of constructor
} //end of class reduce
