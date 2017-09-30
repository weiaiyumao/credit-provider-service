package cn.entity.cm;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Mobile;

@Document(collection="CM1705")
public class CM1705 extends Mobile implements Serializable{

	private static final long serialVersionUID = -831206559021873482L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public CM1705(String id){
		this.id = id;
	}
}