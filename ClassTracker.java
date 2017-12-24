import com.mongodb.client.MongoCursor;
import org.jsoup.*;
import org.jsoup.select.*;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ClassTracker implements Runnable {
    private static final MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
    private static final MongoClient mongoClient = new MongoClient(connectionString);

    private ClassTrackerBot classBot;
    private boolean isRunning;

    public ClassTracker(ClassTrackerBot classBot) {
        this.classBot = classBot;
        isRunning = true;
    }

    public void run() {
        MongoDatabase database = mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("uniqueCRN");

        while(isRunning) {
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    String crn = (String) doc.get("crn");
                    
                    String[] newInfo = getInfoArray(crn);
                    String[] oldInfo = getInfoDoc(doc);
                    if (newInfo != null && !newInfo[2].equals(oldInfo[2])) {
                        Document newDoc = makeDoc(newInfo, crn);

                        collection.updateOne(Document.parse("{crn : \"" + crn + "\"}"), newDoc);
                        classBot.pushChange(crn);
                    }
                }
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*
            Document doc = new Document("crn", crn)
                .append("name", classInfo[0])
                .append("cap", classInfo[1])
                .append("remain", classInfo[2])
                .append("wait_cap", classInfo[3])
                .append("wait_remain", classInfo[4]);
             */
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
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getInfoDoc(Document doc) {
        String[] classInfo = new String[5];

        classInfo[0] = (String) doc.get("name");
        classInfo[1] = (String) doc.get("cap");
        classInfo[2] = (String) doc.get("remain");
        classInfo[3] = (String) doc.get("wait_cap");
        classInfo[4] = (String) doc.get("wait_remain");

        return classInfo;
    }

    public static Document makeDoc(String[] classInfo, String crn) {
        return new Document("crn", crn)
                .append("name", classInfo[0])
                .append("cap", classInfo[1])
                .append("remain", classInfo[2])
                .append("wait_cap", classInfo[3])
                .append("wait_remain", classInfo[4]);
    }
}
