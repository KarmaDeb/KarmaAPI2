# KarmaAPI2
KarmaAPI2 is the new version of the KarmaAPI

## TODO

### Core API
- [X] [Source](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/source/KarmaSource.java)
- [X] [Source runtime enviroment](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/source/runtime/SourceRuntime.java)
- [X] [Source loader](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/source/SourceLoader.java)
- [X] [Source configuration](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/core/config/APIConfiguration.java)
- [ ] String utilities
- [X] [JavaVM utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/JavaVirtualMachine.java)

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
- [ ] File utilities
- [X] [Stream utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/file/util/StreamUtils.java)

### Logging API
- [X] [Console logger](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/logger/log/BoundedLogger.java)
- [X] [File logger](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/logger/log/file/LogFile.java)
- [X] [Logger manager](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/logger/LogManager.java)

### Web API
- [ ] URL utilities
- [ ] HTTP helper
- [X] [Version utilities](https://github.com/KarmaDeb/KarmaAPI2/blob/master/KarmaAPI-Core/src/main/java/es/karmadev/api/version/checker/VersionChecker.java)

## Minecraft TODO

- [ ] KarmaPlugin

### Bukkit API
- [ ] Spigot version checker
- [ ] Bukkit server utilities
- [ ] Title API
- [ ] Actionbar API
- [ ] BossBar API
- [ ] Legacy support
- [ ] ArmorStand trackers
- [ ] Inventory API

### BungeeCord API
- [ ] Spigot version checker (BungeeCord support)
- [ ] Title API (MakeItEasy)
- [ ] Actionbar API (MakeItEasy)
- [ ] BossBar API (MakeItEasy)
