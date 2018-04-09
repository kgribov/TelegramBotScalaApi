[![](https://jitpack.io/v/kgribov/TelegramBotScalaApi.svg)](https://jitpack.io/#kgribov/TelegramBotScalaApi)
![alt text](https://travis-ci.org/kgribov/TelegramBotScalaApi.svg?branch=master)

# Telegram Bot Scala API
![bot logo](http://www.chatterbotcollection.com/images/telegram-bot_1436220825.png)
## Overview
Would like to write powerfull telegram bots using scala? This project is made for you.
Scala API is currently available for **Scala 2.12**.
If you don't have your own project where to add this API and you want to start using API as fast as possible, I highly recommend for you to look at this project: [TelegramBot](https://github.com/kgribov/TelegramBot).
It was also created by me for next purposes:
* You don't need to generate new project, it is already done, just clone repository with set up classes
* It contains all dependencies that you need, main classes and CLI parsing
* You could run your bot in **docker** container with **graphite**+**grafana** to enable metric and logs collecting with beautiful dashboards.

## Getting started
To add this API to your project you need:
* Add JitPack repository: `resolvers += "jitpack" at "https://jitpack.io"`
* Add API dependency: `libraryDependencies += "com.github.kgribov" % "TelegramBotScalaApi" % "master-SNAPSHOT"`

> Currently project doesn't have release version, but first release is coming soon. To get last version of API just execute: `sbt update` in your project

To start using API just add some helpful imports:
```
import com.kgribov.telegram.dsl._
import com.kgribov.telegram.security._
import scala.concurrent.duration._
```
Create bot schema and just start it!
```
new BotSchema("your_bot_api_key", "bot_name")
      .replyOnCommand("random", _ => s"Random number: ${Random.nextInt(100)}")
      .startBot()
```
This bot will return random number, just print `/random` in private chat with your bot.
> To create your own bot in telegram and get bot API key you should ask [BotFather](https://telegram.me/BotFather), he will help you with it.
> Don't forget to disable privacy rules for your bot, to read all messages in chat, BotFather could do it with command: `/setprivacy`, set status to **DISABLED**


## API features
This API contains lots of features, which will help you to create any kind of bot. You could discover examples of API usage in example package: [Examples](https://github.com/kgribov/TelegramBotScalaApi/tree/master/src/main/scala/com/kgribov/telegram/examples)
### Processing messages
### Processing commands
### Commands permissions
### Creating dialogs
### Creating quizes
