package rmi;

import java.io.Serializable;

public class LocalFile implements Serializable{
	public String title;
	public RMIClientInterface user;
	public String path;
	public String tags;
	
	public LocalFile(String title, RMIClientInterface user, String path, String tags){
		this.title = title;
		this.user = user;
		this.path = path;
		this.tags = tags;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public void setUser(RMIClientInterface user){
		this.user = user;
	}
	
	public RMIClientInterface getUser(){
		return this.user;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public String getPath(){
		return this.path;
	}
	
	public void setTags(String tags){
		this.tags = tags;
	}
	
	public String getTags(){
		return this.tags;
	}
	
}
