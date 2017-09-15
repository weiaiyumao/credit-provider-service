package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Telecommunication;

public class CT180 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = 7683719400623794462L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CT180(String id){
		this.id = id;
	}
}
