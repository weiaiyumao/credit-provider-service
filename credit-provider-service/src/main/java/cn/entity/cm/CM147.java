package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM147 extends Mobile implements Serializable {

	private static final long serialVersionUID = -8792558704370328635L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM147(String id){
		this.id = id;
	}
}