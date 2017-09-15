package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM152 extends Mobile implements Serializable{

	private static final long serialVersionUID = 7340968158042879946L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM152(String id){
		this.id = id;
	}
}