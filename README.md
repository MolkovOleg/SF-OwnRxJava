# OwnRxJava

Простая реализация основных концепций RxJava на Java. Этот проект представляет собой облегченную версию библиотеки ReactiveX для Java, демонстрирующую основные принципы реактивного программирования.

## Описание проекта

OwnRxJava реализует ключевые компоненты реактивного программирования:

- **Observable** - источник данных, который может испускать элементы, ошибки и сигналы завершения
- **Observer** - подписчик, который получает уведомления от Observable
- **Disposable** - механизм для отмены подписки
- **Scheduler** - планировщик для управления потоками выполнения

Проект включает следующие основные операторы:
- `map` - преобразование элементов
- `filter` - фильтрация элементов
- `flatMap` - преобразование элементов в новые Observable и объединение результатов

Также реализованы различные типы планировщиков:
- `CachedThreadScheduler` - использует пул потоков с динамическим созданием новых потоков
- `ComputationScheduler` - использует фиксированный пул потоков для вычислительных задач
- `SingleThreadScheduler` - выполняет задачи в одном выделенном потоке

## Требования

- Java 21
- Maven 3.6+

## Примеры использования

### Базовое использование Observable

```java
Observable<Integer> observable = Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onNext(3);
    observer.onComplete();
});

observable.subscribe(
    item -> System.out.println("Получено: " + item),
    error -> System.out.println("Ошибка: " + error),
    () -> System.out.println("Завершено")
);
```

### Использование операторов

```java
Observable<Integer> observable = Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onComplete();
});

// Использование map
observable.map(x -> x * 10)
    .subscribe(
        item -> System.out.println("Результат map: " + item),
        error -> System.out.println("Ошибка: " + error),
        () -> System.out.println("Завершено")
    );

// Использование filter
observable.filter(x -> x % 2 == 0)
    .subscribe(
        item -> System.out.println("Результат filter: " + item),
        error -> System.out.println("Ошибка: " + error),
        () -> System.out.println("Завершено")
    );
```

### Использование планировщиков

```java
CachedThreadScheduler scheduler = new CachedThreadScheduler();

try {
    Observable<Integer> observable = Observable.create(observer -> {
        observer.onNext(1);
        observer.onNext(2);
        observer.onComplete();
    });

    observable
        .subscribeOn(scheduler)  // Выполнение источника в отдельном потоке
        .observeOn(scheduler)    // Обработка результатов в отдельном потоке
        .subscribe(
            item -> System.out.println("Получено: " + item),
            error -> System.out.println("Ошибка: " + error),
            () -> System.out.println("Завершено")
        );
} finally {
    scheduler.shutdown();  // Важно завершить работу планировщика
}
```

## Тестирование

Проект содержит набор юнит-тестов, демонстрирующих работу основных компонентов:

- `ObservableTest` - тесты для Observable и основных операторов
- `ConcurrencyTest` - тесты для проверки многопоточной работы
- `OperationChainTest` - тесты для цепочек операторов

Запуск тестов:
```bash
mvn test
```

## Структура проекта

```
src/
├── main/java/
│   ├── components/
│   │   ├── Disposable.java     - интерфейс для отмены подписки
│   │   ├── Observable.java     - основной класс для создания потоков данных
│   │   ├── Observer.java       - интерфейс для получения уведомлений
│   │   └── Scheduler.java      - интерфейс для управления потоками
│   ├── schedulers/
│   │   ├── CachedThreadScheduler.java  - планировщик с динамическим пулом потоков
│   │   ├── ComputationScheduler.java   - планировщик для вычислительных задач
│   │   └── SingleThreadScheduler.java  - однопоточный планировщик
│   └── Main.java               - демонстрационные примеры
└── test/java/
    ├── ConcurrencyTest.java    - тесты многопоточности
    ├── ObservableTest.java     - тесты Observable и операторов
    └── OperationChainTest.java - тесты цепочек операторов
```
