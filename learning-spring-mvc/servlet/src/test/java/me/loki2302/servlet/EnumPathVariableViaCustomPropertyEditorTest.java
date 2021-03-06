package me.loki2302.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.beans.PropertyEditorSupport;

import static org.junit.Assert.assertEquals;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@RunWith(SpringRunner.class)
public class EnumPathVariableViaCustomPropertyEditorTest {
    @Test
    public void checkCurrencies() {
        RestTemplate restTemplate = new RestTemplate();
        Assert.assertEquals("US Dollars", restTemplate.getForObject("http://localhost:8080/usd", String.class));
        Assert.assertEquals("Euros", restTemplate.getForObject("http://localhost:8080/eur", String.class));

        try {
            restTemplate.getForObject("http://localhost:8080/omg", String.class);
        } catch(HttpClientErrorException e) {
            Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        }
    }

    @Configuration
    @EnableAutoConfiguration
    public static class Config {
        @Bean
        public CurrencyController dummyController() {
            return new CurrencyController();
        }
    }

    @RestController
    public static class CurrencyController {
        @InitBinder
        public void registerCurrencyEnumPropertyEditor(WebDataBinder webDataBinder) {
            webDataBinder.registerCustomEditor(Currency.class, new CurrencyEnumPropertyEditor());
        }

        @RequestMapping(value = "/{currency}", method = RequestMethod.GET)
        public String index(@PathVariable("currency") Currency currency) {
            if(currency.equals(Currency.USD)) {
                return "US Dollars";
            } else if(currency.equals(Currency.EUR)) {
                return "Euros";
            }

            return "should never happen";
        }
    }

    public static class CurrencyEnumPropertyEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String s) throws IllegalArgumentException {
            if(s.equals("usd")) {
                setValue(Currency.USD);
            } else if(s.equals("eur")) {
                setValue(Currency.EUR);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public enum Currency {
        USD,
        EUR
    }
}
