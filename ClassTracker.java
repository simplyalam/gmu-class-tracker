import com.mongodb.client.MongoCursor;
import org.jsoup.nodes.*;
import org.jsoup.*;
import org.jsoup.select.*;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ClassTracker implements Runnable {
    public void run() {
        while(true) {
            MongoCursor<Document> cursor = collection.find().iterator();
            try {
                while (cursor.hasNext()) {
                    System.out.println(cursor.next().toJson());
                }
            } finally {
                cursor.close();
            }
        }
    }

    public static String getInfoString(String crn) {
        String[] classInfo = getInfoArray(crn);
        if (classInfo == null) {
            return "Class not found";
        }
        return classInfo[0] +
                "\nClass Size: " +
                classInfo[1] +
                "\nSeats Remaining: " +
                classInfo[2] +
                "\nWaitlist Size: " +
                classInfo[3] +
                "\nWaitlist Remaining: " +
                classInfo[4];
    }

    public static String[] getInfoArray(String crn) {
        try {
            String[] classInfo = new String[5];

            Document doc = Jsoup.connect("https://patriotweb.gmu.edu/pls/prod/bwckschd.p_disp_detail_sched?term_in=201770&crn_in=" + crn).get();
            // Using CSS like selector syntax, the seat values are retrieved.
            Elements seats = doc.select("table[summary*=seat]  tr:gt(0) td:matches(\\d)");
            // The class information is retrieved.
            // (Class name, CRN, course #, section #)
            Elements classHTML = doc.select("th:contains(" + crn + ")");

            classInfo[0] = classHTML.get(0).html().replace("<br><br>", "");
            classInfo[1] = seats.get(0).html();
            classInfo[2] = seats.get(2).html();
            classInfo[3] = seats.get(3).html();
            classInfo[4] = seats.get(5).html();

            return classInfo;
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
