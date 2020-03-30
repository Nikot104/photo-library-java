package com.example.photosandroid.model;


import java.util.ArrayList;

public class Album implements java.io.Serializable {
	public String albumName;
	public ArrayList<Photo> photos;

	public Album(String albName)
	{
		this.albumName = albName;
		this.photos = new ArrayList<Photo>();
	}

	public void reName(String albName)
	{
		this.albumName = albName;
		//We might need to redirect back to the user main screen to show changes
	}

	public void addPhoto(Photo p)
	{
		this.photos.add(p);
	}

	@Override
	public String toString() {
		return albumName;
	}
}


