package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Mobile;

@Document(collection="KCM157")
public class KCM157 extends Mobile implements Serializable{

	private static final long serialVersionUID = -1818143997248498109L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}