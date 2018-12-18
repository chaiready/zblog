package com.min.zblog.core.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.zblog.core.dao.ArchiveDao;
import com.min.zblog.core.dao.ArticleDao;
import com.min.zblog.core.dao.BlogQueryDsl;
import com.min.zblog.core.dao.ArticleTagDao;
import com.min.zblog.core.dao.CategoryDao;
import com.min.zblog.core.dao.CommentDao;
import com.min.zblog.core.dao.TagDao;
import com.min.zblog.core.dao.VisitHstDao;
import com.min.zblog.core.facility.GlobalContextHolder;
import com.min.zblog.core.service.ArticleService;
import com.min.zblog.data.entity.TmArchive;
import com.min.zblog.data.entity.TmArticle;
import com.min.zblog.data.entity.TmArticleTag;
import com.min.zblog.data.entity.TmArticleTagKey;
import com.min.zblog.data.entity.TmCategory;
import com.min.zblog.data.entity.TmTag;
import com.min.zblog.data.view.ArchiveInfo;
import com.min.zblog.data.view.ArticleInfo;
import com.min.zblog.data.view.BlogInfo;
import com.min.zblog.data.view.PageInfo;
import com.min.zblog.facility.enums.ArticleState;
import com.min.zblog.facility.enums.Indicator;
import com.min.zblog.facility.enums.VisitType;
import com.min.zblog.facility.exception.ProcessException;
import com.min.zblog.facility.utils.Constants;

@Service
public class ArticleServiceImpl implements ArticleService {
	@Autowired
	private ArticleDao articleDao;
	@Autowired
	private VisitHstDao visitHstDao;
	@Autowired
	private ArticleTagDao articleTagDao;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private BlogQueryDsl blogQueryDsl;
	@Autowired
	private ArchiveDao archiveDao;
	@Autowired
	private CommentDao commentDao;
	
	public List<TmArticle> listAll() {
		return articleDao.findAll();
	}
	
	public TmArticle findOne(Long id) {
		return articleDao.findOne(id);
	}
	
	public TmArticle addArticle(Map<String, Object> map) {
		Date time = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		String archiveTitle = sdf.format(time);
		String archiveName = archiveTitle.replace("-", "");
		
		//归档
		TmArchive archive = archiveDao.findByName(archiveName);
		Long archiveId;
		if(archive == null){
			TmArchive newArchive = new TmArchive(archiveName, archiveTitle, 1, time, time, 0);
			archiveDao.save(newArchive);
			archiveId = newArchive.getId();
		}else{
			archiveId = archive.getId();
			archive.setCount(archive.getCount()+1);
			archive.setUpdateTime(time);
			archiveDao.save(archive);
		}
		
		TmArticle article = new TmArticle();
		article.setUserId(Long.valueOf(1));//默认用户1
		article.setTitle((String)map.get("title"));
		article.setDescription((String)map.get("description"));
		article.setTop((Indicator)map.get("top"));
		article.setRecommend((Indicator)map.get("recommend"));
    	article.setOriginal((Indicator)map.get("original"));
    	article.setComment((Indicator)map.get("comment"));
    	article.setCategoryId((Long)map.get("categoryId"));
    	article.setContent((String)map.get("markdown"));
    	article.setState((ArticleState)map.get("state"));
    	article.setArchiveId(archiveId);//归档id
    	article.setCreateTime(time);
    	article.setUpdateTime(time);
    	article.setJpaVersion(0);
    	
		articleDao.save(article);
		
		//更新标签
		List<Long> tagIdList = (List<Long>)map.get("tagIdList");
		if(tagIdList != null){
			for(Long tagId:tagIdList){
				//保存关联表tm_article_tag
				TmArticleTag articleTag = new TmArticleTag(tagId, article.getId(), time, time, 0);
				articleTagDao.save(articleTag);
			}
		}
		
		//更新分类
		//getOne委托给JPA实体管理器,返回实体引用，导致LazyInitializationException，建议使用findOne
		TmCategory category = categoryDao.findOne((Long)map.get("categoryId"));
		if(category != null){
			category.setCount(category.getCount()+1);
			category.setUpdateTime(time);
			categoryDao.save(category);
		}
		
		return article;
	}
	
	public void deleteArticleById(Long id) {
		TmArticle article = articleDao.findOne(id);
		if(article == null){
			throw new ProcessException(Constants.ERRA001_CODE, Constants.ERRA001_MSG);
		}
		
		//更新分类数、更新归档数
		TmCategory category = categoryDao.findOne(article.getCategoryId());
		if(category != null && category.getCount() != null){
			Integer count = category.getCount()-1;
			category.setCount(count<0?0:count);
			categoryDao.save(category);
		}
		TmArchive archive = archiveDao.findOne(article.getArchiveId());
		if(archive != null && archive.getCount() != null){
			Integer count = archive.getCount()-1;
			archive.setCount(count<0?0:count);
			archiveDao.save(archive);
		}
		//删除访问历史
		visitHstDao.deleteTmVisitHstByArticleId(id);
		//删除标签关联记录
		articleTagDao.deleteTmArticleTagByArticleId(id);
		//删除评论
		commentDao.deleteTmCommentByArticleId(id);
		//删除文章
		articleDao.delete(id);
	}
	
	@Override
	@Cacheable(value="articleByCategoryNameCache", key="T(String).valueOf(#pageSize).concat('-').concat(#currentPage).concat('-').concat(#name)")
	public PageInfo<ArticleInfo> listArticleByPageCategoryName(long pageSize, long currentPage, String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		//根据分类名称，查找已经发布文章
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByPageCategoryName(pageSize, currentPage, name, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			
			articleInfoList.add(articleInfo);
		}
		
		PageInfo<ArticleInfo> pageInfo = new PageInfo<ArticleInfo>();
		pageInfo.setCurrentPage(currentPage);
		long total = blogQueryDsl.countArticleByCategoryName(name, ArticleState.PUBLISH);
		pageInfo.setTotalPages((total%pageSize == 0)?(total/pageSize):((total/pageSize)+1));
		pageInfo.setList(articleInfoList);
		
		return pageInfo;
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
	@Cacheable(value="articleByArchiveCache", key="T(String).valueOf(#pageSize).concat('-').concat(#currentPage).concat('-').concat(#name)")
	public PageInfo<ArticleInfo> listArticleByPageArchive(long pageSize, long currentPage, String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		//根据分类名称，查找已经发布文章
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByPageArchive(pageSize, currentPage, name, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			
			articleInfoList.add(articleInfo);
		}
		
		PageInfo<ArticleInfo> pageInfo = new PageInfo<ArticleInfo>();
		pageInfo.setCurrentPage(currentPage);
		long total = blogQueryDsl.countArticleByArchive(name, ArticleState.PUBLISH);
		pageInfo.setTotalPages((total%pageSize == 0)?(total/pageSize):((total/pageSize)+1));
		pageInfo.setList(articleInfoList);
		
		return pageInfo;
	}
	//fetchArticleByTag

	@Override
	@Cacheable(value="articleByTagCache", key="T(String).valueOf(#pageSize).concat('-').concat(#currentPage).concat('-').concat(#name)")
	public PageInfo<ArticleInfo> listArticleByPageTag(long pageSize, long currentPage, String name) {
		if(StringUtils.isBlank(name)) {
			return null;
		}
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByPageTag(pageSize, currentPage, name, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setId(article.getId());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfoList.add(articleInfo);
		}
		
		PageInfo<ArticleInfo> pageInfo = new PageInfo<ArticleInfo>();
		pageInfo.setCurrentPage(currentPage);
		long total = blogQueryDsl.countArticleByTag(name, ArticleState.PUBLISH);
		pageInfo.setTotalPages((total%pageSize == 0)?(total/pageSize):((total/pageSize)+1));
		pageInfo.setList(articleInfoList);
		
		return pageInfo;
	}

	/* 查询已发布且阅读排行前5的文章
	 * @see com.min.zblog.core.service.ArticleService#listArticleByReadRank()
	 */
	@Override
	public List<ArticleInfo> listArticleByReadRank() {
		List<ArticleInfo> gArticleInfoList = GlobalContextHolder.getArticleReadRankList();
		if(gArticleInfoList != null && gArticleInfoList.size() > 0) {
			return gArticleInfoList;
		}
		
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
		//缓存
		GlobalContextHolder.setArticleReadRankList(articleInfoList);
				
		return articleInfoList;
	}

	/* 获取博客统计信息
	 * @see com.min.zblog.core.service.ArticleService#obtainBlogInfo()
	 */
	@Override
	public BlogInfo obtainBlogInfo() {
		BlogInfo gBlogInfo = GlobalContextHolder.getBlogInfo();
		if(gBlogInfo != null) {
			return gBlogInfo;
		}
		BlogInfo blogInfo = new BlogInfo();
		blogInfo.setTotalArticleNum(articleDao.countByState(ArticleState.PUBLISH));
		blogInfo.setTotalReadNum(blogQueryDsl.countVisitHstByType(VisitType.READ, ArticleState.PUBLISH));
		blogInfo.setTotalCommentNum(blogQueryDsl.countCommentByState(ArticleState.PUBLISH));
		//缓存
		GlobalContextHolder.setBlogInfo(blogInfo);
		
		return blogInfo;
	}

	@Override
	@Cacheable(value="articleByPageCache", key="T(String).valueOf(#pageSize).concat('-').concat(#currentPage).concat('-').concat(#name)")
	public PageInfo<ArticleInfo> listArticleByPage(long pageSize, long currentPage, ArticleState state) {
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByPage(currentPage, pageSize, state);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			
			articleInfoList.add(articleInfo);
		}
		
		PageInfo<ArticleInfo> pageInfo = new PageInfo<ArticleInfo>();
		pageInfo.setCurrentPage(currentPage);
		long total = articleDao.countByState(state);
		pageInfo.setCount(total);
		pageInfo.setTotalPages((total%pageSize == 0)?(total/pageSize):((total/pageSize)+1));
		pageInfo.setList(articleInfoList);
		
		return pageInfo;
	}
	
	@Override
	public PageInfo<ArticleInfo> queryArticleByPage(long pageSize, long currentPage, Map<String, Object> map) {
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleConditionByPage(currentPage, pageSize, map);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			
			articleInfoList.add(articleInfo);
		}
		
		PageInfo<ArticleInfo> pageInfo = new PageInfo<ArticleInfo>();
		pageInfo.setCurrentPage(currentPage);
		long total = blogQueryDsl.countArticleByCondition(map);
		
		pageInfo.setCount(total);
		pageInfo.setTotalPages((total%pageSize == 0)?(total/pageSize):((total/pageSize)+1));
		pageInfo.setList(articleInfoList);
		
		return pageInfo;
	}

	@Override
	public ArticleInfo findOneArticle(Long id) {
		ArticleInfo articleInfo = new ArticleInfo();
		
		TmArticle article = articleDao.findOne(id);
		if(article != null){
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setContent(article.getContent());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setFavorNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.FAVOR));
			articleInfo.setShareNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.SHARE));
			articleInfo.setTop(article.getTop() == Indicator.Y?true:false);
			articleInfo.setRecommend(article.getRecommend() == Indicator.Y?true:false);
			articleInfo.setOriginal(article.getOriginal() == Indicator.Y?true:false);
			articleInfo.setComment(article.getComment() == Indicator.Y?true:false);
			TmCategory category = categoryDao.findOne(article.getCategoryId());
			if(category != null){
				articleInfo.setCategoryId(category.getId());
				articleInfo.setCategoryName(category.getName());
			}
			List<TmTag> tmTagList = blogQueryDsl.fetchTagByArticleId(article.getId());
			List<String> list = new ArrayList<String>();
			List<Long> tagIdList = new ArrayList<Long>();
			for(TmTag tag:tmTagList){
				list.add(tag.getName());
				tagIdList.add(tag.getId());
			}
			articleInfo.setTagList(list);
			articleInfo.setTagIdList(tagIdList);
		}
		
		return articleInfo;
	}

	@Override
	public ArticleInfo findPreOneArticle(Long id) {
		TmArticle article = blogQueryDsl.fetchFirstArticleByIdNear(id, ArticleState.PUBLISH, Indicator.Y);
		ArticleInfo articleInfo = new ArticleInfo();
		if(article != null){
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setContent(article.getContent());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setFavorNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.FAVOR));
			articleInfo.setShareNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.SHARE));
			TmCategory category = categoryDao.findOne(article.getCategoryId());
			if(category != null){
				articleInfo.setCategoryName(category.getName());
			}
			List<TmTag> tmTagList = blogQueryDsl.fetchTagByArticleId(article.getId());
			List<String> list = new ArrayList<String>();
			for(TmTag tag:tmTagList){
				list.add(tag.getName());
			}
			articleInfo.setTagList(list);
		}
		return articleInfo;
	}

	@Override
	public ArticleInfo findNextOneArticle(Long id) {
		TmArticle article = blogQueryDsl.fetchFirstArticleByIdNear(id, ArticleState.PUBLISH, Indicator.N);
		ArticleInfo articleInfo = new ArticleInfo();
		if(article != null){
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setContent(article.getContent());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			articleInfo.setFavorNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.FAVOR));
			articleInfo.setShareNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.SHARE));
			TmCategory category = categoryDao.findOne(article.getCategoryId());
			if(category != null){
				articleInfo.setCategoryName(category.getName());
			}
			List<TmTag> tmTagList = blogQueryDsl.fetchTagByArticleId(article.getId());
			List<String> list = new ArrayList<String>();
			for(TmTag tag:tmTagList){
				list.add(tag.getName());
			}
			articleInfo.setTagList(list);
		}
		return articleInfo;
	}

	@Override
	public TmArticle saveArticle(Map<String, Object> map) throws ProcessException {
		TmArticle article = articleDao.findOne((Long)map.get("articleId"));
		if(article == null){
			throw new ProcessException(Constants.ERRA001_CODE, Constants.ERRA001_MSG);
		}
		
		Date time = new Date();
		article.setTitle((String)map.get("title"));
		article.setDescription((String)map.get("description"));
		article.setTop((Indicator)map.get("top"));
		article.setRecommend((Indicator)map.get("recommend"));
    	article.setOriginal((Indicator)map.get("original"));
    	article.setComment((Indicator)map.get("comment"));
    	article.setCategoryId((Long)map.get("categoryId"));
    	article.setContent((String)map.get("markdown"));
    	if(ArticleState.CREATE == article.getState()){
    		article.setState((ArticleState)map.get("state"));
    	}
    	article.setUpdateTime(time);
//    	article.setJpaVersion(0);
    	
		articleDao.save(article);
		
		//更新标签
		List<Long> tagIdList = (List<Long>)map.get("tagIdList");
		if(tagIdList != null){
			for(Long tagId:tagIdList){
				//保存关联表tm_article_tag
				//如果不存在，则保存
				TmArticleTagKey key = new TmArticleTagKey(tagId, article.getId());
				if(!articleTagDao.exists(key)){
					TmArticleTag articleTag = new TmArticleTag(tagId, article.getId(), time, time, 0);
					articleTagDao.save(articleTag);
				}
			}
		}
		
		//更新分类
		//getOne委托给JPA实体管理器,返回实体引用，导致LazyInitializationException，建议使用findOne
		TmCategory category = categoryDao.findOne((Long)map.get("categoryId"));
		if(category != null){
			category.setCount(category.getCount()+1);
			category.setUpdateTime(time);
			categoryDao.save(category);
		}
		
		return article;
	}

	/* 软删除，更改文章状态
	 * @see com.min.zblog.core.service.ArticleService#deleteArticleVirtualById(java.lang.Long)
	 */
	@Override
	public void deleteArticleVirtualById(Long articleId) throws ProcessException {
		TmArticle article = articleDao.findOne(articleId);
		if(article == null){
			throw new ProcessException(Constants.ERRA001_CODE, Constants.ERRA001_MSG);
		}
		
		//更新分类数、更新归档数、文章状态
		TmCategory category = categoryDao.findOne(article.getCategoryId());
		if(category != null && category.getCount() != null){
			Integer count = category.getCount()-1;
			category.setCount(count<0?0:count);
			categoryDao.save(category);
		}
		TmArchive archive = archiveDao.findOne(article.getArchiveId());
		if(archive != null && archive.getCount() != null){
			Integer count = archive.getCount()-1;
			archive.setCount(count<0?0:count);
			archiveDao.save(archive);
		}
		
		article.setState(ArticleState.DELETE);
		articleDao.save(article);
	}
	
	@Override
	public PageInfo<ArticleInfo> listArticleByPageCategoryId(long pageSize, long currentPage, Long id) {
		if(id == null) {
			return null;
		}
		//根据分类名称，查找已经发布文章
		List<TmArticle> tmArticleList = blogQueryDsl.fetchArticleByPageCategoryId(pageSize, currentPage, id, ArticleState.PUBLISH);
		List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
		for(TmArticle article:tmArticleList){
			
			ArticleInfo articleInfo = new ArticleInfo();
			articleInfo.setId(article.getId());
			articleInfo.setTitle(article.getTitle());
			articleInfo.setDescription(article.getDescription());
			articleInfo.setCreateTime(article.getCreateTime());
			articleInfo.setCommentNum(blogQueryDsl.countCommentByArticleId(article.getId()));
			articleInfo.setReadNum(blogQueryDsl.countVisitHstByArticleId(article.getId(), VisitType.READ));
			
			articleInfoList.add(articleInfo);
		}
		
		PageInfo<ArticleInfo> pageInfo = new PageInfo<ArticleInfo>();
		pageInfo.setCurrentPage(currentPage);
		long total = blogQueryDsl.countArticleByCategoryId(id, ArticleState.PUBLISH);
		pageInfo.setTotalPages((total%pageSize == 0)?(total/pageSize):((total/pageSize)+1));
		pageInfo.setList(articleInfoList);
		
		return pageInfo;
	}
}
