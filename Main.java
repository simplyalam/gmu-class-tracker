import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        ClassTrackerBot classBot = new ClassTrackerBot();
        try {
            botsApi.registerBot(classBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        (new Thread(new ClassTracker(classBot))).start();
    }
}
