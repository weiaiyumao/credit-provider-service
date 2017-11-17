package cn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

	@Override
	public Page<CvsFilePath> getPageByUserId(int pageNo, int pageSize, String userId) {
		Sort sort = new Sort(Direction.DESC,"createTime");
		Pageable pageable = new PageRequest(pageNo - 1, pageSize, sort);
		Page<CvsFilePath> page = cvsFilePathReository.getPageByUserId(userId, pageable);
		return page;
	}
	
	

}
