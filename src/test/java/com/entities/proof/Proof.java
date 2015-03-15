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
		// initial save, begins and commits cleanly
		// see AbstractViewModelRepository.create(id)
		em.getTransaction().begin();
		ViewEntity viewEntity = new ViewEntity(42);
		System.out.println("first save: " + viewEntity);
		em.persist(viewEntity);
		em.getTransaction().commit();

		// second phase! we got some new information and now we need to modify
		// the entity
		em.getTransaction().begin();

		System.out.println("marking as dirty and setting the view data");
		viewEntity.markDirty(); // same as calling write() on ViewModelEntity
		viewEntity.setViewData("cubicle walls");
		System.out.println("flushing: " + viewEntity);

		/*
		 * Here's where we depart from the happy path. In our case,
		 * BusinessServiceQuery looks up the defaultServiceLocationType, which
		 * in turn calls the ReferenceDataRepository to run a query on the
		 * reference data.
		 * 
		 * A potentially external service call while building a view model. That
		 * topic is a separate conversation, but it is not really ideal. It's
		 * probably okay in this case because we know the data comes from the
		 * local database. In this test, we'll skip past the junk of making a
		 * service call while saving.
		 * 
		 * What we know is that the reference data cache is not initialized
		 * automatically. It is only populated under one circumstance: when the
		 * UI loads for the first time. If the UI has not been initiated by a
		 * user (after a restart, perhaps) then the cache is empty, the entity
		 * manager has to run a query (a select in this case) and the entity
		 * manager flushes during the process of running the query.
		 * 
		 * JPA review: flushing synchronizes the persistence context to the
		 * database. This means any entity will have any unpersisted updates run
		 * on the database. Hibernate will call the SQL on the database, but it
		 * has not committed it yet. This is useful to detect if an incompatible
		 * change has already happened in the database.
		 * 
		 * Incidentally, there is no guarantee of when the flush happens, just
		 * that it will happen at some point. Only the order of operations is
		 * guaranteed.
		 */
		em.flush();
		System.out.println("done flushing");
		/*
		 * Now that the flush has been run, the dirty flag is presumably set
		 * back to false.
		 * 
		 * This is obviously problematic because the dirty flag being true is
		 * the only way we can get our processed data up to date.
		 * 
		 * We can keep making changes, but when the commit happens, this field
		 * will be skipped because the entity thinks it's not dirty.
		 */
		viewEntity.setViewData("mountain and sky");
		System.out.println("committing this entity: " + viewEntity);
		em.getTransaction().commit();

		// clear the em to make sure it doesn't return the same object reference
		// as before
		em.clear();

		ViewEntity retrievedEntity = em.find(ViewEntity.class, 42);
		System.out.println("final result: " + retrievedEntity);

		// Compare to what we would expect to find at this point.
		ViewEntity expectedResult = new ViewEntity(42);
		expectedResult.setVersion(2L);
		expectedResult.setProcessedViewData("MOUNTAIN AND SKY");
		System.out.println("expected: " + expectedResult);

		Assert.assertEquals("" + //
				"expected to be looking at mountains and sky, " + //
				"but instead I'm looking at cubicle walls.", expectedResult,
				retrievedEntity);
	}
}
