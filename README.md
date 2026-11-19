# AuthMC

AuthMC 是一个基于 **Microsoft 账号** 的 Bukkit/Paper 服务器登录验证插件。它使用微软的 **设备代码流程（Device Code Flow）**，让玩家通过浏览器完成账号授权，从而将 Minecraft 账号与微软账号绑定，实现安全、免密码的登录验证。

> 本项目使用 Java 与 Kotlin 混合编写，最低要求 Paper 1.20.x，依赖 [MoxLib](https://github.com/Moxqeon) 框架。

## 功能特性

- 🔐 **微软账号验证**：通过 Microsoft Graph API 获取并绑定玩家的微软账号邮箱。
- 🖥️ **设备代码流程**：玩家无需在游戏内输入密码，只需复制设备代码到浏览器完成授权。
- ⚡ **自动登录**：绑定成功或已通过验证的玩家自动完成登录。
- 🕐 **免验证登录**：支持基于 IP 与登录时间间隔的自动通过验证（见配置 `Login`）。
- 🚫 **行为限制**：未验证玩家将被限制移动、交互、聊天、命令、飞行与受伤。
- 🔌 **Skript 集成**：自动检测并拦截 Skript 的 `EffectCommandEvent`。
- 📢 **事件 API**：暴露 `PlayerVerifyEvent` 与 `PlayerPassVerifyEvent` 供其他插件监听。
- 🌐 **多语言支持**：内置可自定义的 `lang.yml`，支持 MiniMessage 格式与点击/悬浮交互。

## 依赖

| 依赖 | 类型 | 说明 |
| --- | --- | --- |
| [MoxLib](https://github.com/Moxqeon) | 必需 (`depend`) | 提供语言、持久化、HTTP 等基础能力 |
| [Skript](https://github.com/SkriptLang/Skript) | 可选 (`softdepend`) | 检测到后自动启用 Hook |
| Paper 1.20.x | 运行时 | 使用 Adventure 与 Paper API |

## 安装

1. 将 [MoxLib](https://github.com/Moxqeon) 与编译好的 `AuthMC` jar 放入服务器的 `plugins/` 目录。
2. （可选）如需 Skript 集成，将 Skript 也放入 `plugins/`。
3. 启动服务器，插件会自动生成默认配置与语言文件。
4. 根据需要修改 `config.yml`，然后重启服务器。

## 配置说明

`config.yml` 默认内容如下：

```yaml
Proxy:
  Enabled: true
  Address: 127.0.0.1:8080

Microsoft:
  ClientId: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

Login:
  CheckIP: true
  CheckLoginTime: 43200
```

| 路径 | 默认值 | 说明 |
| --- | --- | --- |
| `Proxy.Enabled` | `true` | 是否使用 HTTP 代理请求微软接口 |
| `Proxy.Address` | `127.0.0.1:8080` | 代理地址（`host:port`） |
| `Microsoft.ClientId` | `27b9a083-...` | 用于 OAuth 的微软应用 Client ID |
| `Login.CheckIP` | `true` | 是否在登录时校验玩家上次登录 IP |
| `Login.CheckLoginTime` | `43200` | 免验证登录的有效时间（秒），仅当 IP 校验通过时生效 |

> `Login.CheckIP` 与 `Login.CheckLoginTime` 共同决定玩家是否可以直接跳过验证：只有上次登录 IP 相同 **且** 距上次登录不超过设定秒数时才会自动通过。

## 语言文件

`lang.yml` 支持 MiniMessage 格式，并内置了多个可交互占位符：

| 占位符 | 说明 |
| --- | --- |
| `{prefix}` | 插件消息前缀（青色 `[AuthMC]`） |
| `{link}` | 点击后打开 `https://www.microsoft.com/link` 的可点击文本 |

常用文本节点包括：

- `Hint.Register` / `Hint.Login`：注册 / 登录提示（含 `{link}`）。
- `Hint.UserCode`：设备代码提示（支持点击复制代码）。
- `Hint.BindSuccess` / `Hint.LoginSuccess` / `Hint.LoginFail` / `Hint.PassLogin`：绑定、登录结果提示。
- `Hint.EventWarning`：未验证玩家触发受限操作时的提示。
- `Hover.LoginButton` / `Hover.UserCode.Ready` / `Hover.UserCode.NotReady`：悬浮提示。

## 事件 API

供其他插件监听的自定义事件：

- `PlayerVerifyEvent`：玩家验证完成时触发，包含 `isSuccess` 属性表示是否验证成功。
- `PlayerPassVerifyEvent`：玩家满足免验证条件、自动通过验证时触发。

## 开发

项目使用 Gradle 构建，Kotlin 与 Java 混合编译，JDK 21。

```bash
./gradlew build jar
```


## 许可

由 [Moxqeon](https://github.com/Moxqeon) 开发，保留所有权利。
