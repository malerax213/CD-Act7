package rmi;

public class ServerClass {
	public String name;
	public String ip;
	public String port;
	
	public ServerClass(){}
	
	public ServerClass(String name, String ip, String port){
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public void setPort(String port){
		this.port = port;
	}
	
	public String getPort(){
		return this.port;
	}
	
}
