package me.loki2302;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {
    @RequestMapping(value = "/", method = RequestMethod.GET)    
    public String index(Model model) {
        model.addAttribute("currentTime", new Date());
        return "index";
    }
    
    @RequestMapping(value = "/markdown", method = RequestMethod.GET)    
    public String markdown() {        
        return "markdown";
    }
}
