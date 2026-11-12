# ServerFade

Плагин для BungeeCord и Paper, который при переходе между серверами плавно затемняет экран и показывает экран загрузки, пока игрока переносит. Модов игрокам ставить не нужно, всё работает через титл и шейдер в серверном ресурспаке.

![ServerFade](docs/screenshot.png)

## Русский

### Как это выглядит

Обычно при переходе на другой сервер игрок видит резкое «Подключение…», а потом экран загрузки мира. С ServerFade экран сначала плавно уходит в чёрный, и только тогда игрока отправляют дальше. Пока он подключается, на чёрном фоне висит логотип с анимированными точками. На новом сервере картинка плавно проявляется, а чанки за это время успевают прогрузиться.

Так же экран проявляется и при первом входе на прокси. Если это не нужно, выключите в конфиге.

Если игрока кикнуло с сервера или сервер упал, переход не задерживается: игрок сразу уходит туда, куда его отправил прокси.

### BungeeCord

На прокси затемняются все обычные переходы: по команде `/server`, по сообщению с бэкенда (например, из NPC или меню) и из других плагинов на прокси. Если какой-то плагин ждёт результат подключения, ServerFade такой переход не трогает, чтобы ничего ему не сломать.

| Команда | Право | Что делает |
|---|---|---|
| `/serverfade test` | `serverfade.admin` | Показывает затемнение без перехода |
| `/serverfade reload` | `serverfade.admin` | Перечитывает `config.yml` |

### Paper

На самом сервере работает то же затемнение, только без перехода. Например, перед телепортом на арену или сменой мира. Само по себе оно не срабатывает, его вызывают другие плагины через API.

| Команда | Право | Что делает |
|---|---|---|
| `/fade test` | `serverfade.admin` | Затемняет экран и проявляет обратно |
| `/fade reload` | `serverfade.admin` | Перечитывает `config.yml` |

### Конфиг

В `config.yml` задаётся, сколько длится затемнение, сколько держится чёрный экран и сколько длится проявление, всё в тиках. Там же список серверов, переходы на которые и с которых не затемняются, и переключатель проявления при первом входе на прокси.

### Для разработчиков

```java
ScreenFade fade = ScreenFade.get();   // null, если ServerFade не включён

fade.fade(player, () -> player.teleport(arena))
        .thenAccept(done -> { /* false, если игрок вышел раньше */ });
```

`fade` затемняет экран, выполняет ваше действие, пока он чёрный, и проявляет обратно. Если вызвать его ещё раз, пока затемнение идёт, новый вызов присоединится к текущему, а не запустит второе.

Если нужно управлять самому, есть `darken`, который затемняет экран и держит его чёрным, и `reveal`, который проявляет его обратно.

Чтобы подключить API, добавьте `ServerFade` в `depend` или `softdepend` в `plugin.yml`.

## English

### What it looks like

Normally a server switch shows an abrupt "Connecting…" followed by the terrain loading screen. With ServerFade the screen fades to black first, and only then is the player sent on. While they connect, a logo with animated dots sits on the black screen. On the new server the picture fades back in, giving chunks time to load.

The same fade-in also plays on the first join to the proxy. You can turn that off in the config.

If the player was kicked or the server crashed, the switch isn't held up: the player goes straight to wherever the proxy sends them.

### BungeeCord

On the proxy, every normal switch is faded: the `/server` command, a plugin message from a backend (say, from an NPC or a menu) and switches started by other proxy plugins. If some plugin is waiting for the connect result, ServerFade leaves that switch alone so it doesn't break anything.

| Command | Permission | What it does |
|---|---|---|
| `/serverfade test` | `serverfade.admin` | Shows the fade without switching |
| `/serverfade reload` | `serverfade.admin` | Reloads `config.yml` |

### Paper

The same fade works inside a single server, just without a switch. For example before a teleport to an arena or a world change. It never runs on its own, other plugins trigger it through the API.

| Command | Permission | What it does |
|---|---|---|
| `/fade test` | `serverfade.admin` | Fades the screen out and back in |
| `/fade reload` | `serverfade.admin` | Reloads `config.yml` |

### Config

`config.yml` sets how long the fade-out, the black hold and the fade-in last, all in ticks. It also has a list of servers whose switches (to or from) are never faded, and a toggle for the fade-in on the first proxy join.

### For developers

```java
ScreenFade fade = ScreenFade.get();   // null if ServerFade is not enabled

fade.fade(player, () -> player.teleport(arena))
        .thenAccept(done -> { /* false if the player left before it finished */ });
```

`fade` fades the screen out, runs your action while it's black, then fades it back in. Calling it again while a fade is running joins the current one instead of starting a second.

If you want to control it yourself, `darken` fades out and holds the screen black, and `reveal` brings it back.

To use the API, add `ServerFade` to `depend` or `softdepend` in your `plugin.yml`.
