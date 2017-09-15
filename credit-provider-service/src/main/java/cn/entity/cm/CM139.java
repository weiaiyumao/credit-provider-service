package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM139 extends Mobile implements Serializable {

	private static final long serialVersionUID = -427536285076454703L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM139(String id){
		this.id = id;
	}
}