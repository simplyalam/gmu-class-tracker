import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.Math.toIntExact;

public class ClassTrackerBot extends TelegramLongPollingBot {
    private boolean toTrack;
    public ClassTrackerBot() {
        toTrack = false;
    }
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

            String user_first_name = update.getMessage().getChat().getFirstName();
            String user_last_name = update.getMessage().getChat().getLastName();
            String user_username = update.getMessage().getChat().getUserName();
            long user_id = update.getMessage().getChat().getId();
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            System.out.println(user_first_name + " " + user_last_name + ": " + message_text);
            String text = "";
            if (toTrack || message_text.equals("/track")) {
                text += "Tracking . . .\n";
            }
            switch (message_text) {
                case "/help":
                    text += "Search for a class: enter the CRN" + "\n" +
                            "Add a class: /track" + "\n" +
                            "Stop tracking: /stop" + "\n" +
                            "Start tracking: /start" + "\n" +
                            "Clear tracker: /clear" + "\n" +
                            "Show tracker: /show" + "\n" +
                            "Cancel add a class: /cancel";
                    break;
                case "/stop":
                    text += "All tracking has been stopped.";
                    toTrack = false;
                    break;
                case "/track":
                    text += "Please send a 5-digit CRN";
                    toTrack = true;
                    break;
                case "/cancel":
                    text += "Canceled";
                    toTrack = false;
                    break;
                default:
                    if (message_text.matches("\\d{5}")) { // When the text is a 5 digit integer.
                        String classInfo = ClassTracker.getInfoString(message_text);
                        text += classInfo;
                        if (toTrack && !classInfo.equals("Class not found")) {
                            System.out.println(UserData.addClass(toIntExact(chat_id), message_text)); // Adds a class to track.
                            toTrack = false;
                        }
                    } else {
                        text += "Please send a 5-digit CRN";
                    }
            }

            // Create a SendMessage object with chat ID and message.
            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(text);
            try {
                execute(message); // Call method to send the message
                UserData.check(user_first_name, user_last_name, toIntExact(user_id), user_username);
            }
            catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "ClassTrackerBot";
    }

    @Override
    public String getBotToken() {
        return "343007403:AAE9JjogzW33e3MLeOUaIa7adzEpLBIzO0M";
    }

    public void pushChange(String crn) {
        MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        try (MongoCursor<Document> cursor = collection.find(eq("crn", crn)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                SendMessage message = new SendMessage() // Create a message object object
                        .setChatId(((Integer) doc.get("chat_id")).toString())
                        .setText(ClassTracker.getInfoString((String) doc.get("crn")));
                execute(message); // Sending our message object to user
            }
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
