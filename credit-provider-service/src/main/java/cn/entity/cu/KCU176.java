package cn.entity.cu;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.entity.base.Unicom;

@Document(collection="KCU176")
public class KCU176 extends Unicom implements Serializable{

	private static final long serialVersionUID = -7332880647964562893L;
	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
