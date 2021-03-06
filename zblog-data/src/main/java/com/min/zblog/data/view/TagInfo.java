package com.min.zblog.data.view;

import java.io.Serializable;
import java.util.Date;

/**
 * 页面显示标签信息
 * @author zhouzm
 *
 */
public class TagInfo implements Serializable {

	private static final long serialVersionUID = -1085778405285680739L;
	
	/**
	 * 标签id
	 */
	private Long id;
	
	/**
	 * 标签名称
	 */
	private String tagName;
	
	/**
	 * 标签描述
	 */
	private String description;
	
	/**
	 * 标签样式
	 */
	private String style;
	
	/**
	 * 创建时间
	 */
	private Date createTime;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
