import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
    private static final MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
    public static final MongoClient mongoClient = new MongoClient(connectionString);

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
