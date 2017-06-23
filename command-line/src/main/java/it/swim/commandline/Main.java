package it.swim.commandline;

//import com.google.common.collect.ImmutableMap;

import it.swim.client.SwimClient;
import it.swim.util.Uri;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static it.swim.util.Uri.parse;
import static java.lang.String.join;
import static java.util.Collections.emptyList;

public class Main {

  private static final String HELP = "help";
  private static final String LINK = "link";
  private static final String SYNC = "sync";
  private static Command linkCommand = new Command(LINK, LINK + " to a given host, node and lane ",
          LINK + " [host] [node] [lane]", false, 3);
  private static Command syncCommand = new Command(SYNC, SYNC + " to a given host, node and lane ",
          SYNC + " [host] [node] [lane]", true, 3);
  private static Option helpOption = new Option(HELP, "-" + HELP.substring(0, 1) + " | --" + HELP,
          "display help message");

  private static class Option {
    String key;
    String usage;
    String description;
    List<String> values;

    Option(final String key, final String usage, final String description) {
      this(key, usage, description, emptyList());
    }

    Option(final String key, final String usage, final String description, final List<String> values) {
      this.key = key;
      this.usage = usage;
      this.description = description;
      this.values = values;
    }
  }

  private static class Command {
    String key;
    String description;
    String usage;
    Boolean isSynced;
    int argCount;

    Command(final String key, final String description, final String usage, final Boolean isSynced, int argCount) {
      this.key = key;
      this.description = description;
      this.usage = usage;
      this.isSynced = isSynced;
      this.argCount = argCount;
    }
  }

    /*private static final Map<String, Option> OPTIONS = new ImmutableMap.Builder<String, Option>()
            .put(HELP, new Option(HELP, "-" + HELP.substring(0, 1) + " | --" + HELP,
                    "display help message"))
            .build();
    private static final Map<String, Command> COMMANDS = new ImmutableMap.Builder<String, Command>()
            .put(LINK, new Command(LINK, LINK + " to a given host, node and lane ",
                    LINK + " [host] [node] [lane]", false, 3))
            .put(SYNC, new Command(SYNC, SYNC + " to a given host, node and lane ",
                    SYNC + " [host] [node] [lane]", true, 3))
            .build();*/

  public static void main(final String[] args) throws InterruptedException, IOException {
    List<String> commandLineArgs = new ArrayList<String>();
    boolean help = false;

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-h") || args[i].equals("--h")) {
        help = true;
      } else {
        commandLineArgs.add(args[i]);
      }
    }
    if (help) {
      display(getHelp());
    } else {

      if (commandLineArgs.isEmpty()) {
        displayError(getHelp("no command specified"));
      } else {
        if (!commandLineArgs.get(0).equalsIgnoreCase("sync") &&
                !commandLineArgs.get(1).equalsIgnoreCase("link")) {
          displayError(getHelp("invalid command"));
        } else if (commandLineArgs.size() != 4) {
          displayError(getHelp("invalid command arguments"));
        } else {
          Uri host = parse(commandLineArgs.get(1));
          Uri node = parse(commandLineArgs.get(2));
          Uri lane = parse(commandLineArgs.get(3));

          if (host == null || node == null || lane == null) {
            displayError(getHelp("invalid command options"));
          } else {
            SwimClient client = new SwimClient();
            final CountDownLatch didSync = new CountDownLatch(1);

            client.start();
            client.downlink()
                    .hostUri(host)
                    .nodeUri(node)
                    .laneUri(lane)
                    .keepLinked(true)
                    .keepSynced(true)
                    .onEvent(event -> {
                      String msg = event.toRecon();
                      System.out.println(msg);
                    })
                    .open();
            didSync.await();
          }
        }
      }
    }
  }

  private static void display(final String message) throws IOException {
    System.out.println(message);
  }

  private static void displayError(final String message) throws IOException {
    display(message);

    System.exit(0);
  }

  private static String getHelp() throws IOException {
    return getHelp(null);
  }

  private static String getHelp(final String error) throws IOException {
    StringBuilder options = new StringBuilder();
    options.append(MessageFormat.format("  {0}: {1}{2}\n", pad(helpOption.usage), helpOption.description));


    StringBuilder commands = new StringBuilder();

    commands.append(MessageFormat.format("  {0}: {1}\n", pad(linkCommand.usage), linkCommand.description));
    commands.append(MessageFormat.format("  {0}: {1}\n", pad(syncCommand.usage), syncCommand.description));

    return MessageFormat.format("{0}usage: {1} [options] [commands]\n" +
                    "\n" +
                    "options:\n" +
                    options.toString() +
                    "\n" +
                    "commands:\n" +
                    commands.toString() +
                    "\n",
            (error != null) ? "ERROR: " + error + "\n\n" : "", "swim-command-line");
  }

  private static String pad(final String message) {
    return String.format("%1$-38s", message);
  }
}