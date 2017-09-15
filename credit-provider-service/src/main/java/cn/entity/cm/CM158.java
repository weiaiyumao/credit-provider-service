package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM158 extends Mobile implements Serializable{

	private static final long serialVersionUID = -6424576041786604211L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM158(String id){
		this.id = id;
	}
}