import java.io.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;

public class HadoopSort extends Configured implements Tool {

	public static class SortMapper extends Mapper<Object, Text, Text, Text> {

		public Text token1 = new Text();
		public Text token2 = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String val = value.toString();
			token1.set(val.substring(0, 10));
			token2.set(val.substring(10));
			context.write(token1, token2);
		}
	}

	public static class SortReducer extends Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text reducer_key, Iterable<Text> reducer_value, Context context)
				throws IOException, InterruptedException {
			for (Text values : reducer_value) {
				context.write(reducer_key, values);
			}
		}
	}

	public static class SortPartitioner extends Partitioner<Text, Text> {
		@Override
		public int getPartition(Text key, Text value, int numReduceTasks) {
			int partition = 0;
			int start = 32, end = 0;

			int first = key.toString().charAt(0);
			int partitionSize = 94 / numReduceTasks;
			
			for (int i = 0; i < numReduceTasks; i++) {
				end = start + partitionSize;
				if (first >= start && first <= end) {
					partition = i;
					break;
				}
				start = end + 1;
			}
			return partition;
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		System.out.println("Hadoop Sort Program Started");
		
		long start = System.currentTimeMillis();
		
		Configuration config = getConf();
		config.set("mapred.textoutputformat.separator", " ");// add single space between key and value

		Job job = Job.getInstance(config, "Hadoop Sort");
		job.setJarByClass(HadoopSort.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		int num = Integer.parseInt(args[2]);
		
		job.setMapperClass(SortMapper.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setPartitionerClass(SortPartitioner.class);
		job.setNumReduceTasks(num);
		job.setReducerClass(SortReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.waitForCompletion(true);
		
		long end = System.currentTimeMillis();
        double time = end-start;
        System.out.println("Time taken to sort: " + time+" milliseconds.");
        return 0;
	}

	public static void main(String ar[]) throws Exception {
		int res = ToolRunner.run(new Configuration(), new HadoopSort(), ar);
		System.exit(res);
	}
}
