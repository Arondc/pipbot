# Deutsche Anleitung 
## Pipbot - Ein Multichannel Kotlin (twitch) Chatbot
### Einrichtung des Bots
Um deine eigene Version des Pipbots zu starten brauchst du:
* JVM 21 oder neuer
* Einen oauth-Token für den Account den dein Bot nutzen soll

1. Kopiere das Release des Bots in einen Ordner deiner Wahl
2. authentication.yml einrichten
   * Die Vorlage `authentication_template.yml` in den gleichen Ordner herunterladen 
   * Die Vorlage in `authentication.yml` umbenennen
   * Mit einem Texteditor die folgenden Werte anpassen 
     * Den Nutzernamen deines Bots in `twitch-connector.authentication.user` setzen
     * Den oauth-Token für den Account deines Bots in `twitch-connector.authentication.oAuth` setzen
3. Der Bot kann über die Kommandozeile mit den folgenden Befehl gestartet werden
```shell
java -jar pipbot-0.0.1-SNAPSHOT.jar
```
4. Sobald der Bot gestartet ist kann die Oberfläche über [\<Link zur Oberfläche>](http://localhost:8080) erreicht werden

### Konfiguration deines Kanals im Bot

Um einen Kanal in deinem Bot zu registrieren kannst du [\<Kanäle>](http://localhost:8080/channels) nutzen.
Hier siehst du alle konfigurierten Kanäle deines Bots.
Um einen neuen Kanal zu registrieren fülle einfach den unteren Bereich aus und klicke auf `Erstellen/Anpassen`.

| Feld               | Bedeutung                                                                                                                                       | Hinweise                                          |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| Kanalname          | Der Name des Kanals dem der Bot beitreten soll                                                                                                  |                                                   |
| Sprache            | Die Sprache die der Bot im jeweiligen Kanal für Textnachrichten nutzen soll (aktuell nur "de" oder "en"                                         |                                                   |
| Shoutout           | Das Verhalten des Bots bei einem Raid                                                                                                           |                                                   |
|                    | Aus = Der Bot reagiert nicht auf Raids                                                                                                          |                                                   |
|                    | Textantwort = Der Bot sendet eine Wilkommensnachricht in den Chat                                                                               |                                                   |
|                    | StreamElements shoutout = Der Bot nutzt den !so Befehl von StreamElements (nur wenn du auch den StreamElements Bot auch für deinen Kanal nutzt) |                                                   |
|                    | Twitch shoutout = Der Bot nutzt den /shoutout Befehl von Twitch um auf einen Raid zu reagieren                                                  | Der Bot muss hierfür Mod im jeweiligen Kanal sein |
| Shoutout whitelist | Hier kannst du eingrenzen welche Raids der Bot automatisch beantworten soll                                                                     |                                                   |
|                    | Im Feld können mit Komma getrennt die Kanalnamen aufgezählt werden die vom Bot automatisch beantwortet werden sollen.                           |                                                   |
|                    | Ist das Feld leer werden alle Raids vom Bot beantwortet.                                                                                        |                                                   |

Nach dem Erstellen der Kanalkonfiguration tritt der Bot automatisch dem jeweiligen Kanal bei.

Bereits konfigurierte Kanäle siehst du in der oberen Tabelle. Dort kannst du einzelne Kanäle auch zeitweise deaktivieren, die Einstellungen eines Kanals zum Bearbeiten ins untere Formular laden oder den Kanal ganz löschen.
Beachte, dass das Löschen eines Kanals auch alle zugehörigen Einträge in der Datenbank des Bots löscht.

## Befehle

Pipbot besitzt eine Reihe vorgefertigter Befehle.

| Befehl                                  | Beschreibung                                                                                                          |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| !reverse \<text>                        | Kehrt den eingegeben Text um. Aus "Text" wird so "txeT                                                                |
| !meme \<text>                           | Erfasst den Text als Meme in der Datenbank                                                                            |
| !merge                                  | Fügt den aktuellen Stream und den vorhergehenden Stream zusammen (Nützlich, wenn ein Stream eine Unterbrechung hatte) |
| **Zitate**                              |                                                                                                                       |
| !zitat add \<text>                      | Erfasst ein neues Zitat                                                                                               |
| !zitat delete \<nummer>                 | Löscht das Zitat mit der entsprechenden Nummer                                                                        |
| !zitat \<nummer>                        | Sucht das Zitat mit der entsprechenden Nummer                                                                         |
| !zitat \<text>                          | Sucht ein Zitat mit dem entsprechenden Textausschnitt                                                                 |
| **Umfragen**                            | _Die unten aufgeführten Parameter können beliebig kombiniert werden_                                                  |
| !poll "\<text>"                         | Erstellt eine Schnellumfrage mit dem gegebenen Titel (Dauer: 3 Minuten, Antwortoptionen: 1,2)                         |
| !poll options=Ja,Nein,Vielleicht "text" | Erstellt eine Umfrage mit den angegebenen Optionen (Dauer: 3 Minuten, Antwortoptionen: Ja,Nein,Vielleicht)            |
| !poll time=5m "text"                    | Erstellt eine Umfrage mit der angegeben Dauer (Dauer: 5 Minuten, Antwortoptionen: 1,2)                                |
| !poll open=true "text"                  | Erstellt eine offene Umfrage (Dauer: 3 Minuten, Beliebige Antworten möglich )                                         |
| ?\<antwort>                             | Übermittelt eine Antwort für eine Umfrage an den Bot (Wenn ein Zuschauer eine Umfrage beantworten möchte)             |



# English

## Pipbot - A multichannel Kotlin (twitch) Chatbot
Disclaimer: The bots surface and command list is currently german only. An english version is already planned.

### Setting up the bot
You need the following prerequisites:
* JVM 21 oder newer
* An oauth-Token for the account the bot will use

1. Copy the release jar to a folder of your choice
2. Set up authentication.yml 
    * Download the template `authentication_template.yml` into the same folder as the release
    * Rename the template file to `authentication.yml`
    * Edit the following values in the file
        * Set `twitch-connector.authentication.user` to the user name the bot should use
        * Set `twitch-connector.authentication.oAuth` with the oauth token of your bot's account
3. The bot can be started via the command line
```shell
java -jar pipbot-0.0.1-SNAPSHOT.jar
```
4. As soon as the bot started you can reach the gui via [\<Link to gui>](http://localhost:8080)

### Configuring your channel
To register your channel within the bot you can open [\<channels>](http://localhost:8080/channels).
Here you can see all configured channels of your bot. 
To register a new channel just fill out the form in the lower part of the page and klick `Erstellen/Anpassen`

| Feld               | Bedeutung                                                                                                                           | Hinweise                                                |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| Kanalname          | The name of the channel your bot should join                                                                                        |                                                         |
| Sprache            | The language your bot should use within the channel (currently only "de" or "en"                                                    |                                                         |
| Shoutout           | How the bot responds to a raid                                                                                                      |                                                         |
|                    | Aus = The bot doesn't respond to raids at all                                                                                       |                                                         |
|                    | Textantwort = The bot sends a text greeting to the chat                                                                             |                                                         |
|                    | StreamElements shoutout = The bot uses the StreamElements !so command (only if StreamElements is also configured for that channel ) |                                                         |
|                    | Twitch shoutout = The bot uses twitch's /shoutout command to respond to a raid                                                      | The bot needs to be a moderator of the channel to do so |
| Shoutout whitelist | Restricts the bot's response on raiding channels to channels you trust                                                              |                                                         |
|                    | You can give a list of comma separated channel names here. The bot will then only respond to raids from these channels.             |                                                         |
|                    | You can leave the field empty if the bot should respond to all raids.                                                               |                                                         |

After creating the configuration for a channel the bot automatically joins the given channel.

Already configured channels are visible in the table in the upper part of the page. You can deactivate channels by using the deactivate/activate links. You can edit channel configurations by clicking the edit link and submitting the form afterward. You can also delete a channel completely. This will also delete related data from the bot, so use it with caution.

## Commands

Pipbot offers a range of predefined commands.

| Befehl                            | Beschreibung                                                                                                |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------|
| !reverse \<text>                  | Reverses the input text. "Text" -> "txeT"                                                                   |
| !meme \<text>                     | Saves the given text as meme                                                                                |
| !merge                            | Merges the current stream and the last one. This is helpful if a stream was interrupted.                    |
| **Quotes (Zitate)**               |                                                                                                             |
| !zitat add \<text>                | Saves a new quote                                                                                           |
| !zitat delete \<number>           | Deleted the quote with the given number                                                                     |
| !zitat \<number>                  | Searches the quote with the given number                                                                    |
| !zitat \<text>                    | Searches a quote with the given text (if there is more than one a random one with the text will be picked)  |
| **Polls**                         | _The parameters presented below can be combined_                                                            |
| !poll "\<text>"                   | Creates a quick poll with the given text (Duration: 3 Minutes, Possible answers: 1,2)                       |
| !poll options=Yes,No,Maybe "text" | Creates a poll with the given possible answer options (Duration: 3 Minutes, Possible answers: Yes,No,Maybe) |
| !poll time=5m "text"              | Creates a poll with the given duration (Duration: 5 Minutes, Possible answers: 1,2)                         |
| !poll open=true "text"            | Creates an open poll (Duration: 3 Minuten, Takes all answers given by users  )                              |
| ?\<answer>                        | Vote for an answer (Used to participate in a vote)                                                          |


# Developer information (english only)

WIP - The bot is still in development and is shared as is.

## Writing own modules

1. Add listener for event to react to

| Direction | Event            | Raised by       | Published when...                      |
|-----------|------------------|-----------------|----------------------------------------|
| In        | TwitchMessage    | TwitchConnector | Twitch messages are incoming from chat |
| In        | TwitchRaidEvent  | TwitchConnector | Twitch raid is incoming                |
| Out       | SendMessageEvent | PipBot          | Sending Messages to Twitch             |

2. process the event
~~~Kotlin
@ApplicationModuleListener
fun processEvent(message: TwitchMessage) {
    //processing...
}
~~~ 

3. optional: Publish a response event
~~~Kotlin
class YourProcessingClass(val publisher : ApplicationEventPublisher) {
    @ApplicationModuleListener
    fun processEvent(message: TwitchMessage) {
        //processing...
        publisher.publishEvent(SendMessageEvent(channel, message))
    }
}
~~~
