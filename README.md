# KarmaAPI2
KarmaAPI2 is the new version of the KarmaAPI

## TODO

### Core API
- [X] [Source](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/source/KarmaSource.java)
- [X] [Source runtime enviroment](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/source/runtime/SourceRuntime.java)
- [X] [Source loader](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/source/SourceLoader.java)
- [X] [Source configuration](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/config/APIConfiguration.java)
- [X] [String utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/strings/StringUtils.java)
- [X] [Object utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/object/ObjectUtils.java)
- [X] [JavaVM utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/JavaVirtualMachine.java)
- [X] [Placeholder engine](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/strings/placeholder/PlaceholderEngine.java)
- [X] [Time calculator](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/time/TimeCalculator.java)
- [X] [Minecraft API](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/web/minecraft/MineAPI.java)
- [X] [Permission module](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/security/PermissionManager.java)

### Database API
- [X] [JSON database engine](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/database/model/JsonDatabase.java)
- [ ] MySQL database engine
- [ ] SQLite database engine
- [ ] SQL query builder

### Scheduler API
- [X] [Task scheduler](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/schedule/task/TaskScheduler.java)
- [X] [Scheduled task](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/schedule/task/ScheduledTask.java)
- [X] [Asynchronous scheduler](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/schedule/task/scheduler/AsynchronousScheduler.java)
- [X] [Task runner](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/schedule/runner/TaskRunner.java)

### File API
- [X] [Yaml parser (with comments support)](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/yaml/handler/YamlReader.java)
- [X] [Yaml helper](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/yaml/handler/YamlHandler.java)
- [X] [Yaml interpreter](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/yaml/YamlFileHandler.java)
- [X] [Path utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/util/PathUtilities.java)
- [X] [File utilities (replaced with path utilities)](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/util/PathUtilities.java)
- [X] [Stream utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/util/StreamUtils.java)
- [X] [Directory/File serializer](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/serializer/FileSerializer.java)

### Logging API
- [X] [Console logger](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/logger/log/BoundedLogger.java)
- [X] [File logger](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/logger/log/file/LogFile.java)
- [X] [Logger manager](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/logger/LogManager.java)

### Web API
- [X] [URL utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/web/url/URLUtilities.java)
- [X] [Version utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/version/checker/VersionChecker.java)

## Minecraft TODO

- [X] [KarmaPlugin](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/core/KarmaPlugin.java)

### Bukkit API
- [X] [Spigot scheduler](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/core/scheduler/SpigotTaskScheduler.java)
- [ ] Spigot version checker
- [X] [Bukkit server utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/spigot/server/SpigotServer.java)
- [X] [Title API](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/spigot/reflection/title/SpigotTitle.java)
- [ ] Actionbar API
- [ ] BossBar API
- [ ] Legacy support **(might be discarded)**
- [X] [ArmorStand trackers](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/spigot/tracker/stand/TrackerStand.java)
- [ ] Inventory API
- [X] [Cuboid region](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/spigot/region/RawCuboid.java)
- [ ] Hexagonal region
- [X] [Permission manager](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Spigot/src/main/java/es/karmadev/api/spigot/security/SpigotPermissionManager.java)

### BungeeCord API
- [ ] Spigot version checker (BungeeCord support)
- [ ] Title API (MakeItEasy)
- [ ] Actionbar API (MakeItEasy)
- [ ] BossBar API (MakeItEasy)

### Velocity API (Discarded)
KarmaAPI 2.0 won't be ported to Velocity