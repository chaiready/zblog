package com.min.zblog.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.zblog.core.dao.ArticleDao;
import com.min.zblog.core.dao.BlogQueryDsl;
import com.min.zblog.core.dao.ArticleTagDao;
import com.min.zblog.core.dao.VisitHstDao;
import com.min.zblog.core.service.ArticleService;
import com.min.zblog.data.entity.TmArticle;
import com.min.zblog.data.view.ArchiveInfo;
import com.min.zblog.data.view.ArticleInfo;
import com.min.zblog.data.view.BlogInfo;
import com.min.zblog.facility.enums.ArticleState;
import com.min.zblog.facility.enums.VisitType;

@Service
public class ArticleServiceImpl implements ArticleService {
	@Autowired
	private ArticleDao articleDao;
	@Autowired
	private VisitHstDao visitHstDao;
	@Autowired
	private ArticleTagDao articleTagDao;
	@Autowired
	private BlogQueryDsl blogQueryDsl;
	
	public List<TmArticle> listAll() {
		return articleDao.findAll();
	}
	
	public TmArticle findOne(Long id) {
		return articleDao.findOne(id);
	}
	
	@Transactional
	public void addArticle(TmArticle article) {
		articleDao.save(article);
	}
	
	public void deleteArticleById(Long id) {
		TmArticle article = articleDao.findOne(id);
		if(article == null){
			
		}
		
		//删除文章
		articleDao.delete(id);
		//删除访问历史
		blogQueryDsl.deleteVisitHstByArticleId(article.getId());
		//删除标签关联记录
		blogQueryDsl.deleteVisitHstByArticleId(article.getId());
		//删除评论
	}

	@Override
	public List<ArticleInfo> listArticleByCategoryName(String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		//根据分类名称，查找已经发布文章
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByCategoryName(name, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setId(article.getId());
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setTitle(article.getTitle());
			articleInfoList.add(articleInfo);
		}
		return articleInfoList;
	}

	@Override
	public List<ArticleInfo> listAllArticles() {
		//查找已经发布文章
		List<TmArticle> tmArticleList = blogQueryDsl.fetchAllArticles(ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setId(article.getId());
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setTitle(article.getTitle());
			articleInfoList.add(articleInfo);
		}

		return articleInfoList;
	}

	/* (non-Javadoc)
	 * @see com.min.zblog.core.service.ArticleService#listArticleByArchive(java.lang.String)
	 */
	@Override
	public List<ArticleInfo> listArticleByArchive(String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		//根据分类名称，查找已经发布文章
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByArchive(name, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setId(article.getId());
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setTitle(article.getTitle());
			articleInfoList.add(articleInfo);
		}
		return articleInfoList;
	}
	//fetchArticleByTag

	@Override
	public List<ArticleInfo> listArticleByTag(String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByTag(name, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setId(article.getId());
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setTitle(article.getTitle());
			articleInfoList.add(articleInfo);
		}
		return articleInfoList;
	}

	/* 查询已发布且阅读排行前5的文章
	 * @see com.min.zblog.core.service.ArticleService#listArticleByReadRank()
	 */
	@Override
	public List<ArticleInfo> listArticleByReadRank() {
		List<TmArticle> tmArticleList = blogQueryDsl.fetchTopArticleByHst(5, VisitType.READ, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
//			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
//			articleInfo.setCreateTime(article.getCreateTime());
//			articleInfo.setDescription(article.getDescription());
			articleInfo.setId(article.getId());
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setTitle(article.getTitle());
			articleInfoList.add(articleInfo);
		}
		return articleInfoList;
	}

	/* 获取博客统计信息
	 * @see com.min.zblog.core.service.ArticleService#obtainBlogInfo()
	 */
	@Override
	public BlogInfo obtainBlogInfo() {
		BlogInfo blogInfo = new BlogInfo();
		blogInfo.setTotalArticleNum(articleDao.countByState(ArticleState.PUBLISH));
		blogInfo.setTotalReadNum(blogQueryDsl.countVisitHstByType(VisitType.READ, ArticleState.PUBLISH));
		blogInfo.setTotalCommentNum((long)0);
		
		return blogInfo;
	}
}
