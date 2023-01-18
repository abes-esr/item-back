package fr.abes.item;

import fr.abes.item.constant.Constant;
import fr.abes.item.traitement.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Slf4j
public class KopyaBatchConfigurer implements BatchConfigurer {
    private final EntityManagerFactory entityManagerFactory;

    private PlatformTransactionManager transactionManager;

    private JobRepository jobRepository;

    private JobLauncher jobLauncher;

    private JobExplorer jobExplorer;


    @Autowired
    @Qualifier("itemDataSource")
    @SuppressWarnings("squid:S3305")
    protected DataSource kopyaDataSource;

    /**
     * Create a new {@link KopyaBatchConfigurer} instance.
     *
     * @param entityManagerFactory the entity manager factory
     */
    public KopyaBatchConfigurer(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public JobRepository getJobRepository() {
        return this.jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return this.jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        return this.jobExplorer;
    }

    @PostConstruct
    public void initialize() {
        try {
            // transactionManager:
            log.warn(Constant.SPRING_BATCH_FORCING_USAGE_JPA_TRANSACTION_MANAGER);
            if (this.entityManagerFactory == null) {
                log.error(Constant.SPRING_BATCH_ENTITY_MANAGER_FACTORY_NULL);
            } else {
                this.transactionManager = new JpaTransactionManager(this.entityManagerFactory);
            }
            // jobRepository:
            JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
            factory.setDataSource(kopyaDataSource);
            factory.setTransactionManager(transactionManager);
            factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
            factory.setTablePrefix("BATCH_");
            factory.setMaxVarCharLength(1000);
            factory.setValidateTransactionState(false);
            this.jobRepository = factory.getObject();

            // jobLauncher:
            SimpleJobLauncher jobLauncherParam = new SimpleJobLauncher();
            jobLauncherParam.setJobRepository(getJobRepository());
            jobLauncherParam.afterPropertiesSet();
            this.jobLauncher = jobLauncherParam;

            // jobExplorer:
            JobExplorerFactoryBean jobExplorerFactory = new JobExplorerFactoryBean();
            jobExplorerFactory.setDataSource(kopyaDataSource);
            jobExplorerFactory.setTablePrefix("BATCH_");
            jobExplorerFactory.afterPropertiesSet();
            this.jobExplorer = jobExplorerFactory.getObject();

        } catch (Exception ex) {
            throw new IllegalStateException(Constant.SPRING_BATCH_INITIALIZATION_FAILED, ex);
        }
    }

}
