package com.example.photosandroid.model;

import com.example.photosandroid.model.*;

import java.util.ArrayList;

public class User implements java.io.Serializable{

	public ArrayList<Album> albums;

	public User()
	{
		this.albums = new ArrayList<Album>();
	}
}