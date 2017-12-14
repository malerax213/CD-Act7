package rmi;

import java.io.Serializable;

public class LocalFile implements Serializable{
	public String id;
	public String title;
	public String user;
	public String server;
	public String tags;
	
	public LocalFile(){}
	
	public LocalFile(String title, String user,String server, String id, String tags){
		this.title = title;
		this.user = user;
		this.server = server;
		this.id = id;
		this.tags = tags;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public String getUser(){
		return this.user;
	}
	
	public void setServer(String server){
		this.server = server;
	}
	
	public String getServer(){
		return this.server;
	}
	
	public void setTags(String tags){
		this.tags = tags;
	}
	
	public String getTags(){
		return this.tags;
	}
	
	public String getJson(){
		return "{\"id\":\""+this.getId()+"\",\"title\":\""+this.getTitle()+"\", \"user\":\""+this.getUser()+"\", \"server\":\""+this.getServer()+"\", \"tags\":\""+this.getTags()+"\"}";
	}
	
}
