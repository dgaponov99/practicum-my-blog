## Бэкенд приложения Мой блог

### Требования:
- jdk 21
- tomcat 10
- docker engine

### Настройка окружения
- Указать данные для подключения к реляционной базе данных в `portal-ext.properties`
- По умолчанию используется postgresql с версией драйвера `42.7.8`
- При необходимости использования другой реляционной СУБД: указать имя драйвера в переменную `spring.datasource.driverClassName` и добавить расположение драйвера в classpath приложения

### Запуск тестов:
- Запуск docker-engine
- Вызов `mvnw clean test`

### Установка:
- Собрать WAR с помощью `mvnw clean package` (для прохождения тестов понадобится запущенный docker engine)
- Запустить tomcat 10 с указанием JAVA_HOME='jdk-21-path'
- Собранный ROOT.war поместить в `/tomcat/webapp` предварительно удалив изначальную директорию ROOT
- После установки API приложения доступно по `http://localhost:8080/`

### Запуск фронта
- `cd my-blog-front-app`
- `docker-compose up -d`
- Открыть http://localhost:80