import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import static java.lang.Math.toIntExact;

public class ClassTrackerBot extends TelegramLongPollingBot {
    boolean toTrack;
    boolean isTracking;
    public ClassTrackerBot() {
        toTrack = false;
        isTracking = false;
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
            System.out.println(toTrack);
            System.out.println(isTracking);
            String text = "";
            if (toTrack == true || message_text.equals("/track")) {
                text += "Tracking . . .\n";
            }
            if (message_text.equals("/help")) { // The help command
                text += "Search for a class: enter the CRN" + "\n" +
                        "Add a class: /track" + "\n" +
                        "Stop tracking: /stop" + "\n" +
                        "Start tracking: /start" + "\n" +
                        "Clear tracker: /clear" + "\n" +
                        "Show tracker: /show" + "\n" + 
                        "Cancel add a class: /cancel";
            } else if (message_text.equals("/stop")) { // Stop all operations.
                text += "All tracking has been stopped.";
                toTrack = false;
                isTracking = false;
            } else if (message_text.matches("\\d{5}")) { // When the text is a 5 digit integer.
                ClassTracker classtracker = new ClassTracker(message_text);
                text += classtracker.getInfo();
                if (toTrack == true && !classtracker.getInfo().equals("Class not found")) {
                    System.out.println(UserData.addClass(toIntExact(user_id), message_text)); // Adds a class to  track.
                    toTrack = false;
                }
            } else if (message_text.equals("/track")) { // Begin tracking.
                text += "Please send a 5-digit CRN";
                toTrack = true;
            } else if (message_text.equals("/cancel")) { // Stops the current tracking operation
                text += "Canceled";
                toTrack = false;
            } else {
                text += "Please send a 5-digit CRN";
            }


            // Create a SendMessage object with chat ID and message.
            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(text);
            try {
                sendMessage(message); // Call method to send the message
                UserData.check(user_first_name, user_last_name, toIntExact(user_id), user_username);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "class_tracker_bot";
    }

    @Override
    public String getBotToken() {
        return "401224780:AAEM-N8JzDugj1pPJRPCMNzM9C64NkNkdew";
    }
}
