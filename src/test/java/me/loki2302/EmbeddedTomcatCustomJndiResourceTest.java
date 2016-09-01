package me.loki2302;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.servlet.http.HttpSession;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
public class EmbeddedTomcatCustomJndiResourceTest {
    @Test
    public void ping() {
        RestTemplate restTemplate = new RestTemplate();
        assertEquals("dummyContextResource says: Hello JNDI!", restTemplate.getForObject("http://localhost:8080/", String.class));
    }

    @Configuration
    @EnableAutoConfiguration
    public static class Config {
        @Bean
        public DummyController dummyController() {
            return new DummyController();
        }

        @Bean
        public DummyContextResource dummyContextResource() throws NamingException {
            JndiTemplate jndiTemplate = new JndiTemplate();
            return jndiTemplate.lookup("java:comp/env/dummy", DummyContextResource.class);
        }

        @Bean
        public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
            TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory = new TomcatEmbeddedServletContainerFactory() {
                @Override
                protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(Tomcat tomcat) {
                    tomcat.enableNaming();
                    return super.getTomcatEmbeddedServletContainer(tomcat);
                }
            };

            tomcatEmbeddedServletContainerFactory.addContextCustomizers(new TomcatContextCustomizer() {
                @Override
                public void customize(Context context) {
                    ContextResource dummyContextResource = new ContextResource();
                    dummyContextResource.setName("dummy");
                    dummyContextResource.setType(DummyContextResource.class.getName());
                    dummyContextResource.setProperty("message", "Hello JNDI!");
                    dummyContextResource.setProperty("factory", DummyContextResourceFactory.class.getName());

                    context.getNamingResources().addResource(dummyContextResource);
                }
            });

            return tomcatEmbeddedServletContainerFactory;
        }
    }

    public static class DummyContextResource {
        public final String message;

        public DummyContextResource(String message) {
            this.message = message;
        }
    }

    public static class DummyContextResourceFactory implements ObjectFactory {
        private final static Logger LOGGER = LoggerFactory.getLogger(DummyContextResourceFactory.class);

        @Override
        public Object getObjectInstance(Object o, Name name, javax.naming.Context context, Hashtable<?, ?> hashtable) throws Exception {
            Reference reference = (Reference)o;
            List<RefAddr> refAddrList = Collections.list(reference.getAll());
            Map<String, Object> properties = refAddrList.stream()
                    .collect(Collectors.toMap(RefAddr::getType, RefAddr::getContent));

            LOGGER.info("Constructing " + DummyContextResource.class.getSimpleName() + " with {}", properties);

            String message = (String)properties.get("message");

            DummyContextResource dummyContextResource = new DummyContextResource(message);

            return dummyContextResource;
        }
    }

    @RestController
    public static class DummyController {
        @Autowired
        private DummyContextResource dummyContextResource;

        @RequestMapping(value = "/", method = RequestMethod.GET)
        public String index(HttpSession session) { // inject session to start it
            return "dummyContextResource says: " + dummyContextResource.message;
        }
    }
}
