import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;

public class MongoData {

    public static void check(String first_name, String last_name, String user_id, String username) {
        MongoDatabase database = Main.mongoClient.getDatabase("userDatabase");
        MongoCollection<Document> collection = database.getCollection("users");

        // Checks if the user is already in the database. Returns a positive number if the user is already in the database.
        Document query = new Document("user_id", user_id);
        long found = collection.count(query);

        if (found == 0) {
            Document doc = new Document("first_name", first_name)
                    .append("last_name", last_name)
                    .append("user_id", user_id)
                    .append("username", username);
            collection.insertOne(doc);
            System.out.println("NEW USER = TRUE");
        } else {
            System.out.println("NEW USER = FALSE");
        }
    }

    public static boolean addClass(String chat_id, String crn) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        Document query = new Document("chat_id", chat_id).append("crn", crn);
        long found = collection.count(query);

        if (found == 0) {
            MongoData.checkUnique(crn);
            Document doc = new Document("chat_id", chat_id)
                    .append("crn", crn);
            collection.insertOne(doc);
            System.out.println("ADDED: " + crn);
            return true;
        } else {
            System.out.println("DUPLICATE: " + crn);
            return false;
        }
    }

    public static boolean removeClass(String chat_id, String crn) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        Document query = new Document("chat_id", chat_id).append("crn", crn);
        long found = collection.count(query);

        if (found > 0) {
            collection.deleteOne(and(eq("chat_id", chat_id), eq("crn", crn)));
            System.out.println("REMOVED: " + crn);

            if (collection.count(new Document("crn", crn)) == 0) {
                removeUnique(crn);
                System.out.println("REMOVED UNIQUE");
            }
            return true;
        } else {
            System.out.println("DOES NOT EXIST: " + crn);
            return false;
        }
    }

    public static void removeAll(String chat_id) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        try (MongoCursor<Document> cursor = collection.find(eq("chat_id", chat_id)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String crn = (String) doc.get("crn");

                removeClass(chat_id, crn);
            }
        }
    }

    private static void checkUnique(String crn) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("uniqueCRN");

        Document query = new Document("crn", crn);
        long found = collection.count(query);
        String[] classInfo = ClassTracker.getInfoArray(crn);

        if (found > 0) {
            System.out.println("UNIQUE CRN = FALSE");
        } else if (classInfo == null) {
            System.out.println("Error: could not find class");
        } else {
            Document doc = ClassTracker.makeDoc(classInfo, crn);
            collection.insertOne(doc);
            System.out.println("UNIQUE CRN = TRUE");
        }
    }

    private static void removeUnique(String crn) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("uniqueCRN");

        collection.deleteMany(eq("crn", crn));
    }

    public static boolean validCRN(String crn) {
        return crn != null && crn.length() == 5 && ClassTracker.getInfoArray(crn) != null;
    }

    public static ArrayList<String> getClasses(String chat_id) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");
        ArrayList<String> crns = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find(eq("chat_id", chat_id)).iterator()) {
            while (cursor.hasNext()) {
                crns.add((String) cursor.next().get("crn"));
            }
        }
        return crns;
    }
}
