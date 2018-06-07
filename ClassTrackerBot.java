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
import java.util.ArrayList;
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
            MongoData.check(user_first_name, user_last_name, user_id, user_username);

            String text = "";
            if (message_text.equals("/help")) {
                text = "Commands:" + "\n" +
                        "Add:    /add {5-digit CRN}" + "\n" +
                        "Remove: /remove {5-digit CRN}" + "\n" +
                        "Remove all: /removeall" + "\n" +
                        "Show all:   /show" + "\n" +
                        "More help: /help {command}";
            } else if (message_text.equals("/show")) {
                ArrayList<String> crns = MongoData.getClasses(chat_id);
                for (String crn : crns) {
                    String classInfo = ClassTracker.getInfoString(crn);

                    SendMessage message = new SendMessage()
                            .setChatId(chat_id)
                            .setText(classInfo);
                    try {
                        execute(message); // Call method to send the message
                    }
                    catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                return;
            } else if (message_text.equals("/removeall")) {
                removingMsg(chat_id, true);

                MongoData.removeAll(chat_id);
                text = "Removed all classes.";
            } else if (message_text.substring(0,4).equals("/add")) { // When the text is a 5 digit integer.
                trackingMsg(chat_id);

                String[] values = message_text.trim().split("\\s+");

                if (values.length < 2) {
                    text =  "Each CRN must be 5 digits long.\n" +
                            "Separate each CRN to be added with a space.\n" +
                            "/add {crn 1} {crn 2} . . .";
                } else {
                    for (int i = 1; i < values.length; i++) {
                        String crn = values[i];
                        if (MongoData.validCRN(crn)) {
                            if (MongoData.addClass(chat_id, crn)) {
                                text += "Added: " + crn + "\n";
                            } else {
                                text += "Duplicate: " + crn + "\n";
                            }
                        } else {
                            text += "Invalid: " + crn + "\n";
                            break;
                        }
                    }
                }
            } else if (message_text.substring(0,7).equals("/remove")) {
                removingMsg(chat_id, false);

                String[] values = message_text.trim().split("\\s+");

                if (values.length < 2) {
                    text = "Each CRN must be 5 digits long.\n" +
                            "Separate each CRN to be added with a space.\n" +
                            "/remove {crn 1} {crn 2} . . .";
                } else {
                    for (int i = 1; i < values.length; i++) {
                        String crn = values[i];
                        if (MongoData.validCRN(crn)) {
                            if (MongoData.removeClass(chat_id, crn)) {
                                text += "Removed: " + crn + "\n";
                            } else {
                                text += "Does not exist: " + crn + "\n";
                            }
                        } else {
                            text += "Invalid: " + crn + "\n";
                            break;
                        }
                    }
                }
            } else {
                text = "Type /help for more info.";
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
        return "TELEGRAM BOT TOKEN HERE";
    }

    public void pushChange(String crn) {
        MongoDatabase database = Main.mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");

        Document query = new Document("crn", crn);
        if (collection.count(query) == 0) return;

        try (MongoCursor<Document> cursor = collection.find(eq("crn", crn)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                SendMessage message = new SendMessage() // Create a message object object
                        .setChatId((String) doc.get("chat_id"))
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

    private void removingMsg(String chat_id, boolean isAll) {
        String text;
        if (isAll) {
            text = "Removing all . . .";
        } else {
            text = "Removing . . .";
        }

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

    private void log(String first_name, String last_name, String user_id, String chat_id, String txt) {
        System.out.println("\n----------------------------");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        System.out.println("User: " + first_name + " " + last_name + " (user_id = " + user_id + ") (chat_id = " + chat_id + ")\n" + "Message: " + txt);
    }
}
