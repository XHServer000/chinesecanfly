# 中国人能飞（Chinese Can Fly）

一个 Minecraft **Fabric 模组**：在聊天框发送「我是中国人」，即可获得飞行能力，自由翱翔！

> 我是中国人，我能飞 🕊️

## 适用版本

| 项目 | 版本 |
| --- | --- |
| Minecraft | Java Edition 1.21.11 |
| Fabric Loader | ≥ 0.19.3 |
| Fabric API | ≥ 0.141.6+1.21.11 |
| Java | ≥ 21 |

## 功能

- **触发方式**：在聊天框输入 `我是中国人` 并发送。
- **飞行效果**：获得 3 分钟「飞行」Buff（绿色药水图标，效果图标为原版缓降样式）。
  - 获得 Buff 后立即进入**创造模式式飞行**（可悬停、升降），但**游戏模式不会改变**（仍为生存/冒险）。
  - Buff 期间每 tick 维持飞行权限，不会被意外重置；效果结束后自动关闭飞行（创造/旁观模式不受影响）。
- **音效**：触发时播放专属音乐（`chinacanfly.ogg`，当前为占位空文件，可自行替换为任意 OGG 音频，放置于 `src/main/resources/assets/chinesecanfly/sounds/`）。
- **冷却**：5 分钟冷却，冷却期间重复触发无效。
- **成就**：「中国人能飞」成就（冒险风格背景），由 `data/chinesecanfly/advancement/chinese_can_fly.json` 定义，触发后自动授予。

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/installer/)（≥ 0.19.3）。
2. 将 [Fabric API](https://modrinth.com/mod/fabric-api)（≥ 0.141.6+1.21.11）放入 `mods` 文件夹。
3. 将本模组的 `chinesecanfly-1.0.0.jar` 放入 `mods` 文件夹。
4. 启动游戏，进服后在聊天框发送 `我是中国人` 即可起飞。

## 从源码构建

需要 **JDK 21** 与网络环境。

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

构建产物位于 `build/libs/chinesecanfly-1.0.0.jar`。

> 国内网络下若 Gradle 发行版下载缓慢，`gradle/wrapper/gradle-wrapper.properties` 已默认指向腾讯镜像。

## 开发说明

- 项目结构遵循 Fabric 标准模板（`fabric-loom` 插件 + yarn mappings）。
- 主逻辑位于 `src/main/java/io/xhserver/chinesecanfly/`：
  - `ChineseCanFlyMod.java`：入口，注册效果/音效，监听聊天、加入、重生、每 tick 飞行维护。
  - `ChineseCanFlyEffect.java`：飞行状态效果。
- 资源文件位于 `src/main/resources/`：
  - `assets/chinesecanfly/icon.png`：模组图标。
  - `assets/chinesecanfly/textures/mob_effect/chinesecanfly.png`：飞行 Buff 图标（18×18）。
  - `assets/chinesecanfly/sounds/chinacanfly.ogg`：触发音效（占位）。
  - `data/chinesecanfly/advancement/chinese_can_fly.json`：成就定义。

## 许可证

[MIT](LICENSE) © 2026 XHServer
