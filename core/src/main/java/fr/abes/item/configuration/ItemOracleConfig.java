package fr.abes.item.configuration;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(transactionManagerRef = "itemTransactionManager",
		entityManagerFactoryRef = "itemEntityManagerFactory",
		basePackages = "fr.abes.item.dao.item")
@NoArgsConstructor
public class ItemOracleConfig extends AbstractConfig {
	@Value("${kopya.datasource.url}")
	private String url;
	@Value("${kopya.datasource.username}")
	private String username;
	@Value("${kopya.datasource.password}")
	private String password;
	@Value("${kopya.datasource.driver-class-name}")
	private String driver;

	@Primary
	@Bean
	public DataSource itemDataSource() {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean itemEntityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(itemDataSource());
		em.setPackagesToScan(new String[]{"fr.abes.item.entities.item", "fr.abes.item.entities.baseXml"});
		configHibernate(em);
		return em;
	}

	@Primary
	@Bean
	public JpaTransactionManager itemTransactionManager(EntityManagerFactory entityManagerFactory) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}
	
	@Primary
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Bean(name = "kopyaJdbcTemplate")
	public JdbcTemplate kopyaJdbcTemplate() { return new JdbcTemplate(itemDataSource());}

}
