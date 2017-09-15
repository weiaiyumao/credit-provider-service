package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Unicom;

public class CU145 extends Unicom implements Serializable{

	private static final long serialVersionUID = 917597170200932260L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CU145(String id){
		this.id = id;
	}
}
