# Simple Java ORM

`simple-java-orm` — это учебный проект для изучения принципов Object-Relational Mapping (ORM) в Java. Этот проект создан исключительно в учебных целях и не рекомендуется для использования в реальных приложениях.

## Возможности

- **CRUD операции**: Простая реализация операций создания, чтения, обновления и удаления (CRUD).
- **Аннотации для маппинга**: Использование аннотаций для маппинга классов Java на таблицы базы данных.
- **Легкость в использовании**: Простой и интуитивно понятный API.

## Использование

### Основные аннотации

- **@Entity**: Помечает класс как сущность базы данных.
- **@Id**: Обозначает поле как первичный ключ.
- **@Column**: Указывает, что поле отображается на столбец таблицы базы данных.

### Пример кода

#### Определение сущности

```java
package com.aiivar.sjorm.model;

import com.aiivar.sjorm.annotations.Entity;
import com.aiivar.sjorm.annotations.Id;
import com.aiivar.sjorm.annotations.Column;

@Entity
public class User {
    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    // Getters and setters
}
```

#### Создание сессии и выполнение операций

```java
package com.aiivar.sjorm;

import com.aiivar.sjorm.model.User;
import com.aiivar.sjorm.session.Session;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
             Session session = new Session(connection)) {

            // Создание таблицы
            session.createTable(User.class);

            // Создание сущности
            User user = new User();
            user.setName("John Doe");
            user.setEmail("john.doe@example.com");
            session.save(user);

            // Чтение сущности
            User retrievedUser = session.find(User.class, user.getId());
            System.out.println("Retrieved User: " + retrievedUser.getName());

            // Обновление сущности
            user.setName("Jane Doe");
            session.update(user);

            // Удаление сущности
            session.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
