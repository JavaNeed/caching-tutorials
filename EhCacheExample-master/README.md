# EhCacheExample
This is a sample project to integrate EhCache with Spring using EhCache Spring Annotations.

EhCache is a widely-used, pure Java cache that can be easily integrated with most popular Java frameworks, such as Spring and Hibernate. It is often considered to be the most convenient choice for Java applications since it can be integrated into projects easily. In particular:

It can be set up by simply including the JAR in your project. No additional installation steps are required.
It runs in the same process with the application, so it’s fast. No additional service is required to run.
In short, EhCache is a great choice for any pure-Java application.

Additionally, EhCache Spring Annotations allows seamless integration into any Spring application by simply adding annotations to cacheable methods, without modifying the method implementations.

While EhCache provides straight-forward, rich APIs to manipulate the cache programmatically, this article mainly focuses on boosting your Spring applications in a less intrusive way with EhCache Spring Annotations. We’ll set up a Spring MVC project and deploy a RESTful web service in Tomcat. Then, EhCache will be integrated to the web service.

GET http://localhost:8080/EhCacheExample/message/1

POST http://localhost:8080/EhCacheExample/message/set/1/test_message


We’ll define the MessageService class in com.toptal.blog.service. It will access messages stored in our System of Records (SOR). In a production app, the SOR would be something like a relational database. For simplicity, we will use a HashMap:

```
@Service
public class MessageService {
   private ConcurrentHashMap<Integer, String> messages
   = new ConcurrentHashMap<Integer, String>();
   
   public String getMessage( Integer id ) {
      System.out.println( "Getting data from SOR......" );
      return messages.get( id );
   }

   public void setMessage( Integer id, String message ){
      messages.put( id, message );
   }
}
```

Set Up a Custom Cache Manager
--------------------------------
Spring has a built-in EhCache cache manager, org.springframework.cache.ehcache.EhCacheManagerFactoryBean. This is suitable for most caching situations, but I have found defining a custom cache manager to be useful because it allows me to control the cache either programmatically, or with annotations, using the same cache manager. This article focuses on annotations, but let’s go ahead and define a custom cache manager so we will be ready in case we need it. If you prefer to stick with the default cache manager , you can skip this step.

We’ll define the new class in com.toptal.blog.cache.CustomCacheManager:

```
public class CustomCacheManager extends net.sf.ehcache.CacheManager {
	public CustomCacheManager() {
		super();
	}
}
```

Enable it by updating springrest-servlet.xml as follows:

```
<ehcache:annotation-driven cache-manager="customCacheManager" />
   <bean id="customCacheManager"
         class="com.toptal.blog.cache.CustomCacheManager"
         scope="singleton"></bean>
```

Configure EhCache
--------------------
Finally, create the EhCache configuration file ehcache.xml in the classpath. By default, Eclipse will include src/main/resources in the classpath, and we’ll place the file here. This file is required for EhCache to function properly. It defines the cache names and some properties of each cache, such as the timeToLiveSeconds:

```
<ehcache xmlms:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="ehcache.xsd">
   <diskStore path="cache" />
   <cache
      name="messageCache"
      maxElementsInMemory="10000"
      eternal="false"
      timeToIdleSeconds="0"
      timeToLiveSeconds="10"
      overflowToDisk="false"
      memoryStoreEvictionPolicy="LFU" />      
</ehcache>
```

Test the Cache
---------------
Now, with everything set up and ready to go, using EhCache should be easy and happy work. We can simply add @Cacheable to the method or class we want to cache. For example, I added @Cacheable to the getMessage method in MessageService. It’s that easy!

```
@Cacheable( cacheName = "messageCache" )
public String getMessage( Integer id ) {
   System.out.println( "Getting data from SOR......" );
   return messages.get( id );
}
```

To test that our cache is working, we can create a message for ID=1 by issuing an HTTP POST request at http://localhost:8080/EhCacheExample/message/set/1/newMessage, and then get the message for ID=1 multiple times, with GET requests to http://localhost:8080/EhCacheExample/message/1. As you can see in the console output below, the web service asks the SOR to get the message the first time we request the message, but not for the next two requests, returning the cached message instead. Because we defined the timeToLiveSeconds to be 10, the web service calls the SOR to get the message again after 10 seconds:

```
set message [newMessage] at Sun Dec 06 23:55:39 MST 2015
get message [newMessage] at Sun Dec 06 23:55:42 MST 2015
Getting data from SOR......
get message [newMessage] at Sun Dec 06 23:55:47 MST 2015
get message [newMessage] at Sun Dec 06 23:55:49 MST 2015
get message [newMessage] at Sun Dec 06 23:55:54 MST 2015
Getting data from SOR......
```

Refreshing the Cache
--------------------
Now, we are enjoying the speed and convenience a cache gives us, and EhCache is nice enough to refresh by itself every 10 seconds. But what if we would like to have it refreshed immediately after our SOR is updated? EhCache Spring Annotation offers @TriggersRemove to remove specified keys from the cache when the annotated method is called. In our message service API, the cached message should be removed from the cache when setMessage is called. Thus, the next time a getMessage request comes in, the cache will fetch a fresh record from the SOR:


```
@Service
public class MessageService {
	private ConcurrentHashMap<Integer, String> messages = new ConcurrentHashMap<Integer, String>();

	@Cacheable(cacheName = "messageCache", 
			selfPopulating = true, 
			keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", 
			properties = @Property(name = "includeMethod", value = "false") ) )
	public String getMessage(Integer id) {
		System.out.println("Getting data from SOR......");
		return messages.get(id);
	}

	@TriggersRemove(cacheName = "messageCache", 
			keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", 
			properties = @Property(name = "includeMethod", value = "false") ) )
	public void setMessage(@PartialCacheKey Integer id, String message) {
		messages.put(id, message);
	}
}
```

A key generator is used by the cache manager to generate the cache key. A list of pre-defined cache key generators can be found here. By default, @KeyGenerator consumes both the method name and the passed in parameters to generate the cache key. But since we want the setMessage method to generate the same key as getMessage and delete the cached value associated with that key, we must use only the message ID as the key and eliminate the method name for key generation. We therefore set the key generator’s includeMethod property to be false for both methods. Also, since setMessage has two arguments, we use EhCache’s @PartialCacheKey annotation on the id parameter to specify that it is the only one that should be used by the key generator. Finally, recall that we configured a dedicated cache, messageCache, for this resource type, so using only the ID for the key presents no danger of conflicts with other resources types.

Now, if we do several HTTP requests for the message with ID=1, as follows:

```
HTTP POST:  http://localhost:8080/EhCacheExample/message/set/1/newMessage1
HTTP GET:http://localhost:8080/EhCacheExample/message/1
HTTP POST: http://localhost:8080/EhCacheExample/message/set/1/newMessage2
HTTP GET:http://localhost:8080/EhCacheExample/message/1
```

The console will show:
```
set message [newMessage1] at Tue Dec 08 17:53:44 MST 2015
get message [newMessage1] at Tue Dec 08 17:53:47 MST 2015
Getting data from SOR......
set message [newMessage2] at Tue Dec 08 17:53:50 MST 2015
get message [newMessage2] at Tue Dec 08 17:53:53 MST 2015
Getting data from SOR......
```

In this example, we first created a simple Spring MVC RESTful web application. Without modifying even one line of the existing application code, we then seamlessly integrated EhCache into the application using EhCache Spring Annotations. We have demonstrated that EhCache Spring Annotations is both easy to install (by adding its Maven dependency) and elegant to use (by adding annotations to methods).



