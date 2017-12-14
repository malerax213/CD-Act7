package rmi;

public class UserClass {
	public String name;
	public String pass;
	
	public UserClass(){}
	
	public UserClass(String name, String pass){
		this.name = name;
		this.pass = pass;
	}
	
	public void setTitle(String name){
		this.name = name;
	}
	
	public String getTitle(){
		return this.name;
	}
	
	public void setUser(String pass){
		this.pass = pass;
	}
	
	public String getUser(){
		return this.pass;
	}
	
	
}
