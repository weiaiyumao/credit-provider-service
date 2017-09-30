package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Telecommunication;

@Document(collection="CT153")
public class CT153 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = 8084426468913036885L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CT153(String id){
		this.id = id;
	}

}
