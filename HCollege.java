import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import netscape.javascript.JSObject;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONString;

import java.util.ArrayList;
import java.util.Collection;
import java.io.File;
import java.io.FileWriter;
import org.apache.commons.io.FileUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class HCollege {
    static String QUERY = "";
    static String TEXTSEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?key=AIzaSyB4W5NEuoc-aluesvnz6kmc8LAEP3tZ5Rk";
    static DefaultListModel JSONLISTMODEL = new DefaultListModel();

    public static void ScreenDesign() {
        try {
            Dimension SCREENSIZE = Toolkit.getDefaultToolkit().getScreenSize();
            int FRAMEWIDTH = SCREENSIZE.width;
            int FRAMEHEIGHT = SCREENSIZE.height;

            JFrame.setDefaultLookAndFeelDecorated(true);
            JFrame MAINFRAME = new JFrame("Hindu College");
            MAINFRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            MAINFRAME.setResizable(false);

            Container BORDER = MAINFRAME.getContentPane();

            JPanel SCREEN = new JPanel();
            SCREEN.setPreferredSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));
            SCREEN.setLayout(new FlowLayout(5, 5, 5));

            JPanel MAINSCREEN = new JPanel();
            MAINSCREEN.setPreferredSize(new Dimension(FRAMEWIDTH - 15, FRAMEHEIGHT - 85));
            MAINSCREEN.setBorder(BorderFactory.createTitledBorder("Screen"));
            MAINSCREEN.setLayout(new FlowLayout(5, 5, 5));
            MAINSCREEN.setBackground(new Color(255, 255, 255));

            JPanel BOTTOMSCREEN = new JPanel();
            BOTTOMSCREEN.setPreferredSize(new Dimension(FRAMEWIDTH - 15, 70));
            BOTTOMSCREEN.setLayout(new FlowLayout(5, 5, 5));
            BOTTOMSCREEN.setBackground(new Color(255, 255, 255));

            JTextField SEARCHTEXT = new JTextField("");
            SEARCHTEXT.setPreferredSize(new Dimension(FRAMEWIDTH - 400, 30));
            BOTTOMSCREEN.add(SEARCHTEXT);

            String BName = "Get";
            JButton BUTTON = new JButton(BName);
            BUTTON.setPreferredSize(new Dimension(130, 30));
            BUTTON.setActionCommand(BName);
            BUTTON.setForeground(new Color(255, 255, 255));
            BUTTON.setFont(new Font("Arial", Font.PLAIN, 16));
            BUTTON.setBackground(new Color(30, 144, 255));

            BUTTON.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    BUTTON.setEnabled(false);

                    File file = FileUtils.getFile("details.txt");

                    FileUtils.deleteQuietly(file);

                    String QUERY = SEARCHTEXT.getText();
                    Crawl(TEXTSEARCH_URL + "&query=" + QUERY.replaceAll(" ", "%20"));

                    JSONArray DATABASE = getAddressFile();

                    for (int i = 0; i < DATABASE.length(); i++) {
                        JSONObject JSONOBJECT = (JSONObject) DATABASE.getJSONObject(i);
                        JSONLISTMODEL.addElement(JSONOBJECT.get("name") + " ( " + JSONOBJECT.get("address") + " ) ");

                    }
                    BUTTON.setEnabled(true);

                }
            });

            JList JSONLIST = new JList(JSONLISTMODEL);
            JScrollPane JSONSCROLL = new JScrollPane(JSONLIST);
            JSONSCROLL.setPreferredSize(new Dimension(FRAMEWIDTH - 45, FRAMEHEIGHT - 110));
            JSONSCROLL.setBorder(null);
            JSONSCROLL.setForeground(new Color(0, 0, 0));
            MAINSCREEN.add(JSONSCROLL);

            SCREEN.add(MAINSCREEN);

            BOTTOMSCREEN.add(BUTTON);
            SCREEN.add(BOTTOMSCREEN);

            BORDER.add(SCREEN);

            MAINFRAME.pack();
            MAINFRAME.setLocation(0, 0);
            MAINFRAME.setSize(FRAMEWIDTH, FRAMEHEIGHT);
            MAINFRAME.setVisible(true);
        } catch (Exception ex) {
            System.out.println("ERROR : HCollege : ScreenDesign() : " + ex);
        }
    }

    public static void Crawl(String funcData) {
        try {
            QUERY = funcData;
            Document DOCUMENT = Jsoup.connect(QUERY).ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                    .referrer("http://www.google.com").ignoreHttpErrors(true).get();
            String JSONSTRING = DOCUMENT.select("body").text();

            JSONObject JSONOBJECT = new JSONObject(JSONSTRING);

            GetResults(JSONOBJECT);

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void GetResults(JSONObject JSONOBJECT) {
        try {
            String STATUS = JSONOBJECT.get("status").toString();
            String ADDRESS = "";
            String NAME = "";
            if (STATUS.trim().equals("OK")) {
                JSONArray RESULTS = (JSONArray) JSONOBJECT.get("results");

                for (int x = 0; x < RESULTS.length(); x++) {
                    String TYPES_TEXT = RESULTS.getJSONObject(0).getJSONArray("types").toString();
                    String[] TYPES = { "point_of_interest" };

                    if (StringUtils.containsAny(TYPES_TEXT, TYPES)) {
                        ADDRESS = RESULTS.getJSONObject(x).getString("formatted_address");
                        NAME = RESULTS.getJSONObject(x).getString("name");

                    } else {
                        ADDRESS = "NO_ADDRESS";
                        NAME = "NAME";

                    }

                    ADDRESS = StringUtils.trimToEmpty(ADDRESS);
                    if (ADDRESS != "NO_ADDRESS" && NAME != "NAME") {
                        FileUtils.writeStringToFile(new File("details.txt"),
                                ADDRESS + "\r\n" + NAME + "\r\n\r\n", StandardCharsets.UTF_8, true);

                    }
                }

                if (JSONOBJECT.has("next_page_token")) {
                    Thread.sleep(2000);
                    Crawl(TEXTSEARCH_URL + "&query=" + QUERY.replaceAll(" ", " ") + "&pagetoken="
                            + JSONOBJECT.getString("next_page_token"));
                }
            }
        } catch (Exception ex) {
            System.out.println("ERROR : GetResults(HashMap IDS, String url) : " + ex);
        }
    }

    public static JSONArray getAddressFile() {

        JSONArray JSONARRAY = new JSONArray();

        try {
            String DATA = FileUtils.readFileToString(new File(String.valueOf("details.txt")), "UTF-8");
            String[] SCRAP_DATA = StringUtils.trimToEmpty(DATA).split("\n\r");

            for (int x = 0; x < SCRAP_DATA.length; x++) {

                String[] DATAARRAY = SCRAP_DATA[x].split("\r\n");
                String NAME = DATAARRAY[1];
                String ADDRESS = DATAARRAY[0];

                JSONObject OBJECT_DATA = new JSONObject();
                OBJECT_DATA.put("name", NAME);
                OBJECT_DATA.put("address", ADDRESS);
                JSONARRAY.put(OBJECT_DATA);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONARRAY;
    }

    public static void main(String[] args) {
        try {
            ScreenDesign();
        } catch (Exception ex) {
            System.out.println("ERROR : HCollege : main(String[] args) : " + ex);
        }
    }
}
