package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Unicom;

public class CU176 extends Unicom implements Serializable{

	private static final long serialVersionUID = -8574682553029659903L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CU176(String id){
		this.id = id;
	}
}
