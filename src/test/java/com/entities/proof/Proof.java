package com.entities.proof;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.entities.config.AppConfigurationAware;

public class Proof extends AppConfigurationAware {

	@Autowired
	DataSource dataSource;

	@Autowired
	EntityManagerFactory emf;

	@Autowired
	PlatformTransactionManager txmgr;
	TransactionTemplate tx;
	JdbcTemplate jdbc;

	EntityManager em;

	@Before
	public void setUp() {
		em = emf.createEntityManager();
		tx = new TransactionTemplate(txmgr);
		jdbc = new JdbcTemplate(dataSource);
	}

	@After
	public void cleanUp() {
		em.clear();
		emf.close();
	}

	@Test
	public void demonstrateFlush() {
		em.getTransaction().begin();
		ViewEntity viewEntity = new ViewEntity(42);
		System.out.println("first save: " + viewEntity);
		em.persist(viewEntity);
		em.getTransaction().commit();

		em.close();
		em = emf.createEntityManager();
		
		ViewEntity entityCheck = em.find(ViewEntity.class, 42);
		System.out.println("after initial save: " + entityCheck);
		Assert.assertEquals(viewEntity, entityCheck);
		
		// second phase!
		em.getTransaction().begin();

		ViewEntity retrievedEntity = em.find(ViewEntity.class, 42);
		System.out.println("marking as dirty and setting the view data");
		retrievedEntity.markDirty();
		retrievedEntity.setViewData("cubicle walls");
		System.out.println("flushing: " + retrievedEntity);
		
		em.flush();
		System.out.println("done flushing");
		retrievedEntity.setViewData("mountain and sky");

		System.out.println("committing this entity: " + retrievedEntity);
		em.getTransaction().commit();
		
		em.close();
		em = emf.createEntityManager();
		ViewEntity retrievedEntity2 = em.find(ViewEntity.class, 42);
		System.out.println("final result: " + retrievedEntity2);
		ViewEntity expectedResult = new ViewEntity(42);
		expectedResult.setVersion(2L);
		expectedResult.setProcessedViewData("MOUNTAIN AND SKY");
		
		Assert.assertEquals(expectedResult, retrievedEntity2);
	}
}
