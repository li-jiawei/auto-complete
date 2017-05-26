package test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.log4j.BasicConfigurator;

import edu.neu.bigdata.LanguageModel;

public class Main {
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		Configuration conf = new Configuration();
		DBConfiguration.configureDB(conf, "com.mysql.jdbc.Driver", // driver
																	// class
				"jdbc:mysql://localhost:3306/test", // db url
				"root", // username
				"lijiawei"); // password

		Job job = Job.getInstance(conf);
		job.setJarByClass(Main.class);
		job.setMapperClass(LanguageModel.Map.class); // your mapper - not shown in this
										// example
		job.setReducerClass(LanguageModel.Reduce.class);
		job.setMapOutputKeyClass(Text.class); // your mapper - not shown in this
												// example
		job.setMapOutputValueClass(Text.class); // your mapper - not
														// shown in this example
		job.setOutputKeyClass(DBOutputWritable.class); // reducer's KEYOUT
		job.setOutputValueClass(NullWritable.class); // reducer's VALUEOUT
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(DBOutputFormat.class);

		FileInputFormat.addInputPaths(job, "/Users/Jiawei/Desktop/ngramlibrary");

		DBOutputFormat.setOutput(job, "output", // output table name
				new String[] { "starting_phrase", "following_word", "count" } // table columns
		);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}