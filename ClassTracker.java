import com.mongodb.client.MongoCursor;
import org.jsoup.*;
import org.jsoup.select.*;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.net.UnknownHostException;

import static com.mongodb.client.model.Filters.eq;

public class ClassTracker implements Runnable {

    private ClassTrackerBot classBot;
    private boolean isRunning;

    ClassTracker(ClassTrackerBot classBot) {
        this.classBot = classBot;
        isRunning = true;
    }

    public void run() {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("uniqueCRN");

        while(isRunning) {
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String crn = (String) doc.get("crn");

                    Document newDoc = makeDoc(crn);
                    if (newDoc == null) {
                        throw new NullPointerException();
                    }

                    if (!doc.get("remain").equals(newDoc.get("remain"))) {
                        collection.updateOne(eq("crn", crn), new Document("$set", newDoc));
                        classBot.pushChange(crn);
                    }
                }
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                try {
                    System.out.println("Network error: Waiting 10s . . .");
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
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
                (Integer.parseInt(classInfo[3]) - Integer.parseInt(classInfo[4]));
    }

    public static String[] getInfoArray(String crn) {
        try {
            String[] classInfo = new String[5];

            org.jsoup.nodes.Document doc = Jsoup.connect("https://patriotweb.gmu.edu/pls/prod/bwckschd.p_disp_detail_sched?term_in=201810&crn_in=" + crn).get();
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
        catch (UnknownHostException e) {
            System.out.println("Error: Could not connect to patriotweb.gmu.edu");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*private static String[] getInfoArray(Document doc) {
        String[] classInfo = new String[5];

        classInfo[0] = (String) doc.get("name");
        classInfo[1] = (String) doc.get("cap");
        classInfo[2] = (String) doc.get("remain");
        classInfo[3] = (String) doc.get("wait_cap");
        classInfo[4] = (String) doc.get("wait_remain");

        return classInfo;
    }*/

    public static Document makeDoc(String[] classInfo, String crn) {
        return new Document("crn", crn)
                .append("name", classInfo[0])
                .append("cap", classInfo[1])
                .append("remain", classInfo[2])
                .append("wait_cap", classInfo[3])
                .append("wait_remain", classInfo[4]);
    }

    private static Document makeDoc(String crn) {
        String[] classInfo = getInfoArray(crn);
        if (classInfo == null) return null;
        return makeDoc(classInfo, crn);
    }
}
