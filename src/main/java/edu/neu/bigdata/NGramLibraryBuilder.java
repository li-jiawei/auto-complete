package edu.neu.bigdata;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class NGramLibraryBuilder {

	public static class NGramMapper extends Mapper<Object, Text, Text, IntWritable> {

		int noGram;

		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			noGram = conf.getInt("noGram", 5);
		}
		
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString().trim().toLowerCase().replaceAll("[^a-z]", " ");
		
			String[] words = line.split("\\s+");
			
			if (words.length < 2) {
				return;
			}
			
			StringBuilder sb = null;
			for (int i = 0; i < words.length - 1; i++) {
				sb = new StringBuilder();
				for(int j = 0; i + j < words.length && j < noGram; j++) {
					sb.append(" ");
					sb.append(words[i + j]);
					
					// (I love	1)
					context.write(new Text(sb.toString().trim()), new IntWritable(1));
				}
			}
		}
	}

	public static class NGramReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			//(I love	5)
			context.write(key, result);
		}

	}
}
