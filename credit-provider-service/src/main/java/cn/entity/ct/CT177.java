package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Telecommunication;

@Document(collection="CT177")
public class CT177 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = -1287515008144039975L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CT177(String id){
		this.id = id;
	}
}
