package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Telecommunication;

public class CT133 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = -1808109958481406264L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CT133(String id){
		this.id = id;
	}
}
