# ELK = E L K (Elasticsearch, Logstash and Kibana)

---

# ELK Overview

| Component      | Definition                                                                 | Note                                                                                 |
|----------------|---------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Elasticsearch** | Stores and indexes logs for fast search and analytics                   | Must run **before** Logstash                                                       |
| **Logstash**      | Collects, parses, and forwards logs to Elasticsearch                    | Depends on Elasticsearch                                                          |
| **Kibana**        | Visualizes logs from Elasticsearch in dashboards and search            | Spring Boot app should run **after** the ELK stack is running                      |

**Question for beginners:** Why not just read logs in `.log` files or console?

**Answer:**
1. **Centralized Logging** → All services' logs in one place.
2. **Searchable & Structured** → JSON logs, easy to query and filter.
3. **Real-Time Monitoring** → Dashboards in Kibana, alerts possible.
4. **Historical Analysis** → Store and analyze past logs.
5. **Microservices Ready** → Aggregate logs across multiple services, find issues faster.

=> ELK makes debugging, monitoring, and analyzing large apps much easier and faster than classic logging.

## Managing ELK: Manual vs Docker Compose

#### Manual Installation

- **Process**: Download Elasticsearch, Logstash, and Kibana ZIP files (same version) from [Elastic Past Releases](https://www.elastic.co/downloads/past-releases#elasticsearch).
- **Configuration**: Edit `.conf` and `.yml` files manually for each component.
- **Problems faced**:
    - Must **restart all software** after any configuration change.
    - Need to **delete old logs and data folders**.
    - Manually **kill old processes** to free ports or rename default ports of e l k.
    - Time-consuming and error-prone, can cause headaches.

#### Docker Compose Approach (Used in This Project)

- **Process**: Use `docker-compose.yml` to start all ELK components.
- **Advantages**:
    - `docker compose up` starts **all components in the correct order**.
    - Automatically reads `logstash.conf` from the project folder.
    - All files and configurations are in **one place**.
    - Easy to **restart all services** after changes.
    - Avoids manual deletion of logs, data folders, or killing processes.

=> Using Docker Compose simplifies ELK setup, configuration, and restart, reducing time and errors.

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

=> Your `"Echo Triggered"` logs should appear now.



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
