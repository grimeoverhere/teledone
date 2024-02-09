
# TeleDone

TeleDone is inspired by Maxdone app, yet another one GTD app.

## Run Locally

Clone the project

```bash
  git clone https://github.com/grimeoverhere/teledone.git
```

Then open project and configure file **application.yml**

```yaml
  integration:
  telegram:
    bot-token: ${TELEGRAM_BOT_TOKEN:???}
    bot-creator-id: ${TELEGRAM_CREATOR_ID:???}
    bot-username: ${TELEGRAM_BOT_USERNAME:???}
  vk:
    app-id: ${VK_APP_ID:???}
    client-secret: ${VK_CLIENT_SECRET:???}
    service-access-key: ${VK_SERVICE_ACCESS_KEY:???}
    stt-poll-interval: 3000
  gpt:
    token: ${GPT_TOKEN:???}
```

## Deployment for Linux server

Download SDKMAN!

```bash
  $ curl -s "https://get.sdkman.io" | bash
  $ source "$HOME/.sdkman/bin/sdkman-init.sh"
```

Install java

```bash
  $ sdk install java 17-open
```

Install maven

```bash
  $ sdk install maven
```

Clone the project

```bash
  $ git clone https://github.com/grimeoverhere/teledone.git
```

Then open project and configure file **application.yml**

```yaml
  integration:
  telegram:
    bot-token: ${TELEGRAM_BOT_TOKEN:???}
    bot-creator-id: ${TELEGRAM_CREATOR_ID:???}
    bot-username: ${TELEGRAM_BOT_USERNAME:???}
  vk:
    app-id: ${VK_APP_ID:???}
    client-secret: ${VK_CLIENT_SECRET:???}
    service-access-key: ${VK_SERVICE_ACCESS_KEY:???}
    stt-poll-interval: 3000
  gpt:
    token: ${GPT_TOKEN:???}
```

Go to project root directory and build project

```bash
  $ mvn clean install
```

Next run the app

```bash
  $ mvn org.springframework.boot:spring-boot-maven-plugin:run
```

### Backlog

- [x] Delete tasks 
- [x] Edit tasks
- [ ] Add categories like work, study, health etc :fire:
- [ ] Webapp interface
- [ ] Checklist for task
- [x] 'how to run' guide in README.md
- [ ] Switch from VK STT to Yandex Speechkit: https://cloud.yandex.ru/docs/speechkit/stt/