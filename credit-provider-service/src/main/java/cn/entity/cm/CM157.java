package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM157 extends Mobile implements Serializable{

	private static final long serialVersionUID = -3967624310430483127L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM157(String id){
		this.id = id;
	}
}