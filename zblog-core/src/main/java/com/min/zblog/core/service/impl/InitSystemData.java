package com.min.zblog.core.service.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.min.zblog.api.rpc.ArchiveService;
import com.min.zblog.api.rpc.ArticleService;
import com.min.zblog.api.rpc.CategoryService;
import com.min.zblog.api.rpc.TagService;

/**
 * 初始化博客系统统计数据
 * @author zhouzm
 *
 */
@Component
public class InitSystemData implements ApplicationListener<ContextRefreshedEvent>{
	
	@Autowired
	private ArticleService articleService;
	
	@Autowired
	private ArchiveService archiveService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private TagService tagService;
	
	private volatile AtomicBoolean initFlag = new AtomicBoolean(false);
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//已初始化，则返回
		if(!initFlag.compareAndSet(false, true)) {
			return;
		}
		
		//初始化数据
		initData();
	}
	
	private void initData() {
		//初始化博客统计信息
		articleService.initBlogInfo();
		//初始化分类信息
		categoryService.initCategoryInfo();
		//初始化归档信息
		archiveService.initArchiveInfo();
		//初始化标签信息
		tagService.initTagInfo();
		//初始化阅读排行信息
		articleService.initArticleReadRank();
	}
}
