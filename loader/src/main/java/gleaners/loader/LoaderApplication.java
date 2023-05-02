package gleaners.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class LoaderApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(LoaderApplication.class, args);
    }
}
