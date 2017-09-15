package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM137 extends Mobile implements Serializable {

	private static final long serialVersionUID = -8622437492175141649L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM137(String id){
		this.id = id;
	}
}