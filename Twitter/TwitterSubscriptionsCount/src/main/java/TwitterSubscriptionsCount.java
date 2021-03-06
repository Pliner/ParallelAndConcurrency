import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TwitterSubscriptionsCount extends Configured implements Tool
{
    public static class CountMapper extends Mapper<Text, Text, Text, IntWritable> {
        private final HashMap<String, Integer> userSubscriptions = new HashMap<String, Integer>();

        private IntWritable count = new IntWritable();
        private Text user = new Text();

        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            String user = key.toString();

            if(! userSubscriptions.containsKey(user))
                userSubscriptions.put(user, 1);
            else {
                int count = userSubscriptions.get(user);
                userSubscriptions.put(user, count + 1);
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for(Map.Entry<String, Integer> entry : userSubscriptions.entrySet())
            {
                user.set(entry.getKey());
                count.set(entry.getValue());
                context.write(user, count);
            }
            super.cleanup(context);
        }
    }

    public static class CountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
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
        conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
        Job job = new Job(conf, "TwitterSubscriptionsCount");

        job.setInputFormatClass(KeyValueTextInputFormat.class);

        // Setting Output Key Types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Setting Mapper Class
        job.setMapperClass(CountMapper.class);

        // Setting Reducer Class
        job.setReducerClass(CountReducer.class);

        // Setting Output Format
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setJarByClass(TwitterSubscriptionsCount.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new TwitterSubscriptionsCount(), args);
        System.exit(exitCode);
    }
}
