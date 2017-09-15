package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import cn.entity.base.Mobile;

public class CM134 extends Mobile implements Serializable{

	private static final long serialVersionUID = 4499148705388006351L;
	
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CM134(String id){
		this.id = id;
	}
}