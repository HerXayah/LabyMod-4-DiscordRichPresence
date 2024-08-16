package org.example.core;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.entity.player.tag.PositionType;
import net.labymod.api.models.addon.annotation.AddonMain;
import org.example.core.commands.ExamplePingCommand;
import org.example.core.listener.WebSocketManager;
import org.example.core.nametag.DiscordStatusTag;

@AddonMain
public class ExampleAddon extends LabyAddon<ExampleConfiguration> {

  private static ExampleAddon instance;
  private static final WebSocketManager manager = WebSocketManager.getInstance();

  public static ExampleAddon getInstance() {
    return instance;
  }

  @Override
  protected void enable() {
    instance = this;
    manager.addListener(() -> {
      manager.filterData();
      manager.prepareComponent();
      manager.setIcon();
    });
    this.registerSettingCategory();
    this.registerCommand(new ExamplePingCommand());
    this.logger().info("Enabled the Addon");
    manager.connect();
    this.labyAPI().tagRegistry().register(
        "discord_status",
        PositionType.BELOW_NAME,
        new DiscordStatusTag(
            Laby.references().renderPipeline(),
            Laby.references().renderPipeline().rectangleRenderer()
        )
    );
  }

  @Override
  protected Class<ExampleConfiguration> configurationClass() {
    return ExampleConfiguration.class;
  }

  public String getLatestTypeData() {
    return manager.getType();
  }

  public String getDiscordID() {
    return configuration().discordID().get();
  }

  public String getLatestData() {
    return manager.getLatestData();
  }
}
