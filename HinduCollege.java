import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document;  
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import java.net.URL;

import java.io.File;
import java.io.FileWriter;
public class HinduCollege {

    public static void main(String[] args) {
        try {
            FileWriter FWT = new FileWriter(new File("hinducollege.txt"));

            Document DOC = Jsoup.connect("http://drbccchinducollege.edu.in/departments/shift-ii/b-sc-computer-science/#1546367789848-8d1617d0-47ca").get();
            Elements Updated = DOC.select("section").select("div#1546367789848-8d1617d0-47ca").select("div.wpb_column").select("div.wpb_text_column");
            for(Element data : Updated){
                String name = data.select("h3").text();
                String experience  = data.select("p").select("span:contains(Years of experience)").text();
                String email  = data.select("p:contains(Email)").text();
                FWT.write(StringUtils.trimToEmpty(name)+ "\r\n" + StringUtils.trimToEmpty(experience) + "\r\n" + StringUtils.trimToEmpty(email) + "\r\n\r\n");
            }
            FWT.close();
            
            Elements ImageTag = DOC.select("figure").select("img");
            int counter = 1;
            for(Element image : ImageTag){
                String ImageUrl = image.attr("src");
                FileUtils.copyURLToFile(
                        new URL(ImageUrl),
                        new File("images/"+ counter+ ".png"));
                        counter ++;
            }

              
        } catch (Exception ex) {
            System.out.println("ERROR : HinduCollege : main(String[] args) : " + ex);
        }
    }
}
