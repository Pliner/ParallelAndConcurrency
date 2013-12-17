import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class TwitterSplit extends Configured implements Tool
{

    public static class Mapper extends org.apache.hadoop.mapreduce.Mapper<Text, IntWritable, Text, IntWritable> {

        private static class Range {
            public final int from;
            public final int to;

            public boolean contains(int number) {
                return number >= from && number <= to;
            }

            public Range(int from, int to) {
                this.from = from;
                this.to = to;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Range range = (Range) o;

                if (from != range.from) return false;
                if (to != range.to) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = from;
                result = 31 * result + to;
                return result;
            }

            @Override
            public String toString() {
                return "["+ from + ", " + to + "]";
            }
        }

        private HashMap<Range, Integer> rangesCount = new HashMap<Range, Integer>();

        private Text key = new Text();
        private IntWritable value = new IntWritable();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            rangesCount.put(new Range(1, 10), 0);
            rangesCount.put(new Range(11, 100), 0);
            rangesCount.put(new Range(101, 1000), 0);
            rangesCount.put(new Range(1001, 10000), 0);
            rangesCount.put(new Range(10001, 100000), 0);
            rangesCount.put(new Range(100001, 1000000), 0);
            rangesCount.put(new Range(1000001, 10000000), 0);
            rangesCount.put(new Range(10000001, 100000000), 0);
            rangesCount.put(new Range(100000001, 1000000000), 0);
            rangesCount.put(new Range(1000000001, Integer.MAX_VALUE), 0);
        }

        public void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {
            int number = value.get();
            boolean rangeFound= false;
            for(Range range : rangesCount.keySet()) {
                if(range.contains(number)) {
                    int count = rangesCount.get(range);
                    rangesCount.put(range, count + 1);
                    rangeFound = true;
                    break;
                }
            }
            if(!rangeFound)
                throw new IllegalStateException("Number " + number + " is not matches with interval");
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            for (Map.Entry<Range, Integer> entry : rangesCount.entrySet()) {
                key.set(entry.getKey().toString());
                value.set(entry.getValue());
                context.write(key, value);
            }
            super.cleanup(context);
        }
    }

    public static class Reducer extends org.apache.hadoop.mapreduce.Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable value = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            value.set(sum);
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
        Job job = new Job(conf, "TwitterSplit");

        job.setInputFormatClass(SequenceFileInputFormat.class);

        // Setting Output Key Types
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // Setting Mapper Class
        job.setMapperClass(Mapper.class);

        // Setting Reducer Class
        job.setReducerClass(Reducer.class);

        // Setting Output Format
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setJarByClass(TwitterSplit.class);
        job.setNumReduceTasks(1);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new TwitterSplit(), args);
        System.exit(exitCode);
    }
}
