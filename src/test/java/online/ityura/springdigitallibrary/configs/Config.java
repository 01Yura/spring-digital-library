package online.ityura.springdigitallibrary.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    //    Это единственный экземпляр класса Config, создаётся при загрузке класса (eager initialization).
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    /*
    Приватный конструктор, чтобы нельзя было создать new Config() извне → это часть паттерна Singleton
    Загружает файл config.properties из resources в Properties
    Если файла нет → бросает RuntimeException
    Таким образом, конфиг загружается один раз при старте и кэшируется в памяти.
    */
    private Config() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("config.properties is not found in resources");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }


    public static String getProperty(String key) {
//        ПРИОРИТЕТ 1 - это системное свойство apiBaseUrl = ...
//        используется для кубера и продакшн
        String systemValue = System.getProperty(key);
        if (systemValue != null) return systemValue;

//        ПРИОРИТЕТ 2 - это переменная окружения apiBaseUrl - APIBASEURL
//        используется для докера
//        admin.username -> ADMIN_USERNAME
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null) return envValue;

//        ПРИОРИТЕТ 3 - это config.properties
//        используется для локального запуска прямо из IDE
        return INSTANCE.properties.getProperty(key);
    }
}

