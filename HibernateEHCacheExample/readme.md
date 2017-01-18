Hibernate EHCache – Hibernate Second Level Cache
------------------------------------
Welcome to the Hibernate Second Level Cache Example Tutorial. Today we will look into Hibernate EHCache that is the most popular Hibernate Second Level Cache provider.

One of the major benefit of using Hibernate in large application is it’s support for cache, hence reducing database queries and better performance. In earlier example, we looked into the Hibernate First Level Cache and today we will look into Hibernate Second Level Cache using Hibernate EHCache implementation.

Hibernate Second Level cache providers include EHCache and Infinispan, but EHCache is more popular and we will use it for our example project. However before we move to our project, we should know different strategies for caching an object.

- Read Only: This caching strategy should be used for persistent objects that will always read but never updated. It’s good for reading and caching application configuration and other static data that are never updated. This is the simplest strategy with best performance because there is no overload to check if the object is updated in database or not.
- Read Write: It’s good for persistent objects that can be updated by the hibernate application. However if the data is updated either through backend or other applications, then there is no way hibernate will know about it and data might be stale. So while using this strategy, make sure you are using Hibernate API for updating the data.
- Nonrestricted Read Write: If the application only occasionally needs to update data and strict transaction isolation is not required, a nonstrict-read-write cache might be appropriate.
- Transactional: The transactional cache strategy provides support for fully transactional cache providers such as JBoss TreeCache. Such a cache can only be used in a JTA environment and you must specify hibernate.transaction.manager_lookup_class.


Hibernate EHCache
-----------------
Since EHCache supports all the above cache strategies, it’s the best choice when you are looking for second level cache in hibernate. I would not go into much detail about EHCache, my main focus will be to get it working for hibernate application.

Create a maven project in the Eclipse or your favorite IDE, final implementation will look like below image.


Let’s look into each component of the application one by one.

Hibernate EHCache Maven Dependencies
------------------------------------
For hibernate second level cache, we would need to add ehcache-core and hibernate-ehcache dependencies in our application. EHCache uses slf4j for logging, so I have also added slf4j-simple for logging purposes. I am using the latest versions of all these APIs, there is a slight chance that hibernate-ehcache APIs are not compatible with the ehcache-core API, in that case you need to check the pom.xml of hibernate-ehcache to find out the correct version to use. Our final pom.xml looks like below.


Hibernate Second Level Cache – Hibernate EHCache Configuration
---------------------------------------------------------------
Hibernate Second level cache is disabled by default, so we would need to enable it and add some configurations to get it working. Our hibernate.cfg.xml file looks like below.


Some important points about hibernate second level cache configurations are:
--------------------------------------------------------------------------
- hibernate.cache.region.factory_class is used to define the Factory class for Second level caching, I am using org.hibernate.cache.ehcache.EhCacheRegionFactory for this. If you want the factory class to be singleton, you should use org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory class.
If you are using Hibernate 3, corresponding classes will be net.sf.ehcache.hibernate.EhCacheRegionFactory and net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory.

- hibernate.cache.use_second_level_cache is used to enable the second level cache.
- hibernate.cache.use_query_cache is used to enable the query cache, without it HQL queries results will not be cached.
net.sf.ehcache.configurationResourceName is used to define the EHCache configuration file location, it’s an optional parameter and if it’s not present EHCache will try to locate ehcache.xml file in the application classpath.
Hibernate EHCache Configuration File

Our EHCache configuration file myehcache.xml looks like below.

Hibernate EHCache provides a lot of options, I won’t go into much detail but some of the important configurations above are:

1. diskStore: EHCache stores data into memory but when it starts overflowing, it start writing data into file system. We use this property to define the location where EHCache will write the overflown data.
2. defaultCache: It’s a mandatory configuration, it is used when an Object need to be cached and there are no caching regions defined for that.
3. cache name=”employee”: We use cache element to define the region and it’s configurations. We can define multiple regions and their properties, while defining model beans cache properties, we can also define region with caching strategies. The cache properties are easy to understand and clear with the name.
4. Cache regions org.hibernate.cache.internal.StandardQueryCache and org.hibernate.cache.spi.UpdateTimestampsCache are defined because EHCache was giving warning to that.
Hibernate Second Level Cache – Model Bean Caching Strategy

We use org.hibernate.annotations.Cache annotation to provide the caching configuration. org.hibernate.annotations.CacheConcurrencyStrategy is used to define the caching strategy and we can also define the cache region to use for the model beans.

