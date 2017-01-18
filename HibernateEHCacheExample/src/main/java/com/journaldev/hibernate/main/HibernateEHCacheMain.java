package com.journaldev.hibernate.main;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.stat.Statistics;

import com.journaldev.hibernate.model.Address;
import com.journaldev.hibernate.model.Employee;
import com.journaldev.hibernate.util.HibernateUtil;

public class HibernateEHCacheMain {

	public static void main(String[] args) {

		System.out.println("Temp Dir:" + System.getProperty("java.io.tmpdir"));

		// Initialize Sessions
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Statistics stats = sessionFactory.getStatistics();
		System.out.println("Stats enabled=" + stats.isStatisticsEnabled());
		stats.setStatisticsEnabled(true);
		System.out.println("Stats enabled=" + stats.isStatisticsEnabled());

		Session session = sessionFactory.openSession();
		Session otherSession = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Transaction otherTransaction = otherSession.beginTransaction();

		// load dummy data
		loadData(session,transaction);		

		printStats(stats, 0);
		
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();

		Employee emp = (Employee) session.load(Employee.class, 1L);
		printData(emp, stats, 1);

		emp = (Employee) session.load(Employee.class, 1L);
		printData(emp, stats, 2);

		//clear first level cache, so that second level cache is used
		session.evict(emp);
		emp = (Employee) session.load(Employee.class, 1L);
		printData(emp, stats, 3);

		emp = (Employee) session.load(Employee.class, 3L);
		printData(emp, stats, 4);

		emp = (Employee) otherSession.load(Employee.class, 1L);
		printData(emp, stats, 5);

		// Release resources
		transaction.commit();
		otherTransaction.commit();
		sessionFactory.close();
	}

	private static void printStats(Statistics stats, int i) {
		System.out.println("***** " + i + " *****");
		System.out.println("Fetch Count=" + stats.getEntityFetchCount());
		System.out.println("Second Level Hit Count=" + stats.getSecondLevelCacheHitCount());
		System.out.println("Second Level Miss Count=" + stats.getSecondLevelCacheMissCount());
		System.out.println("Second Level Put Count=" + stats.getSecondLevelCachePutCount());
	}

	private static void printData(Employee emp, Statistics stats, int count) {
		System.out.println(count + ":: Name=" + emp.getName() + ", Zipcode=" + emp.getAddress().getZipcode());
		printStats(stats, count);
	}

	private static void loadData(Session session,Transaction transaction){
		Address address1 = new Address();
		address1.setAddressLine1("AddressLine1-1");
		address1.setCity("City1");
		address1.setZipcode("1111");

		Employee employee1 = new Employee();
		employee1.setAddress(address1);
		employee1.setName("Name1");
		employee1.setSalary(100000);
		employee1.setAddress(address1);
		address1.setEmployee(employee1);

		Address address2 = new Address();
		address2.setAddressLine1("AddressLine1-2");
		address2.setCity("City2");
		address2.setZipcode("2222");

		Employee employee2 = new Employee();
		employee2.setAddress(address2);
		employee2.setName("Name2");
		employee2.setSalary(120000);
		employee2.setAddress(address2);
		address2.setEmployee(employee2);

		Address address3 = new Address();
		address3.setAddressLine1("AddressLine1-3");
		address3.setCity("City3");
		address3.setZipcode("333");

		Employee employee3 = new Employee();
		employee3.setAddress(address3);
		employee3.setName("Name3");
		employee3.setSalary(140000);
		employee3.setAddress(address3);
		address3.setEmployee(employee3);

		session.save(employee1);
		session.save(employee2);
		session.save(employee3);
		transaction.commit();
	}
}
