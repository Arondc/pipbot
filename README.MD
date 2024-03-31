# Pipbot - A multichannel kotlin (twitch) chatbot

## Todos before adding Pipbot to GitHub

## Nice to have
1. Bot konfiguration in DB packen
   1. Oberflächensprache
2. Datenbank des alten Bots importieren
3. Tests schreiben

## Setup

bla bla bla yada yada yada

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
