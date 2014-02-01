package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.etl.model.Datastore;
import net.steveperkins.fitnessjiffy.etl.writer.H2Writer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.util.Map;

@Controller
public class AdminController {

    @Autowired
    DataSource dataSource;

    @RequestMapping(value={"/admin"}, method= RequestMethod.GET)
    public String viewAdmin() {
        return Views.ADMIN_TEMPLATE;
    }

    @RequestMapping(value="/admin/import", method=RequestMethod.POST)
    public String importDatabase(@RequestParam("file") MultipartFile file, Map<String, Object> model){
        if (!file.isEmpty()) {
            try {
                String jsonString = new String(file.getBytes());
                Datastore datastore = Datastore.fromJSONString(jsonString);
                H2Writer h2Writer = new H2Writer(dataSource.getConnection(), datastore);
                h2Writer.write();
                model.put("result", "Database import complete");
            } catch (Exception e) {
                model.put("result", "You failed to upload a file => " + e.getMessage());
            }
        } else {
            model.put("result", "You failed to upload because the file was empty.");
        }
        return Views.ADMIN_TEMPLATE;
    }

}
