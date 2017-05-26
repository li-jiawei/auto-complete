package edu.neu.bigdata;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

public class DBOutputWritable implements Writable, DBWritable {

	private String starting_phrase;
	private String following_word;
	private int count;
	
	
	public DBOutputWritable(String starting_phrase, String following_word, int count) {
		this.starting_phrase = starting_phrase;
		this.following_word = following_word;
		this.count = count;
	}
	
	
	public void readFields(ResultSet input) throws SQLException {
		starting_phrase = input.getString(1);
		following_word = input.getString(2);
		count = input.getInt(3);
	}

	public void write(PreparedStatement output) throws SQLException {
		output.setString(1, starting_phrase);
		output.setString(2, following_word);
		output.setInt(3, count);
	}


	public void write(DataOutput out) throws IOException {
		
	}


	public void readFields(DataInput in) throws IOException {
		
	}

}
