package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Mobile;

@Document(collection="KCM151")
public class KCM151 extends Mobile implements Serializable{

	private static final long serialVersionUID = 2544006185393115026L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}