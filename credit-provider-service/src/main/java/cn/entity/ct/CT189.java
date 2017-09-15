package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Telecommunication;

public class CT189 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = -1874942029868759249L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CT189(String id){
		this.id = id;
	}
}
