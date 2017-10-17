package cn.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 绝对空号
 * @author ChuangLan
 *
 */
@Document(collection="SpaceMobile")
public class SpaceMobile implements Serializable {

	private static final long serialVersionUID = -518754662402220872L;
	
	@Id
	private String id;
	
	@Indexed(name = "{'mobile_': 1}")
	private String mobile; // 手机号码

}
