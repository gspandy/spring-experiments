package me.loki2302;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebIntegrationTest
@SpringApplicationConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ServletFilterTest {
    @Test
    public void ping() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("http://localhost:8080/", String.class);
        assertEquals("hello there 127.0.0.1", result);
    }

    @Configuration
    @EnableAutoConfiguration
    public static class Config {
        @Bean
        public DummyController dummyController() {
            return new DummyController();
        }

        @Bean
        public FilterRegistrationBean filterRegistrationBean() {
            FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new MyFilter());
            return filterRegistrationBean;
        }
    }

    @RestController
    public static class DummyController {
        @RequestMapping(value = "/", method = RequestMethod.GET)
        public String index(@Value("#{request.getAttribute('TheRemoteAddr')}") String remoteAddr) {
            return "hello there " + remoteAddr;
        }
    }

    public static class MyFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(
                ServletRequest request,
                ServletResponse response,
                FilterChain chain) throws IOException, ServletException {

            request.setAttribute("TheRemoteAddr", request.getRemoteAddr());
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }
    }
}
