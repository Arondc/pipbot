**Table of contents**
<!-- TOC -->
* [Pipbot - A multichannel Twitch Chatbot written in Kotlin](#pipbot---a-multichannel-twitch-chatbot-written-in-kotlin)
  * [Build from sources](#build-from-sources)
  * [Setting up the bot](#setting-up-the-bot)
  * [Configuring your channel](#configuring-your-channel)
  * [Features](#features)
    * [Stream recognition](#stream-recognition)
    * [Meme recognition & browser source](#meme-recognition--browser-source)
      * [Meme recording](#meme-recording)
      * [Meme browser source](#meme-browser-source)
        * [Examples](#examples)
        * [Meme browser source monitor](#meme-browser-source-monitor)
    * [Quotes](#quotes)
    * [Raid reactions](#raid-reactions)
    * [Polls](#polls)
    * [Autoresponder](#autoresponder)
    * [Automod](#automod)
    * [Other commands](#other-commands)
* [Developer information](#developer-information)
  * [Writing own modules](#writing-own-modules)
    * [Add listener for event to react to](#add-listener-for-event-to-react-to)
    * [Process the event](#process-the-event)
    * [Publish a response event (optional)](#publish-a-response-event-optional)
<!-- TOC -->

# Pipbot - A multichannel Twitch Chatbot written in Kotlin
Disclaimer: The bots surface and command list is currently german only. An english version is already planned.

## Build from sources

1. Clone the git repository onto your own machine
2. Make sure you have a Java JDK 21 or newer installed on your machine
3. Make sure your have the Java installation set as JAVA_HOME environment variable or in your PATH
4. Run
```shell
./gradlew assemble
```
5. The build should be available in the ``build/libs`` directory

## Setting up the bot
You need the following prerequisites:
* JVM 21 oder newer
* An oauth-Token for the account the bot will use

1. Copy the release jar to a folder of your choice
2. Set up authentication.yml 
    * Download the template `authentication_template.yml` into the same folder as the release
    * Rename the template file to `authentication.yml`
    * Edit the following values in the file
        * Set `twitch-connector.authentication.user` to the username the bot should use
        * Set `twitch-connector.authentication.oAuth` with the oauth token of your bot's account
3. The bot can be started via the command line
```shell
java -jar pipbot-0.0.1-SNAPSHOT.jar
```
4. As soon as the bot started you can reach the gui via [\<Link to gui>](http://localhost:8080)

## Configuring your channel
To register your channel within the bot you can open the [\<channels configuration page>](http://localhost:8080/channels).
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

## Features
(This list may currently not be complete)

To use any of the feature in a twitch channel the bot needs a configuration for the given channel.
Simply add the channel via the [\<channel configuration page\>](http://localhost:8080/channels).

### Stream recognition
(documentation is WIP)

| Command                           | Description                                                                                                 |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------|
| !merge                            | Merges the current stream and the last one. This is helpful if a stream was interrupted.                    |

### Meme recognition & browser source
There are two processes handling Memes:
1. Recording Memes and storing them to the bots database automatically
2. Providing a browser source to allow showing meme images directly within a stream

#### Meme recording
Recording a meme is as simple as writing a message into the chat of a connected channel.
There are different ways to record a meme.

* Links to imgflip, YouTube and twitch clips are automatically recognized and stored as meme into the database.
* Chat messages starting with `!meme ` will also be recognized and stored into the meme database. This allows texts or other links to be stored into the database.

Whenever a meme is recorded for a  channel the information about the meme is stored to the database.
This includes the channel name, the time the meme was sent, the user that sent it, the stream it was sent in as well as a link (if applicable). You can see the complete list of recorded memes in the [meme view](http://localhost:8080/memes).

| Command                           | Description                                                                                                 |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------|
| !meme \<text>                     | Saves the given text as meme                                                                                |


#### Meme browser source
The bot currently supports providing imgflip memes (jpg & gif) directly as a browser source. This allows showing memes in your stream by simply setting up another browser source (similarly as the stream overlay you may already use).

To set up the browser source, add it to your OBS scene with the following settings

| Configuration | Value                                                                                |
|---------------|--------------------------------------------------------------------------------------|
| URL           | `http://localhost:8080/browser/memes/<your channel name>`                            |
| Width         | 600 (the bot currently supports only this with and will scale images accordingly)    |
| Height        | The height of your stream (to use the whole screen in case the images are very high) |

There are optional URL parameters you can provide to change the behaviour of the meme browser source.

``imgWidth`` - Changes the default width of the shown image in the browser source

``address`` - Changes the default server to use for requesting new memes in the browser source

##### Examples

| Example urls                                                                                | Used settings                                                  |
|---------------------------------------------------------------------------------------------|----------------------------------------------------------------|
| `http://localhost:8080/browser/memes/mychannel`                                             | imgWidth=600px (default)<br/> address=localhost:8080 (default) |
| `http://localhost:8080/browser/memes/mychannel?imgWidth=300px`                              | imgWidth=300px<br/> address=localhost:8080  (default)          |
| `http://localhost:8080/browser/memes/mychannel?address=123.123.123.123:1234`                | imgWidth=600px (default)<br/> address=123.123.123.123:1234     |
| `http://localhost:8080/browser/memes/mychannel?imgWidth=100px&address=123.123.123.123:1234` | imgWidth=100px<br/> address=123.123.123.123:1234               |

Example configuration in OBS using the default width (Breite) and address

![img.png](doc/img.png)

Please make sure that your source is not squished in OBS by shift dragging the anchor points, else your memes will look squished too.

##### Meme browser source monitor

As the meme browser source is only showing the memes for about 10 seconds, it may be helpful to have a URL to monitor the memes for yourself that always shows the latest shown meme on your channel.

To access this monitor your can use `http://localhost:8080/browser/memes/<your channel name>/monitor`.
This view will update every second and will always show the latest meme.

The browser source monitor supports the same URL parameters as the meme browser source itself. 

``imgWidth`` - Changes the default width of the shown image in the browser source

``address`` - Changes the default server to use for requesting the last shown meme in the browser source


### Quotes
(documentation is WIP)

| Command                           | Description                                                                                                 |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------|
| !zitat add \<text>                | Saves a new quote                                                                                           |
| !zitat delete \<number>           | Deleted the quote with the given number                                                                     |
| !zitat \<number>                  | Searches the quote with the given number                                                                    |
| !zitat \<text>                    | Searches a quote with the given text (if there is more than one a random one with the text will be picked)  |

### Raid reactions
(documentation is WIP)

### Polls
(documentation is WIP)

| Command                           | Description                                                                                                 |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------|
| `!poll "text"`                    | Creates a quick poll with the given text (Duration: 3 Minutes, Possible answers: 1,2)                       |
| `!poll options=Yes,No,Maybe text` | Creates a poll with the given possible answer options (Duration: 3 Minutes, Possible answers: Yes,No,Maybe) |
| `!poll time=5m text`              | Creates a poll with the given duration (Duration: 5 Minutes, Possible answers: 1,2)                         |
| `!poll open=true text`            | Creates an open poll (Duration: 3 Minuten, Takes all answers given by users  )                              |
| `?<answer>`                       | Vote for an answer (Used to participate in a vote)                                                          |


### Autoresponder
(documentation is WIP)

### Automod
(documentation is WIP)


### Other commands

Other commands Pipbot offers.

| Command                           | Description                                                                                                 |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------|
| !reverse \<text>                  | Reverses the input text. "Text" -> "txeT"                                                                   |

# Developer information

WIP - The bot is still in development and is shared as is.

## Writing own modules

### Add listener for event to react to

| Direction | Event            | Raised by       | Published when...                      |
|-----------|------------------|-----------------|----------------------------------------|
| In        | TwitchMessage    | TwitchConnector | Twitch messages are incoming from chat |
| In        | TwitchRaidEvent  | TwitchConnector | Twitch raid is incoming                |
| Out       | SendMessageEvent | PipBot          | Sending Messages to Twitch             |

### Process the event
~~~Kotlin
@ApplicationModuleListener
fun processEvent(message: TwitchMessage) {
    //processing...
}
~~~ 

### Publish a response event (optional)
~~~Kotlin
class YourProcessingClass(val publisher : ApplicationEventPublisher) {
    @ApplicationModuleListener
    fun processEvent(message: TwitchMessage) {
        //processing...
        publisher.publishEvent(SendMessageEvent(channel, message))
    }
}
~~~
