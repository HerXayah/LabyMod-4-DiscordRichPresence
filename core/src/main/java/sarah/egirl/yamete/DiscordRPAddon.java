package sarah.egirl.yamete;

import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.entity.player.tag.PositionType;
import net.labymod.api.models.addon.annotation.AddonMain;
import sarah.egirl.yamete.listener.WebSocketManager;
import sarah.egirl.yamete.nametag.DiscordStatusTag;

@AddonMain
public class DiscordRPAddon extends LabyAddon<DiscordRPConfiguration> {

  private static DiscordRPAddon instance;
  private static final WebSocketManager manager = WebSocketManager.getInstance();

  public static DiscordRPAddon getInstance() {
    return instance;
  }

  @Override
  protected void enable() {
    instance = this;
    if (!configuration().enabled().get()) {
      manager.removeListener(manager::close);
      this.logger().info("Disabled the Addon");
      return;
    }
    manager.addListener(() -> {
      manager.filterData();
      manager.prepareComponent();
      manager.setIcon();
    });
    this.registerSettingCategory();
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
  protected Class<DiscordRPConfiguration> configurationClass() {
    return DiscordRPConfiguration.class;
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
