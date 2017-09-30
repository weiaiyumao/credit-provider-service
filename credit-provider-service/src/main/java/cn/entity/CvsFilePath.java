package cn.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

/**
 * 检测结果 下载地址类
 * @author ChuangLan
 *
 */
public class CvsFilePath implements Serializable{

	private static final long serialVersionUID = -8820457928658267590L;

	@Id
	private String id;
	
	private String thereFilePath;
	
	private String thereFileSize;
	
	private String thereCount;
	
	private String sixFilePath;
	
	private String sixFileSize;
	
	private String sixCount;
	
	private String unknownFilePath; 
	
	private String unknownFileSize;
	
	private String unknownSize;
	
	private String zipName;
	
	private String zipPath;
	
	private String zipSize;
	
	private String userId;
	
	private Date createTime;
	
	private String isDeleted = "0";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getThereFilePath() {
		return thereFilePath;
	}

	public void setThereFilePath(String thereFilePath) {
		this.thereFilePath = thereFilePath;
	}

	public String getSixFilePath() {
		return sixFilePath;
	}

	public void setSixFilePath(String sixFilePath) {
		this.sixFilePath = sixFilePath;
	}

	public String getUnknownFilePath() {
		return unknownFilePath;
	}

	public void setUnknownFilePath(String unknownFilePath) {
		this.unknownFilePath = unknownFilePath;
	}
	
	public String getZipPath() {
		return zipPath;
	}

	public void setZipPath(String zipPath) {
		this.zipPath = zipPath;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getZipSize() {
		return zipSize;
	}

	public void setZipSize(String zipSize) {
		this.zipSize = zipSize;
	}

	public String getThereFileSize() {
		return thereFileSize;
	}

	public void setThereFileSize(String thereFileSize) {
		this.thereFileSize = thereFileSize;
	}

	public String getSixFileSize() {
		return sixFileSize;
	}

	public void setSixFileSize(String sixFileSize) {
		this.sixFileSize = sixFileSize;
	}

	public String getUnknownFileSize() {
		return unknownFileSize;
	}

	public void setUnknownFileSize(String unknownFileSize) {
		this.unknownFileSize = unknownFileSize;
	}

	public String getZipName() {
		return zipName;
	}

	public void setZipName(String zipName) {
		this.zipName = zipName;
	}

	public String getThereCount() {
		return thereCount;
	}

	public void setThereCount(String thereCount) {
		this.thereCount = thereCount;
	}

	public String getSixCount() {
		return sixCount;
	}

	public void setSixCount(String sixCount) {
		this.sixCount = sixCount;
	}

	public String getUnknownSize() {
		return unknownSize;
	}

	public void setUnknownSize(String unknownSize) {
		this.unknownSize = unknownSize;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	
	
}
