import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class CDemo extends Configured implements Tool {

	public enum RecordType {
		Good, Bad
	}

	public static class CMapper extends
			Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws java.io.IOException, InterruptedException {
			if (value.toString() != "" && value.toString().contains(",")) {

				String[] array = value.toString().split(",");

				if (array.length < 2) {
					context.getCounter(RecordType.Bad).increment(1);
				} else {
					if (array[1].isEmpty()) {

						context.getCounter(RecordType.Bad).increment(1);
					} else {
						context.getCounter(RecordType.Good).increment(1);
						context.write(new Text(array[1]), new IntWritable(1));
					}
				}

			}

		};
	}

	public static class CReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws java.io.IOException,
				InterruptedException {
			int count = 0;

			for (IntWritable x : values) {
				int val = Integer.parseInt(x.toString());

				count += val;

			}
			context.write(key, new IntWritable(count));
		};
	}

	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf();

		Job job = new Job(conf, "MI Demo");

		job.setJarByClass(CDemo.class);

		job.setMapperClass(CMapper.class);

		job.setReducerClass(CReducer.class);
		// job.setNumReduceTasks(0);

		job.setInputFormatClass(TextInputFormat.class);

		job.setOutputFormatClass(TextOutputFormat.class);

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(IntWritable.class);

		Path in = new Path(args[0]);

		Path out = new Path(args[1]);

		FileInputFormat.addInputPath(job, in);

		FileOutputFormat.setOutputPath(job, out);

		System.exit(job.waitForCompletion(true) ? 0 : 1);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int res = ToolRunner.run(new Configuration(), new CDemo(), args);
		System.exit(res);
	}

}
