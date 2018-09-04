package com.min.zblog.action.model;

import java.io.Serializable;

/**
 * 页面显示文章统计信息
 * @author zhouzm
 *
 */
public class ArticleInfo implements Serializable {
	
	private static final long serialVersionUID = -2090308250539116291L;
	
	/**
	 * 文章ID
	 */
	private Long id;
	
	/**
	 * 文章标题
	 */
	private String title;
	
	/**
	 * 阅读数
	 */
	private int readNum;
	
	/**
	 * 评论数
	 */
	private int commentNum;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getReadNum() {
		return readNum;
	}

	public void setReadNum(int readNum) {
		this.readNum = readNum;
	}

	public int getCommentNum() {
		return commentNum;
	}

	public void setCommentNum(int commentNum) {
		this.commentNum = commentNum;
	}
}
