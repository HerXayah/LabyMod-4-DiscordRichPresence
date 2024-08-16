package org.example.core.commands;

import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import org.example.core.ExampleAddon;
import org.example.core.listener.WebSocketManager;

public class ExamplePingCommand extends Command {


  public ExamplePingCommand() {
    super("ping", "pong");

    this.withSubCommand(new ExamplePingSubCommand());
  }

  ExampleAddon instance = ExampleAddon.getInstance();
  private final WebSocketManager manager = WebSocketManager.getInstance();

  @Override
  public boolean execute(String prefix, String[] arguments) {

    if (prefix.equalsIgnoreCase("ping")) {

      //displayMessage(this.instance.getDiscordID());
      //displayMessage(Component.text(manager.getActivities(), NamedTextColor.GRAY));

      Component component = manager.component;
      manager.prepareComponent();
      manager.setIcon();
      if (component == null) {
        return true;
      }
      this.displayMessage(component);

      return true;
    }
    this.displayMessage(Component.text("Pong!", NamedTextColor.GOLD));
    return true;
  }
}
