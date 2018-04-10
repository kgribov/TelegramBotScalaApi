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

To catch any message and make some processing use methods: `onMessage` and `replyOnMessage`

First method is helpful, when you want to collect some info, f.e. increment counter:
```
onMessage(message => messagesCounter.addAndGet(1))
```

If you wanna to return some message to user, use method `replyOnMessage`.
You could also filter your messages, if you don't want to process messages with commands f.e.
In this example on message `count` we will return number of messages.
```
replyOnMessage(
    filter = message => message.text.equals("count"),
    reply = message => s"Your number of messages is ${messagesCounter.get()}"
)
```

Example: [ProcessAnyMessage](https://github.com/kgribov/TelegramBotScalaApi/blob/master/src/main/scala/com/kgribov/telegram/examples/ProcessAnyMessage.scala)

### Processing commands

To process commands use methods: `onCommand` to collect some info, and `replyOnCommand` to reply something to user.

`onCommand` usage example:
```
onCommand("add", message => {
    val inputNumber = Try(message.text.toInt).getOrElse(0)
    counter.addAndGet(inputNumber)
})
```
This code will add a number from the message `/add <number>` to counter

If you want to reply something to user on command, use merhod `replyOnCommand`:
```
replyOnCommand("random", _ => s"Random number: ${Random.nextInt(100)}")
```
So, if you type `/random` to bot, it will print random number.

Example: [OnCommandExample](https://github.com/kgribov/TelegramBotScalaApi/blob/master/src/main/scala/com/kgribov/telegram/examples/OnCommandExample.scala)

### Command's permissions

Sometimes you don't want to allow use your bot in group chats or only admin of the group could perform specific command.

You could use command's permissions to achieve this. Don't forget to import `security` package:
```
import com.kgribov.telegram.security._
```

If you want to allow only group chats, just pass `withPermissions = allowGroups()` into command method,
to allow only private chats `withPermissions = allowPrivateChats()`

Also you could specify group's or user's ids, which will be only allowed, f.e:
```
replyOnCommand("privateAction", _ => "Hi man!", withPermissions = allowPrivateChats(<my_user_id>))
```

This `privateAction` will be allowed in chat with you only (good way to test your bot before _production_)

If you want to allow command in group chats and for admin of chat only:
```
replyOnCommand("groupAdminAction", _ => "ADMIN IS HERE", withPermissions = allowGroups(ADMIN_ONLY))
```

**By default all commands allow everywhere.**


Example: [CommandsPermissionsExample](https://github.com/kgribov/TelegramBotScalaApi/blob/master/src/main/scala/com/kgribov/telegram/examples/CommandsPermissionsExample.scala)

### Creating dialogs

Processing messages and commands is not a big problem, lots of frameworks can do it. Our scala API provides cool feature, which help you to build a really powerful bots.
It is creating **dialogs**. Dialog is a sequence of bot's questions to users. With dialogs you could create registration forms, quizzes, games and etc.

You could assign creating dialog on some command with method `startDialogOnCommand`:
```
startDialogOnCommand("ask", askPersonalInfo, withPermissions = allowPrivateChats())
```

Okey, what is `askPersonalInfo`? It is an instance of `Dialog` class:
```
val askPersonalInfo = Dialog(
  questions = Seq(

    askSelectQuestion(
      "What is your gender",
      Seq("Male", "Female"),
      submitAlert = _ => "I like your answer!"
    ),

    askSelectQuestion(
      "What is your age (question is actual for 5 seconds)",
      Seq("Under 30", "30 and more"),
      questionTTL = Some(5.seconds)
    ),

    askQuestion("What is your name?"),

    submitAnswers(answers => {
      val allAnswers = answers.allTextAnswers
      s"Thanks for ask, your answers are: [${allAnswers.values.mkString(",")}]"
    })
  ),

  personalDialog = true,

  dialogTTL = 1.minute
)
```
Dialog have three params:
* `questions` - a sequence of questions, that you want to ask users
* `personalDialog` - a boolean flag, if true then dialog will accept only replies from user who typed command to start dialog, if false from anyone.
This helpful when your bot is using in group chats and you want to ignore other user's replies. By default this param is **true** and you could ommit it.
* `dialogTTL` - duration while your dialog is active. After it answers will be not accepted. By default this param is **5 minutes** and you could ommit it.

There are two types of questions:

**Text questions:**

You could ask simple text question using method `askQuestion`:
```
askQuestion("What is your name?")
```

**Select questions:**

If you have question with possible answers, you could use `askSelectQuestion`:
```
askSelectQuestion(
  "What is your age (question is actual for 5 seconds)",
  Seq("Under 30", "30 and more"),
  questionTTL = Some(5.seconds)
)
```
First param is a question text, second is possible answers.

Also, you could specify some additional (not required) params:
* **questionTTL** - duration while question is active, after this time bot will move to next question (no answers will be accepted for current question).
If **questionTTL** is None, bot will not move to next question till get answer from user.
* **submitAlert** - a function *answer* => *alert message*, what bot should print after user answer.
* **alreadyAnsweredAlert** - an alert message, when user has already gave answer to the question.

But what if you would like to reply something to users, after some users answers? For such purposes you could use `submitAnswers` question:
```
submitAnswers(answers => {
  val allAnswers = answers.allTextAnswers
  s"Thanks for ask, your answers are: [${allAnswers.values.mkString(",")}]"
})
```
You could discover all answers from users using class `DialogAnswers` and return some message to users.


If you want to build dynamic dialogs, when next questions depends on previous answers you should use methods: `askQuestionOnAnswers` and `askSelectQuestionOnAnswers`.
Instead of text question, they take function *answers* => *text question*.

Example: [DialogExample](https://github.com/kgribov/TelegramBotScalaApi/blob/master/src/main/scala/com/kgribov/telegram/examples/DialogExample.scala)

### Creating quizzes
TODO

Example: [QuizExample](https://github.com/kgribov/TelegramBotScalaApi/blob/master/src/main/scala/com/kgribov/telegram/examples/QuizExample.scala)
