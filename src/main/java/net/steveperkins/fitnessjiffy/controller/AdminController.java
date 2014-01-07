package net.steveperkins.fitnessjiffy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;

@Controller
public class AdminController {

    @Autowired
    DataSource dataSource;

    @RequestMapping(value={"/admin"}, method= RequestMethod.GET)
    public String viewAdmin() {
        return Views.ADMIN_TEMPLATE;
    }

    @RequestMapping(value="/admin/import", method=RequestMethod.POST)
    public String importDatabase(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                String jsonString = new String(file.getBytes());
                System.out.println(jsonString);
            } catch (Exception e) {
                return "You failed to upload a file => " + e.getMessage();
            }
        } else {
            return "You failed to upload because the file was empty.";
        }
        return Views.ADMIN_TEMPLATE;
    }

}
