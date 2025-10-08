# 📦 Spring MQTTx — Biblioteca para Integração MQTT com Spring Boot

## 🚀 Objetivo

A **Spring MQTTx** é uma biblioteca desenvolvida para facilitar a comunicação entre aplicações Spring Boot e brokers MQTT como **Mosquitto** e **EMQX**, usando uma abordagem **de anotações (@MqttPublisher e @MqttSubscriber)**.

Ela abstrai toda a configuração e o gerenciamento do cliente MQTT, permitindo publicar e consumir mensagens de forma simples, segura e modular.

---

## 🧩 Estrutura e funcionalidades

- **@MqttPublisher** → Publica mensagens automaticamente após a execução de um método.
- **@MqttSubscriber** → Assina tópicos MQTT e direciona mensagens para métodos anotados.
- **@MqttPayload** → Faz o binding automático do payload recebido em JSON para objetos Java.
- **Configuração automática** via `application.properties`.

---

## ⚙️ Instalação local (sem precisar de repositório remoto)

### 1️⃣ Baixar o `.jar`

Baixe o arquivo `spring-mqttx-1.0.0.jar` disponível neste repositório.

Coloque-o dentro da pasta principal do seu projeto (por exemplo: `libs/` ou `target/`).

---

### 2️⃣ Instalar o `.jar` no Maven local

Abra o **Prompt de Comando (cmd)** e vá até a pasta onde está o `.jar`:

```bash
cd caminho/para/seu/projeto
```

Execute o comando abaixo (ajuste o caminho do arquivo se necessário):

```bash
mvn install:install-file ^
  -Dfile=spring-mqttx-0.0.1.jar ^
  -DgroupId=com.rafaelcosta ^
  -DartifactId=spring-mqttx ^
  -Dversion=0.0.1 ^
  -Dpackaging=jar ^
  -DgeneratePom=true
```

Isso irá copiar a biblioteca para o repositório Maven local (`~/.m2/repository`), tornando-a disponível para qualquer outro projeto.

---

### 3️⃣ Configurar o Maven (se não estiver no PATH)

Se o comando `mvn` não for reconhecido, configure o **Maven do IntelliJ IDEA**:

1. Abra o IntelliJ → `File > Settings > Build, Execution, Deployment > Build Tools > Maven`
2. Copie o caminho do Maven embutido, normalmente:
   ```
   C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin
   ```
3. Adicione esse caminho às variáveis de ambiente do Windows:
   - Pesquise **“Variáveis de ambiente”**
   - Clique em **Editar variáveis de ambiente do sistema**
   - Em “Variáveis do sistema” → clique em **Novo**
     - Nome: `MAVEN_HOME`
     - Valor: caminho do Maven embutido
   - Edite a variável `Path` e adicione:
     ```
     %MAVEN_HOME%
     ```

4. Feche e reabra o cmd e teste:
   ```bash
   mvn -v
   ```

Deve aparecer algo como:
```
Apache Maven 3.9.11
Java version: 21.0.7
```

---

### 4️⃣ Adicionar a dependência no `pom.xml` do projeto

Depois da instalação local, basta adicionar a dependência normalmente:

```xml
<dependency>
  <groupId>com.rafaelcosta</groupId>
  <artifactId>spring-mqttx</artifactId>
  <version>0.0.1</version>
</dependency>
```

---

## ⚙️ Configuração no `application.properties`

Defina as propriedades de conexão com o broker MQTT:

```properties
# Nome da aplicação
spring.application.name=meu_projeto_mqtt

# Configuração do broker (exemplo local)
mqtt.broker-url=tcp://localhost:1883
mqtt.client-id=app-local
mqtt.username=
mqtt.password=

# Configurações adicionais
mqtt.clean-session=true
mqtt.automatic-reconnect=true
mqtt.keep-alive-interval=60
mqtt.connection-timeout=30
mqtt.default-qos=1

# Exemplo com SSL
# mqtt.broker-url=ssl://meu-broker:8883
# javax.net.ssl.trustStore=classpath:meu_truststore.jks
# javax.net.ssl.trustStorePassword=minhasenha
# javax.net.ssl.trustStoreType=JKS
```

---

## 💻 Exemplo de uso no código

### Publicar mensagens

```java
import com.rafaelcosta.modelo_comunicacao_mqtt.domain.annotation.MqttPublisher;
import org.springframework.stereotype.Service;

@Service
public class PublicadorService {

    @MqttPublisher("topico/teste")
    public String publicarMensagem() {
        return "Olá MQTT!";
    }
}
```

### Receber mensagens

```java
import com.rafaelcosta.modelo_comunicacao_mqtt.domain.annotation.MqttSubscriber;
import com.rafaelcosta.modelo_comunicacao_mqtt.domain.annotation.MqttPayload;
import org.springframework.stereotype.Component;

@Component
public class AssinanteHandler {

    @MqttSubscriber("topico/teste")
    public void receberMensagem(@MqttPayload String mensagem) {
        System.out.println("Mensagem recebida: " + mensagem);
    }
}
```

---

## ✅ Pronto!

Agora seu projeto Spring Boot já pode publicar e receber mensagens MQTT de forma automática usando apenas anotações.

---


