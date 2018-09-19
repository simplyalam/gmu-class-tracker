# GMU Class Tracker

Tracks classes by CRN at George Mason University.

The bot communicates with GMU students over the messaging service Telegram. It is integrated with the Telegram API to recieve commands from the user and to send out updates on tracked classes. It holds the tracking information and user information with MongoDB.

## Getting Started

1. Setup a local MongoDB server.
2. Install TelegramBots and JSoup libraries.
3. Input Telegram API key into ClassTrackerBot.java (getBotToken()).
4. Communicate with your bot over Telegram. Use /help to get a list of commands.

### Prerequisites

* [TelegramBots](https://github.com/rubenlagus/TelegramBots)
* [MongoDB](https://www.mongodb.com/)
* [JSoup](https://jsoup.org/)

### Running

/help provides a list of commands

## Built With
* [Java](https://www.java.com/en/)

## Authors

* **Albert Lam** - *All the work* - [simplyalam](https://github.com/simplyalam)

## License

This project is licensed under the GPL License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Thanks to Telegram for a fast messaging service and bot API.
* Thanks to rubenlagus for providing a Telegram API library in Java
* Thanks to MongoDB for developing a quick to set up database system.
* Thanks to JSoup for providing an intuitive and easy to use web scraper.
