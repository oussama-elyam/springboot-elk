# ELK = E L K (Elasticsearch, Logstash and Kibana)

---
## what is ELK

# ELK Overview

| Component      | Definition                                                                 | Note                                                                                 |
|----------------|---------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Elasticsearch** | Stores and indexes logs for fast search and analytics                   | Must run **before** Logstash                                                       |
| **Logstash**      | Collects, parses, and forwards logs to Elasticsearch                    | Depends on Elasticsearch                                                          |
| **Kibana**        | Visualizes logs from Elasticsearch in dashboards and search            | Spring Boot app should run **after** the ELK stack is running                      |

## two way to setup elk
- download elk x3 zip file with same version https://www.elastic.co/downloads/past-releases#elasticsearch and configure .conf and yml file manualy => the problem that i faced is i should restart all software and delete old logs and data folder and kill old processor manualy after each time i changed a configuration and that take to much time and headake sometime.
- or use docker compose as i did in this project with simple docker compose up i can restart all softwares with the correct order docker will read logstash.conf from projects file that existe in same project (all file and configuration in same place and easy to restart all )

## Step-by-Step Guide to Run the ELK Logging Spring Boot Project

#### 1. Start the ELK Stack
> docker compose up -d

Wait **15–20 seconds** for Logstash and Elasticsearch to be fully up.

#### 2. Trigger the Spring Boot Endpoint
> http://localhost:8080/echo

#### 3. Check Logstash Indices
> docker exec -it logstash curl http://elasticsearch:9200/_cat/indices?v

(If `spring-logs` does not appear, Logstash has not forwarded logs yet.)

#### 4. Monitor Logstash Logs
> docker compose logs -f logstash

#### 5. Verify Elasticsearch Indices
> docker exec -it elasticsearch curl http://localhost:9200/_cat/indices?v

#### 6. Open Kibana
> http://localhost:5601/app/management/data/index_management/indices

#### 7. Kibana Setup
- 1. Go to **Stack Management → Index Patterns → Create Index Pattern**
- 2. **Index pattern**: `spring-logs*` as we defined in src/main/resources/logstash.conf
- 3. **Time filter field**: `@timestamp` → **Create**
- 4. Go to **Discover → select `spring-logs*`**
- 5. Apply **Last 24 hours** filter

✅ Your `"Echo Triggered"` logs should appear now.



## Runtime communication

- **[Spring Boot App]** --> (logs over TCP 5000) –-> **[Logstash]** -http9200--> **[Elasticsearch]** –http5601-> **[Kibana]**


---
### Branch: `main` in `logback-spring.xml` we defined:
- **Appender**: `LogstashTcpSocketAppender`
- **Behavior**: Logs are sent directly over TCP to **Logstash** on port `5000`.
- **Pipeline**: Logstash parses them and forwards to **Elasticsearch**, which makes them available in **Kibana**.
- **Advantage**: Real-time log streaming into the ELK stack.


### Branch: `release/file-based-logging` in `logback-spring.xml` we defined:
- **Appender**: `RollingFileAppender`
- **Behavior**: Logs are written to a local file (`app.log`).
- **Rotation**: Daily or when file size exceeds **10MB**. Old logs are rotated and retained up to 7 days or 1GB total.
- **Pipeline impact**: Logstash is **not** directly involved, so Kibana cannot see these logs unless you configure Logstash in src/main/resources/logstash.conf to read from the log files.
- **Advantage**: Keeps local copies of logs for auditing or environments without ELK.

---

### Purpose of logback-spring.xml
`logback-spring.xml` is used to configure logging in a Spring Boot application.  
It defines **appenders** (where logs go), **encoders** (how logs are formatted), and **policies** (rotation, size, retention).  
This allows flexible routing of logs either to files, external systems, or both.

### Purpose of `logstash-logback-encoder` dependency in pom.xml

- Provides **LogstashEncoder**, **LoggingEventCompositeJsonEncoder**, and other encoders to format logs as structured **JSON**.
- JSON logs are **machine-readable** and can be parsed easily by **Logstash** and **Elasticsearch**.
- Without this dependency, logs remain **plain text**, making parsing inconsistent or error-prone.
