# SimpleJDACommandHandler

Lightweight library to create commands for JDA using annotations, with autofill of parameters, cooldowns, and more.

## How to use

### Add the dependency

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.bruhitsalex</groupId>
        <artifactId>SimpleJDACommandHandler</artifactId>
        <version>1.0.2-SNAPSHOT</version>
    </dependency>
</dependencies>
```

#### Gradle

```groovy
repositories {
    maven { url = "https://jitpack.io" }
}

dependencies {
    implementation("com.github.bruhitsalex:SimpleJDACommandHandler:1.0.2-SNAPSHOT")
}
```

This library is written in Kotlin, but the method in Java is the same.

### Register the library with your Bot

When you initialise your bot, you need to add the `ICommandHandler` as an event listener.

The `ICommandHandler` constructor takes two arguments:

- The `JDA` instance
- The package name where your commands are located (e.g. `com.example.commands`)

```kotlin
fun main() {
    val token = System.getenv("TOKEN")
    bot = JDABuilder.createDefault(token)
        .addEventListeners(ICommandHandler(bot, "testbot.commands"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .build()
}
```

### Create a command

To create a command, you need to create a class that includes a `execute` function and annotate it with `@Command`.

A basic command looks like this:

```kotlin
@Command("ping")
class PongCommand {

    fun execute() {
        println("Pong!")
    }

}
```

You can also add additional properties to your command by using following annotations, including:

- `@Cooldown` - Adds a cooldown to the command
- `@RolesRequired` - Adds a list of roles that are required to use the command
- `@Description` - Adds a description to the command
- `@Usage` - Adds a usage to the command
- `@RestrictToChannels` - Adds a list of channels where the command can be used

```kotlin
const val ADMIN_ROLE_ID = "1097870977339237257"

@Command("ping")
@Cooldown(5)
@RolesRequired([ADMIN_ROLE_ID])
class PongCommand {

    fun execute() {
        println("Pong!")
    }

}
```

Finally, you can add parameters to your command. The parameters will be automatically filled with the arguments of the command.

The parameters can be of the following types:

- `MessageReceivedEvent` - The event that triggered the command
- `List<String>` - The arguments of the command
- `Member` - The member that triggered the command
- `Guild` - The guild where the command was triggered
- `TextChannel` - The channel where the command was triggered
- `Message` - The message that triggered the command
- `JDA` - The JDA instance
- `EmbedBuilder` - An template embed builder that you can use to create an embed

There are also parameters that come from the library internals, which may be useful for something like a help command. These are:

- `ICommandHandler` - The command handler instance
- `CommandRegistrator` - The command registrator instance
- `HandlerId` - The ID of the command handler
- `ICommand` - The command instance

In this example, I will use the `TextChannel` parameter to send a message to the channel where the command was triggered.

```kotlin
@Command("ping")
@Cooldown(5)
@RolesRequired([ADMIN_ROLE_ID])
class PongCommand {

    fun execute(channel: TextChannel) {
        channel.sendMessage("Pong!").queue()
    }

}
```