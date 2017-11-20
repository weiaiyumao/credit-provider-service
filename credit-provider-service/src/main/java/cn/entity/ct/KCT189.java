package cn.entity.ct;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Telecommunication;

@Document(collection="KCT189")
public class KCT189 extends Telecommunication implements Serializable{

	private static final long serialVersionUID = 5667305706750121621L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
