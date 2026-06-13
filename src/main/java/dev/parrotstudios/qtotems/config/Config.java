package dev.parrotstudios.qtotems.config;

import dev.parrotstudios.qtotems.QTotems;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Config {
  final String name;

  FileConfiguration config;

  public void loadConfigFile() {
    File configFile = new File("plugins/QTotems/" + name + ".yml");
    if (!configFile.exists()) {
      if (QTotems.getInstance().getResource(name + ".yml") != null) {
        QTotems.getInstance().saveResource(name + ".yml", false);
      } else {
        try {
          configFile.getParentFile().mkdirs();
          configFile.createNewFile();
        } catch (Exception e) {
          QTotems.getInstance().getLogger().severe("Failed to create config file: " + name + ".yml");
          e.printStackTrace();
        }
      }
    }
    config = YamlConfiguration.loadConfiguration(configFile);
  }

  protected abstract void loadFields();

  public void load() {
    loadConfigFile();
    loadFields();
  }

  public void reload(){
    load();
  }

}
