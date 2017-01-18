package com.toptal.blog.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.PartialCacheKey;
import com.googlecode.ehcache.annotations.Property;
import com.googlecode.ehcache.annotations.TriggersRemove;

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
