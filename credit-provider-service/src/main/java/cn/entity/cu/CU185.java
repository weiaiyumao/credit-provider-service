package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Unicom;

@Document(collection="CU185")
public class CU185 extends Unicom implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5439335357897867186L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CU185(String id){
		this.id = id;
	}
}
