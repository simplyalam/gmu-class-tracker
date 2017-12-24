import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
// import org.json.JSONObject;

public class UserData {
    private static final MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
    private static final MongoClient mongoClient = new MongoClient(connectionString);

    public static boolean check(String first_name, String last_name, int user_id, String username) {
        MongoDatabase database = mongoClient.getDatabase("userDatabase");
        MongoCollection<Document> collection = database.getCollection("users");

        // Checks if the user is already in the database. Returns a positive number if the user is already in the database.
        long found = collection.count(Document.parse("{user_id : " + Integer.toString(user_id) + "}"));

        if (found == 0) {
            Document doc = new Document("first_name", first_name)
                    .append("last_name", last_name)
                    .append("user_id", user_id)
                    .append("username", username);
            collection.insertOne(doc);
            System.out.println("User does not exists in database. Written.");
            return false;
        } else {
            System.out.println("User exists in database.");
            return true;
        }
    }

    public static boolean addClass(int chat_id, String crn) {
        MongoDatabase database = mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        if (!validCRN(crn)) {
            return false;
        }
        long found = collection.count(Document.parse("{chat_id : " + Integer.toString(chat_id) + "," + "crn : " + crn + "}"));
        if (found == 0) {
            UserData.checkUnique(crn);
            Document doc = new Document("chat_id", chat_id)
                    .append("crn", crn);
            collection.insertOne(doc);
            System.out.println("Tracking new CRN.");
            return true;
        } else {
            System.out.println("CRN already exists.");
            return false;
        }
    }

    public static boolean checkUnique(String crn) {
        MongoDatabase database = mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("uniqueCRN");


        long found = collection.count(Document.parse("{crn : \"" + crn + "\"}"));
        if (found > 0) {
            System.out.println("The class already exists");
            return false;
        }

        String[] classInfo = ClassTracker.getInfoArray(crn);
        if (classInfo == null) {
            System.out.println("Error: could not find class");
            return false;
        }

        Document doc = ClassTracker.makeDoc(classInfo, crn);
        collection.insertOne(doc);
        return true;
    }

    private static boolean validCRN(String crn) {
        return crn != null && crn.length() == 5 && ClassTracker.getInfoArray(crn) != null;
    }
}
