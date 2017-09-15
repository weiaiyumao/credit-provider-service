package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM138 extends Mobile implements Serializable {

	private static final long serialVersionUID = 8525694734754176557L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM138(String id){
		this.id = id;
	}
}