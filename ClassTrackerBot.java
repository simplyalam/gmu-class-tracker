import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.mongodb.client.model.Filters.eq;
import static java.lang.Math.toIntExact;

public class ClassTrackerBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {

            String user_first_name = update.getMessage().getChat().getFirstName();
            String user_last_name = update.getMessage().getChat().getLastName();
            String user_username = update.getMessage().getChat().getUserName();
            String user_id = Integer.toString(toIntExact(update.getMessage().getChat().getId()));
            String message_text = update.getMessage().getText();
            String chat_id = Integer.toString(toIntExact(update.getMessage().getChatId()));

            log(user_first_name, user_last_name, user_id, chat_id, message_text);
            UserData.check(user_first_name, user_last_name, user_id, user_username);

            String text = "";
            if (message_text.equals("/help")) {
                text += "Helpful commands:" + "\n" +
                        "Add a class: /track {5-digit CRN}" + "\n" +
                        "Start tracking: /start" + "\n" +
                        "Clear tracker: /clear" + "\n" +
                        "Show tracker: /show";
            } else if (message_text.matches("/track \\d{5}")) { // When the text is a 5 digit integer.
                trackingMsg(chat_id);

                String crn = message_text.substring(7, 12);
                text +=  ClassTracker.getInfoString(crn);

                UserData.addClass(chat_id, crn);
            } else {
                text += "Type /help for more info.";
            }

            // Create a SendMessage object with chat ID and message.
            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(text);
            try {
                execute(message); // Call method to send the message
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
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
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

    private void trackingMsg(String chat_id) {
        SendMessage message = new SendMessage()
                .setChatId(chat_id)
                .setText("Tracking . . .");
        try {
            execute(message); // Call method to send the message
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void log(String first_name, String last_name, String user_id, String chat_id, String txt) {
        System.out.println("\n----------------------------");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        System.out.println("User: " + first_name + " " + last_name + " (user_id = " + user_id + ") (chat_id = " + chat_id + ")\n" + "Message: " + txt);
    }
}
