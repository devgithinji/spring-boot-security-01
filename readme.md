# Spring Boot Password Encryption for Application Configuration File using Jasypt


## 1. What is Jasypt?


Jasypt stands for Java Simplified Encryption – a high-security and high-performance encryption library that allows developers to add basic encryption capabilities to their projects with minimal effort, without the need of having deep knowledge on how cryptography works.

Jasypt provides standard-based encryption techniques which can be used for encrypting passwords, texts, numbers, binaries… and it can integrate seamlessly and transparently with enterprise frameworks like Spring and Hibernate. Jasypt is easy to use yet highly configurable.


## 2. Declare dependencies for Jasypt Spring Boot and Jasypt Maven plugin

In order to use Jasypt library for a Spring Boot application, you need to declare the following dependency in the project’s pom.xml file:

```
<dependency>
  <groupId>com.github.ulisesbocchio</groupId>
  <artifactId>jasypt-spring-boot-starter</artifactId>
  <version>3.0.3</version>
</dependency>
```

This will add some JAR files to the project’s classpath, which help Jasypt to decrypt the encrypted values in the application configuration file transparently.

Then you also need to declare Jasypt Maven plugin as follows:

```
<plugin>
   <groupId>com.github.ulisesbocchio</groupId>
   <artifactId>jasypt-maven-plugin</artifactId>
   <version>3.0.3</version>
 </plugin>
```

This plugin is important as it allows use to use Maven commands for encryption and decryption, as described in the following sections.


## 3. Encrypt and Decrypt a single String value

Open a new command prompt window. Change the current directory to the project directory where the pom.xml file is in. And type the following command:

```
mvn jasypt:encrypt-value -Djasypt.encryptor.password=cafe21 -Djasypt.plugin.value=n@mHm2020
```

This will run Jasypt Maven plugin to encrypt the string n@mHm2020 using the default encryption configuration with the private key cafe21. In the output, you would see it prints something like this:

```
ENC(MBTWfX8gqMevQe5CKW0pToMbajnpJk0zlb3yoooiSWPjkfYrE8TFNF6vDEMXTu/j)
```

Here, the encrypted value is wrapped inside ENC(), then you can use replace a password in the configuration file by this value.

If you run the above command again, you will see a different encrypted value because the default encryptor uses a random generator. That means a string can be different encrypted value though the private key is the same.

The default encrypt algorithm is bidirectional, which means you can do decryption. Type the following command:

```
mvn jasypt:decrypt-value -Djasypt.encryptor.password=cafe21 -Djasypt.plugin.value=MBTWfX8gqMevQe5CKW0pToMbajnpJk0zlb3yoooiSWPjkfYrE8TFNF6vDEMXTu/j
```

This will decrypt the specified value using the default encryption configuration with the private key cafe21. Then you would see it prints the original value n@mHm2020.

So these encrypt and decrypt commands are the very basic ones you should be familiar with.


## 4. Encrypt credentials in application.properties file

Suppose that you want to encrypt username and password of a Spring data source in the following application.properties file:

```
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:mysql://localhost:3306/shopmedb
spring.datasource.username=root
spring.datasource.password=password
```

First, wrap the values of username and password inside DEC() as shown below:

```
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:mysql://localhost:3306/shopmedb
spring.datasource.username=DEC(root)
spring.datasource.password=DEC(password)
```


Here, DEC() is a placeholder that tells Jasypt what to encrypt, and the remaining values are untouched.

And in the command prompt, type:

```
mvn jasypt:encrypt -Djasypt.encryptor.password=cafe21
```

Then it will replace the DEC() placeholders in the application.properties file with the encrypted value:

```
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:mysql://localhost:3306/shopmedb
spring.datasource.username=ENC(9tl1aMX4Ije8n0+IcjyS...)
spring.datasource.password=ENC(IQi6U2g7sz4pw6wL4GoY...)
```

Voila! Very easy and convenient, right? No manual copy and paste. Just put the values you want to encrypt inside DEC() and run the mvn jasypt:encrypt command.


## 5. Run a Spring Boot application with Jasypt

Now, to run the Spring Boot application you need to pass the private key password as VM arguments in the command prompt like this:

```
java -Djasypt.encryptor.password=cafe21 –jar yourapp.jar
```


## 6. Decrypt credentials in Spring application configuration file

In case you want to see the original values of encrypted ones in the Spring Boot configuration file, type the following Maven command:

```
mvn jasypt:decrypt -Djasypt.encryptor.password=cafe21
```

Jasypt will print content of the application.properties file in the output, as it was before encryption. So this command would be useful for checking and verification purpose. Note that it doesn’t update the configuration file.


## 7. Encrypt credentials in application.yml file

By default, Jasypt will update the application.properties file. In case you’re using application.yml in your project, specify the path of the file in the command like this:

```
mvn jasypt:encrypt -Djasypt.encryptor.password=cafe21 -Djasypt.plugin.path="file:src/main/resources/application.yml"
```

Using this syntax, you can encrypt credentials in any properties file you wish to.


## 8. Re-encryption with new encryption password

If you want to change the encryptor’s private key (password), simply use this command:

```
mvn jasypt:reencrypt -Djasypt.plugin.old.password=cafe21 -Djasypt.encryptor.password=10duke
```

Then Jasypt Maven plugin will replace the values encrypted with the old password cafe21 with the new ones encrypted with the new password 10duke – and you get the configuration file updated instantly. Very convenient.


## 9. Configure encryptor in Spring configuration class

Jasypt is easy to use, as you’ve seen with the commands above. And it is also highly configurable if you have some knowledge in cryptography and you want to customize settings for the encryptor. For example, create a new Spring configuration class in the project as follows:

```
package net.codejava.security;
 
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
@Configuration
public class JasyptAdvancedConfig {
 
    @Bean(name = "jasyptStringEncryptor")
    public StringEncryptor getPasswordEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
     
        config.setPassword("password"); // encryptor's private key
     
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
     
        encryptor.setConfig(config);
     
        return encryptor;
    }
}
```

This code will override the default encryption configuration, so you need to write some code to encrypt a password like this:

```
String rawPassword = "password";
String encryptedPassword = encryptor.encrypt(rawPassword);
System.out.println(encryptedPassword);
```

Then update the Spring Boot application configuration file by putting the encrypted values inside ENC() like this:

```
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:mysql://localhost:3306/shopmedb
spring.datasource.username=ENC(encrypted_username)
spring.datasource.password=ENC(encrypted_password)
```
