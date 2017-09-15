package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Unicom;

public class CU131 extends Unicom implements Serializable{

	private static final long serialVersionUID = 6055813342461419588L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CU131(String id){
		this.id = id;
	}

}
