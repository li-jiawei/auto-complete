package edu.neu.bigdata;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.log4j.BasicConfigurator;

public class Driver {
	public static void main(String[] args)
			throws IllegalArgumentException, IOException, ClassNotFoundException, InterruptedException {
		 // job1
		 Configuration conf1 = new Configuration();
		
		 BasicConfigurator.configure();
		
		 conf1.set("textinputformat.record.delimiter", ".");
		 conf1.set("noGram", args[2]);
		 Job job1 = Job.getInstance(conf1);
		
		 job1.setJobName("NGram");
		 job1.setJarByClass(Driver.class);
		
		 job1.setMapperClass(NGramLibraryBuilder.NGramMapper.class);
		 job1.setReducerClass(NGramLibraryBuilder.NGramReducer.class);
		
		 job1.setOutputKeyClass(Text.class);
		 job1.setOutputValueClass(IntWritable.class);
		
		 job1.setInputFormatClass(TextInputFormat.class);
		 job1.setOutputFormatClass(TextOutputFormat.class);
		
		 TextInputFormat.setInputPaths(job1, new Path(args[0]));
		 TextOutputFormat.setOutputPath(job1, new Path(args[1]));
		 job1.waitForCompletion(true);

		
		
		
		// job2
		Configuration conf2 = new Configuration();
		 conf2.set("threshold", args[3]);
		 conf2.set("n", args[4]);

		DBConfiguration.configureDB(conf2, 
				"com.mysql.jdbc.Driver", 							// driver
				"jdbc:mysql://localhost:3306/test", 				// db url
				"root", 											// username
				"lijiawei"); 										// password

		Job job2 = Job.getInstance(conf2);
		job2.setJobName("Model");
		job2.setJarByClass(Driver.class);

//		job2.addArchiveToClassPath(new Path("/Users/Jiawei/app/mysql-connector-java-5.1.39/mysql-connector-java.jar"));
		job2.setMapOutputKeyClass(Text.class); 					// your mapper - not shown in this
		job2.setMapOutputValueClass(Text.class); 				// your mapper - not
		job2.setOutputKeyClass(DBOutputWritable.class); 		// reducer's KEYOUT
		job2.setOutputValueClass(NullWritable.class); 			// reducer's VALUEOUT
		job2.setInputFormatClass(TextInputFormat.class);
		job2.setOutputFormatClass(DBOutputFormat.class);
		
		
		job2.setMapperClass(LanguageModel.Map.class);
		job2.setReducerClass(LanguageModel.Reduce.class);


		FileInputFormat.setInputPaths(job2, args[1]);

		DBOutputFormat.setOutput(job2, "output", new String[] { "starting_phrase", "following_word", "count" });

		System.exit(job2.waitForCompletion(true) ? 0 : 1);
	}
}
