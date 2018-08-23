package com.min.zblog.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.min.zblog.entity.TmArticle;

@RunWith(SpringJUnit4ClassRunner.class) //使用junit4进行测试
@ContextConfiguration(locations={"classpath:spring-hibernate.xml"}) //加载配置文件
public class TestArticleService {
	@Autowired
	private ArticleService articleService;
	
	@Test
	public void testListArticle(){
		System.out.println(articleService.listAll());
	}
	
	public void testAddArticle(){
		TmArticle article = new TmArticle();
		article.setTitle("maven");
		article.setComment('Y');
		article.setContent("Maven其实主要就是解决项目编译和项目依赖的问题，在大型项目中很有优势。例如你的一个项目中有几十外部依赖包，和你自己写的一些包，如果是不用Maven这种东西的话，你得一个一个包的下载，这样比较效率低下，而且一旦你用的包有更新，你又要重新去下载，可能在小项目中这倒没什么，但是在正规的大项目中，外部包的更新换代对项目还是有很大的影响，如果一个外部包有漏洞，可能给黑客攻击的机会，这个时候就必须得更新包。");
		articleService.addArticle(article);
	}
	
	public void testDeleteArticle(){
	}
}
