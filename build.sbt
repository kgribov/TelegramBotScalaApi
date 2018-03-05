
lazy val telegramBotScalaApi = (project in file("."))
  .settings(
    name := "telegram_bot_scala_api",
    version := "0.1",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Dependencies.dependencies,
    assemblyJarName in assembly := s"${name.value}.jar",
    mainClass in Compile := Some("com.kgribov.telegram.examples.SimpleDialogWithKeyboard")
  )
