package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Unicom;

public class CU132 extends Unicom implements Serializable{

	private static final long serialVersionUID = 4456255845523674685L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CU132(String id){
		this.id = id;
	}

}
