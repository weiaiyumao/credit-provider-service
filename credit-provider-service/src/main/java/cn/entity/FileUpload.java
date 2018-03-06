package cn.entity;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;


/**
 * 文件上传信息存储类
 */
public class FileUpload implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userId; // 用户id

    private String fileName; // 备用生成检测结果包的时候采用这个名字

    private String fileRows; // 需要检测的总条数参考

    private Date createTime;

    private String isDeleted = "0";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileRows() {
        return fileRows;
    }

    public void setFileRows(String fileRows) {
        this.fileRows = fileRows;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }
}
