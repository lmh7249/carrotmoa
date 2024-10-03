package carrotmoa.carrotmoa.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/community")
@Controller
public class CommunityViewController {

    @GetMapping
    public String getFleaMarket() {
        return "community";
    }
}