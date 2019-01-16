package com.min.zblog.admin.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.min.zblog.core.service.ArchiveService;
import com.min.zblog.api.rpc.CategoryService;
import com.min.zblog.data.entity.TmArticle;
import com.min.zblog.data.entity.TmCategory;
import com.min.zblog.data.view.ArchiveInfo;
import com.min.zblog.data.view.ArticleInfo;
import com.min.zblog.data.view.CategoryInfo;
import com.min.zblog.data.view.PageInfo;
import com.min.zblog.facility.enums.ArticleState;
import com.min.zblog.facility.enums.Indicator;
import com.min.zblog.facility.utils.Constants;

@Controller
@RequestMapping("/archive")
public class ArchiveController {
	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	ArchiveService archiveService;
	
	@RequestMapping("/queryPage")
    public ModelAndView queryPage(){
    	ModelAndView modelAndView =new ModelAndView("queryArchive");
    	
        return modelAndView;
    }
	
	@ResponseBody
	@RequestMapping("/query")
    public Map<String, Object> query(@RequestParam(value="page")Integer page, 
    		@RequestParam(value="limit")Integer limit,
    		@RequestParam(value="startDate",required=false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, 
    		@RequestParam(value="endDate",required=false) @DateTimeFormat(pattern = "yyyy-MM-dd")Date endDate){
		Map<String, Object> reqMap = new HashMap<String, Object>();
    	reqMap.put(Constants.START_DATE, startDate);
    	reqMap.put(Constants.END_DATE, endDate);
    	
    	PageInfo<ArchiveInfo> pageInfo = archiveService.queryArchiveByPage(limit, page, reqMap);
    	logger.debug("currentPage:"+pageInfo.getCurrentPage()+",totalPages:"+pageInfo.getTotalPages());
    	
    	Map<String, Object> map = new HashMap<String, Object>();
        map.put("code", 0);
        map.put("count", pageInfo.getCount());
        map.put("data", pageInfo.getList());
        map.put("msg", "加载成功");
        return map;
    }
	
	@ResponseBody
    @RequestMapping("/delete")
    public Map<String, Object> deleteCategory(
    		@RequestParam(value="archiveId") Long archiveId){
    	Map<String, Object> result = new HashMap<String, Object>();
		try{
			archiveService.deleteArchiveById(archiveId);
			//返回json格式结果
        	result.put("success", true);
        	result.put("message", "");
		}catch(Exception e){
			//返回json格式结果
        	result.put("success", false);
        	result.put("message", e.getMessage());
		}
    	
        return result;
    }
}
