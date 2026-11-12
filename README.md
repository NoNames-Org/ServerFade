# ServerFade

Plugin for **BungeeCord** & **Paper** that smoothly fades the screen to black on server switches and shows a loading screen while the player is moved. **No client mods needed**: works through a title and a resource pack shader.

![ServerFade](docs/screenshot.png)

---

# 🇷🇺 Русский

## 🌒 Что это

Вместо резкого «Подключение…» игрок видит плавное затемнение, экран загрузки с логотипом и анимированными точками, а на новом сервере игра плавно проявляется.

- Затемнение перед переходом: игрок уходит на другой сервер, только когда экран уже чёрный
- Экран подключения и загрузки мира заменён на логотип с индикатором
- Плавное проявление на новом сервере, пока прогружаются чанки
- Проявление из чёрного и при первом входе на прокси
- Переходы после кика или падения сервера не задерживаются

## 🔀 BungeeCord

Затемняются переходы по команде `/server`, по сообщению с бэкенда (NPC, меню) и из плагинов на прокси. Если другой плагин ждёт результат подключения, переход не трогается.

| Команда | Право | Описание |
|---|---|---|
| `/serverfade test` | `serverfade.admin` | Показать затемнение без перехода |
| `/serverfade reload` | `serverfade.admin` | Перечитать `config.yml` |

## 🎮 Paper

То же затемнение внутри одного сервера: телепорт на арену, смена мира и т.п. Вызывается другими плагинами через API.

| Команда | Право | Описание |
|---|---|---|
| `/fade test` | `serverfade.admin` | Затемнить и проявить |
| `/fade reload` | `serverfade.admin` | Перечитать `config.yml` |

## ⚙️ Конфигурация

- Время затемнения, удержания чёрного экрана и проявления (в тиках)
- Список серверов, переходы на которые и с которых не затемняются
- Проявление при первом входе на прокси можно выключить

## 🧩 API

```java
ScreenFade fade = ScreenFade.get();   // null, если ServerFade не включён

fade.fade(player, () -> player.teleport(arena))
        .thenAccept(done -> { /* false — игрок вышел раньше */ });
```

- `fade` — затемнить, выполнить действие на чёрном экране, проявить. Повторные вызовы присоединяются к идущему затемнению
- `darken` / `reveal` — затемнить и держать, проявить вручную
- Добавьте `ServerFade` в `depend` или `softdepend`

---

# 🇬🇧 English

## 🌒 What it is

Instead of an abrupt "Connecting…", the player sees a smooth fade to black, a loading screen with a logo and animated dots, and the game fades back in on the new server.

- Fade before the switch: the player is sent only once the screen is black
- Connecting and terrain loading screens are replaced with the logo and a loading indicator
- Smooth fade-in on the new server while chunks load
- Fade-in from black on the first proxy join too
- Redirects after a kick or a server crash are never delayed

## 🔀 BungeeCord

Switches by `/server`, by a backend plugin message (NPCs, menus) and by proxy plugins are faded. If another plugin waits for the connect result, the switch is left alone.

| Command | Permission | Description |
|---|---|---|
| `/serverfade test` | `serverfade.admin` | Show the fade without a switch |
| `/serverfade reload` | `serverfade.admin` | Reload `config.yml` |

## 🎮 Paper

The same fade inside one server: teleport to an arena, world change, etc. Triggered by other plugins through the API.

| Command | Permission | Description |
|---|---|---|
| `/fade test` | `serverfade.admin` | Fade out and back in |
| `/fade reload` | `serverfade.admin` | Reload `config.yml` |

## ⚙️ Configuration

- Fade-out, black hold and fade-in times (in ticks)
- Servers whose switches (to or from) are never faded
- Fade-in on the first proxy join can be turned off

## 🧩 API

```java
ScreenFade fade = ScreenFade.get();   // null if ServerFade is not enabled

fade.fade(player, () -> player.teleport(arena))
        .thenAccept(done -> { /* false: the player left before */ });
```

- `fade`: fade out, run the action on the black screen, fade in. Further calls join the running fade
- `darken` / `reveal`: fade out and hold, fade in manually
- Add `ServerFade` to `depend` or `softdepend`
