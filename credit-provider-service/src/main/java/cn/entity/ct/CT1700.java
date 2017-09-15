package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Telecommunication;

public class CT1700 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = -3070807663599960782L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CT1700(String id){
		this.id = id;
	}

}
