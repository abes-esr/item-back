package fr.abes.item.configuration;


import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
	@Value("${basexml.datasource.url}")
	private String url;
	@Value("${basexml.datasource.username}")
	private String username;
	@Value("${basexml.datasource.password}")
	private String password;
	@Value("${basexml.datasource.driver-class-name}")
	private String driver;

	@Bean(name="baseXmlDataSource")
	public DataSource baseXmlDataSource() {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean baseXmlEntityManager() {
		LocalContainerEntityManagerFactoryBean em
				= new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(baseXmlDataSource());
		em.setPackagesToScan(
				new String[] { "fr.abes.item.entities.baseXml" });
		configHibernate(em);
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
