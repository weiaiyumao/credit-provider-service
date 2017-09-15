package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Unicom;

public class CU130 extends Unicom implements Serializable{

	private static final long serialVersionUID = 119828393867864778L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CU130(String id){
		this.id = id;
	}

}
