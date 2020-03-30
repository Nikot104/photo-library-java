package com.example.photosandroid.model;

import java.util.ArrayList;

public class Photo implements java.io.Serializable {
	public String itemPath;
	public String caption;
	public ArrayList<String> tags;

	public Photo(String itemPath, String caption)
	{
		this.itemPath = itemPath;
		this.caption = caption;
		this.tags = new ArrayList<String>();
	}

	public void reCap(String newCap)
	{
		this.caption = newCap;
	}

	public void addTag(String tagType, String tagValue)
	{
		if(tags.contains((tagType + "   " + tagValue)))
		{
			return;
		}

		tags.add((tagType + ":  " + tagValue));
	}


}