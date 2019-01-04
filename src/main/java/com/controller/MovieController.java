package com.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);


    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @HystrixCommand(fallbackMethod = "findByIdFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000"),
            @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000")
    }, threadPoolProperties = {
            @HystrixProperty(name = "coreSize", value = "1"),
            @HystrixProperty(name = "maxQueueSize", value = "10")
    })
    @GetMapping("/user/{id}")
    public Map findById(@PathVariable Long id) {
        return restTemplate.getForObject("http://eureka-provider/" + id, Map.class);
    }

    @GetMapping("/logInstance")
    public void logInstance() {
        ServiceInstance serviceInstance = loadBalancerClient.choose("eureka-provider");
        logger.info("{}:{}:{}", serviceInstance.getServiceId(), serviceInstance.getHost(), serviceInstance.getPort());

    }

    public Map findByIdFallback(Long id) {
        Map map = new HashMap();
        map.put("error", "hystrix error");
        return map;
    }

    @HystrixCommand(fallbackMethod = "findByIdFallback1", commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    @GetMapping("/user/{id}")
    public Map findById1(@PathVariable Long id) {
        return restTemplate.getForObject("http://eureka-provider/" + id, Map.class);
    }
}
