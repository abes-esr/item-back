package fr.abes.item.configuration;


import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(transactionManagerRef = "baseXmlTransactionManager",
		entityManagerFactoryRef = "baseXmlEntityManager",
		basePackages = "fr.abes.item.dao.baseXml")
@NoArgsConstructor
public class BaseXMLOracleConfig extends AbstractConfig {
	@Value("${spring.jpa.basexml.database-platform}")
	protected String platform;
	@Value("${spring.jpa.basexml.hibernate.ddl-auto}")
	protected String ddlAuto;
	@Value("${spring.jpa.basexml.generate-ddl}")
	protected boolean generateDdl;
	@Value("${spring.jpa.basexml.properties.hibernate.dialect}")
	protected String dialect;
	@Value("${spring.jpa.basexml.show-sql}")
	private boolean showsql;
	@Value("${spring.sql.basexml.init.mode}")
	private String initMode;
	@Value("${spring.hibernate.basexml.enable_lazy_load_no_trans}")
	private boolean lazyload;

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.basexml")
	public DataSource baseXmlDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean baseXmlEntityManager() {
		LocalContainerEntityManagerFactoryBean em
				= new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(baseXmlDataSource());
		em.setPackagesToScan(
				new String[] { "fr.abes.item.entities.baseXml" });
		configHibernate(em, platform, showsql, dialect, ddlAuto, generateDdl, initMode, lazyload);
		return em;
	}

	@Bean
	public PlatformTransactionManager baseXmlTransactionManager() {
		JpaTransactionManager transactionManager
				= new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(
				baseXmlEntityManager().getObject());
		return transactionManager;
	}

	@Bean(name = "baseXmlJdbcTemplate")
	public JdbcTemplate baseXmlJdbcTemplate() {
		 return new JdbcTemplate(baseXmlDataSource());
	 }

}
