package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM184 extends Mobile implements Serializable{

	private static final long serialVersionUID = 2611834216260154651L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM184(String id){
		this.id = id;
	}
}