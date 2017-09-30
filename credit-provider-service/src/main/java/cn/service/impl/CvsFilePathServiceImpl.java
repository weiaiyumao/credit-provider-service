package cn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.entity.CvsFilePath;
import cn.repository.CvsFilePathReository;
import cn.service.CvsFilePathService;

/**
 * 
 * @author ChuangLan
 *
 */
@Service
public class CvsFilePathServiceImpl implements CvsFilePathService{

	@Autowired
	private CvsFilePathReository cvsFilePathReository;
	
	@Override
	public List<CvsFilePath> findByUserId(String userId) {
		return cvsFilePathReository.findByUserId(userId);
	}

	@Override
	public void deleteByIds(String ids) {

		String[] id = ids.split(",");
		
		for (String str : id) {
			cvsFilePathReository.delete(str);
		}
		
	}
	
	

}
