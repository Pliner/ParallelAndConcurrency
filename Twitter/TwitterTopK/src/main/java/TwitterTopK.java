import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.TreeSet;

public class TwitterTopK extends Configured implements Tool
{
    public static class TwitterUser implements Comparable<TwitterUser>{
        public final String userId;
        public final int subscriptionCount;


        private TwitterUser(String userId, int subscriptionCount) {
            this.userId = userId;
            this.subscriptionCount = subscriptionCount;
        }

        @Override
        public int compareTo(TwitterUser o) {
            if( subscriptionCount < o.subscriptionCount )
                return -1;
            else if(subscriptionCount > o.subscriptionCount)
                return 1;
            return userId.compareTo(o.userId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TwitterUser that = (TwitterUser) o;

            if (subscriptionCount != that.subscriptionCount) return false;
            if (!userId.equals(that.userId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = userId.hashCode();
            result = 31 * result + (subscriptionCount ^ (subscriptionCount >>> 32));
            return result;
        }
    }


    public static class Mapper extends org.apache.hadoop.mapreduce.Mapper<Text, IntWritable, Text, IntWritable> {


        private TreeSet<TwitterUser> topCache = new TreeSet<TwitterUser>();

        private Text key = new Text();
        private IntWritable value = new IntWritable();

        public void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {
           topCache.add(new TwitterUser(key.toString(), value.get()));
           if(topCache.size() > 50)
               topCache.pollFirst();
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (TwitterUser twitterUser : topCache) {
                key.set(twitterUser.userId);
                value.set(twitterUser.subscriptionCount);
                context.write(key, value);
            }
            super.cleanup(context);
        }
    }

    public static class Reducer extends org.apache.hadoop.mapreduce.Reducer<Text, IntWritable, Text, NullWritable> {

        private TreeSet<TwitterUser> topCache = new TreeSet<TwitterUser>();
        private Text key = new Text();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            topCache.add(new TwitterUser(key.toString(), sum));
            if(topCache.size() > 50)
                topCache.pollFirst();
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (TwitterUser twitterUser : topCache) {
                key.set(twitterUser.userId);
                context.write(key, NullWritable.get());
            }
            super.cleanup(context);
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
        Job job = new Job(conf, "TwitterTopK");

        job.setInputFormatClass(SequenceFileInputFormat.class);

        // Setting Output Key Types
        job.setOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputValueClass(NullWritable.class);

        // Setting Mapper Class
        job.setMapperClass(Mapper.class);

        // Setting Reducer Class
        job.setReducerClass(Reducer.class);

        // Setting Output Format
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setJarByClass(TwitterTopK.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new TwitterTopK(), args);
        System.exit(exitCode);
    }
}
