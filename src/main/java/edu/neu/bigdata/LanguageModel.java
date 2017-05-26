package edu.neu.bigdata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class LanguageModel {

	public static class Map extends Mapper<Object, Text, Text, Text> {

		// if the count of a some-gram is less than threshold, we just throw it away
		int threshold;

		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			threshold = conf.getInt("threshold", 1);
		}
		
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			if (value == null || value.toString().trim().length() == 0) {
				return;
			}
			
			String line = value.toString().trim();
			
			// split phrase and count
			String[] wordsPlusCount = line.split("\t");

			if (wordsPlusCount.length < 2) {
				return;
			}
			
			String[] words = wordsPlusCount[0].split("\\s+");
//			int count = Integer.valueOf(wordsPlusCount[1]); 
			int count = Integer.valueOf(wordsPlusCount[1]);
			
			// if line is null or empty, or incomplete, or count less than threshold
			if (count < threshold) {
				return;
			}
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < words.length - 1; i++) {
				sb.append(words[i]).append(" ");
			}
			String outputKey = sb.toString().trim();
			String outputValue = words[words.length - 1];
			
			if (outputKey != null && outputKey.length() >= 1) {
				context.write(new Text(outputKey), new Text(outputValue + "=" + count));
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, DBOutputWritable, NullWritable> {
		// a starting phrase can have many following words,
		// n is to limit the number of these following words which would then get stored in database.
		private int n;
		
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			n = conf.getInt("n", 5);
		}

		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			TreeMap<Integer, List<String>> tm = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
			for (Text val: values) {
				String curValue = val.toString().trim();
				String[] wordPlusCount = curValue.split("=");
				int count = Integer.parseInt(wordPlusCount[1].trim());
				String word = wordPlusCount[0].trim();
				if (!tm.containsKey(count)) {
					tm.put(count, new ArrayList<String>());
				}
				tm.get(count).add(word);
			}
			Iterator<Integer> iter = tm.keySet().iterator();
			for(int j = 0; iter.hasNext() && j < n; j++) {
				int keyCount = iter.next();
				List<String> words = tm.get(keyCount);
				for (String curWord: words) {
					context.write(new DBOutputWritable(key.toString(), curWord, keyCount), NullWritable.get());
					j++;
				}
			}
		}

	}
}
