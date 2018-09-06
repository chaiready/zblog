package com.min.zblog.core.service;

import java.util.List;

import com.min.zblog.data.entity.TmArticle;
import com.min.zblog.data.view.ArchiveInfo;
import com.min.zblog.data.view.ArticleInfo;

public interface ArticleService {
	public List<TmArticle> listAll();
	public void addArticle(TmArticle article);
	public TmArticle findOne(Long id);
	public void deleteArticleById(Long id);
	public List<ArticleInfo> listArticleByCategoryName(String name);
	public List<ArticleInfo> listAllArticles();
}
