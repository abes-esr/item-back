package fr.abes.item.core.configuration;

import jakarta.persistence.EntityManagerFactory;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(transactionManagerRef = "itemTransactionManager",
		entityManagerFactoryRef = "itemEntityManagerFactory",
		basePackages = "fr.abes.item.core.repository.item")
@NoArgsConstructor
@ItemConfiguration
public class ItemPostgreConfig extends AbstractConfig {
	@Value("${spring.jpa.item.hibernate.ddl-auto}")
	protected String ddlAuto;
	@Value("${spring.jpa.item.generate-ddl}")
	protected boolean generateDdl;
	@Value("${spring.jpa.item.show-sql}")
	private boolean showsql;
	@Value("${spring.sql.item.init.mode}")
	private String initMode;
	@Value("${spring.hibernate.item.enable_lazy_load_no_trans}")
	private boolean lazyload;


	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource.item")
	public DataSource itemDataSource() {
		return DataSourceBuilder.create().build();
	}


	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean itemEntityManagerFactory() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(itemDataSource());
		em.setPackagesToScan("fr.abes.item.core.entities.item");
		configHibernate(em, showsql, ddlAuto, generateDdl, initMode, lazyload);
		return em;
	}


	@Bean
	@Primary
	public JpaTransactionManager itemTransactionManager(EntityManagerFactory entityManagerFactory) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}

	@Bean(name = "itemJdbcTemplate")
	public JdbcTemplate itemJdbcTemplate() { return new JdbcTemplate(itemDataSource());}

}
