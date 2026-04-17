## Бэкенд приложения Мой блог

### Требования:
- jdk 21
- docker engine

### Настройка окружения
- Указать данные для подключения к реляционной базе данных в `portal-ext.properties`
- По умолчанию используется postgresql с версией драйвера `42.7.8`
- При необходимости использования другой реляционной СУБД: указать имя драйвера в переменную `spring.datasource.driverClassName` и добавить расположение драйвера в classpath приложения

### Запуск тестов:
- Запуск docker-engine
- Вызов `gradlew clean test`

### Сборка jar:
- Вызов `gradlew clean bootJar`
- Собранный fat-jar находится в `/build/libs`

### Локальный запуск:
- Вызов `gradlew clean bootRun`
- После установки API приложения доступно по `http://localhost:8080/`

### Запуск фронта
- `cd my-blog-front-app`
- `docker-compose up -d`
- Открыть http://localhost:80