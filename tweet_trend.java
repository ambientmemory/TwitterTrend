package tweet_trend;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
/**
 *
 * @author piyali
 */
public class tweet_trend {

	public static void main(String[] args) throws Exception {
		//create the new job
		Job job = Job.getInstance();
		job.setJarByClass(tweet_trend.class);	//set it to our name
		job.setJobName("tweet_trend");
//		job.setInputFormatClass(NLineInputFormat.class);
//		NLineInputFormat.addInputPath(job, new Path("/data/tweet_data.txt"));
//      job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap", 5);

		//set input and output path
//		LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
		FileInputFormat.addInputPath(job, new Path("s3://piubucket/data/tweet_data.txt"));
		FileOutputFormat.setOutputPath(job, new Path("s3://piubucket/output"));
		//define the mapper and the reducer classes
		job.setMapperClass(map.class);
		//job.setCombinerClass(reduce.class); //our combiner - local reducer - is identical
		job.setReducerClass(reduce.class);
		//define the output types
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		//wait till it happens
        //job.setNumReduceTasks(0);
		//
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}// end of main
} //end of class
