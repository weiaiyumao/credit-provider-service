package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM159 extends Mobile implements Serializable{

	private static final long serialVersionUID = 2128725149933994991L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM159(String id){
		this.id = id;
	}
}