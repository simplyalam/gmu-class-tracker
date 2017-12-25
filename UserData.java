import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

public class UserData {

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

    public static void addClass(String chat_id, String crn) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        if (!validCRN(crn)) {
            System.out.println("Error: invalid CRN");
        } else {
            Document query = new Document("chat_id", chat_id).append("crn", crn);
            long found = collection.count(query);

            if (found == 0) {
                UserData.checkUnique(crn);
                Document doc = new Document("chat_id", chat_id)
                        .append("crn", crn);
                collection.insertOne(doc);
                System.out.println("NEW CRN = TRUE");
            } else {
                System.out.println("NEW CRN = FALSE");
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

    private static boolean validCRN(String crn) {
        return crn != null && crn.length() == 5 && ClassTracker.getInfoArray(crn) != null;
    }
}
