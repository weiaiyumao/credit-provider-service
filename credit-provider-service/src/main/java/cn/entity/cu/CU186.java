package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Unicom;

@Document(collection="CU186")
public class CU186 extends Unicom implements Serializable{

	private static final long serialVersionUID = -7699250872726769074L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CU186(String id){
		this.id = id;
	}
}
