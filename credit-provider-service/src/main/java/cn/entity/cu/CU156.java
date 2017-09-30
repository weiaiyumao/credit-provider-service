package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Unicom;

@Document(collection="CU156")
public class CU156 extends Unicom implements Serializable{

	private static final long serialVersionUID = -1975712731833992197L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CU156(String id){
		this.id = id;
	}
}
