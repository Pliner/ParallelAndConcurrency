import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class TwitterAverageSubscriptionsCount extends Configured implements Tool
{

    public static class Mapper extends org.apache.hadoop.mapreduce.Mapper<Text, IntWritable, Text, IntWritable> {
        private Text dummyKey = new Text();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
        }

        public void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {
            dummyKey.set("subscriptions");
            context.write(dummyKey, value);
        }
    }

    public static class Reducer extends org.apache.hadoop.mapreduce.Reducer<Text, IntWritable, Text, DoubleWritable> {
        private DoubleWritable value = new DoubleWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            double sum = 0;
            int count = 0;
            for (IntWritable val : values) {
                sum += val.get();
                count++;
            }
            value.set(sum/count);
            context.write(key, value);
        }

    }

    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.printf("Usage: %s [generic options] <input> <output>\n",
                    getClass().getSimpleName());
            ToolRunner.printGenericCommandUsage(System.err);
            return -1;
        }

        Configuration conf = getConf();
        //conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
        Job job = new Job(conf, "TwitterAverageSubscriptionsCount");

        job.setInputFormatClass(SequenceFileInputFormat.class);

        // Setting Output Key Types
        job.setOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputValueClass(DoubleWritable.class);

        // Setting Mapper Class
        job.setMapperClass(Mapper.class);

        // Setting Reducer Class
        job.setReducerClass(Reducer.class);

        // Setting Output Format
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setJarByClass(TwitterAverageSubscriptionsCount.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new TwitterAverageSubscriptionsCount(), args);
        System.exit(exitCode);
    }
}
