<!-- language: pt-BR -->
# Spring MQTTx — Biblioteca para Integração MQTT com Spring Boot

## Objetivo

A **Spring MQTTx** é uma biblioteca desenvolvida para facilitar a comunicação entre aplicações Spring Boot e brokers MQTT como **Mosquitto** e **EMQX**, usando uma abordagem **de anotações (@MqttPublisher e @MqttSubscriber)**.

Ela abstrai toda a configuração e o gerenciamento do cliente MQTT, permitindo publicar e consumir mensagens de forma simples, segura e modular.

---

## Estrutura e funcionalidades

- **@MqttPublisher** → Publica mensagens automaticamente após a execução de um método.
- **@MqttSubscriber** → Assina tópicos MQTT e direciona mensagens para métodos anotados.
- **@MqttPayload** → Faz o binding automático do payload recebido em JSON para objetos Java.
- **Configuração automática** via `application.properties`.

---

## Instalação local (sem precisar de repositório remoto)

### 1️ - Baixar o `.jar`

Baixe o arquivo `spring-mqttx-0.0.1.jar` disponível neste repositório.

Coloque-o dentro de uma pasta (exemplo: `C:\Users\SeuUsuario\Downloads`)).

---

### 2️ - Instalar o `.jar` no Maven local

Abra o **Prompt de Comando (cmd)** e vá até a pasta onde está o `.jar`:

```bash
cd C:\Users\SeuUsuario\Downloads
```

Execute o comando abaixo:

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

## Pré-requisitos

### Java

A biblioteca requer **Java 17+** (recomendado Java 21).  
Certifique-se de que o Java está instalado e configurado no PATH:

```bash
java --version
```

Saída esperada:
```
java 21.0.7 2025-04-15 LTS
Java(TM) SE Runtime Environment ...
```

### Maven

A ferramenta **Maven** é usada para instalar e gerenciar dependências.  
Verifique se o Maven está configurado corretamente:

```bash
mvn -v
```

Se o comando não for reconhecido, configure o Maven do IntelliJ:

1. Abra o IntelliJ → `File > Settings > Build, Execution, Deployment > Build Tools > Maven`
2. Copie o caminho do Maven embutido, ex:
   ```
   C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin
   ```
3. Adicione o caminho às **variáveis de ambiente**:
   - Nome: `MAVEN_HOME`
   - Valor: caminho copiado
   - Edite `Path` e adicione `%MAVEN_HOME%`
4. Feche e reabra o terminal, então teste novamente:
   ```bash
   mvn -v
   ```

---
### 3 - Adicionar a dependência no `pom.xml` do projeto

Depois da instalação local, basta adicionar a dependência normalmente:

```xml
<dependency>
  <groupId>com.rafaelcosta</groupId>
  <artifactId>spring-mqttx</artifactId>
  <version>0.0.1</version>
</dependency>
```

---

## Configuração no `application.properties`

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

## Exemplo de uso no código

### Publicar mensagens

```java
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
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
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
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

## Testando com Mosquitto

Antes de iniciar o projeto, certifique-se de que o **broker Mosquitto** está em execução.

### 1️ - Iniciar o broker local

```bash
mosquitto
```

> Isso inicia o broker na porta padrão `1883`.

---

### 2️ - Abrir um terminal para publicar mensagens

```bash
mosquitto_pub -h localhost -t "topico/teste" -m "Mensagem do terminal"
```

---

### 3️ - Abrir outro terminal para escutar

```bash
mosquitto_sub -h localhost -t "topico/teste"
```

Você verá as mensagens publicadas pelo seu app Java ou pelo terminal.

---

## Teste completo

1. Inicie o **Mosquitto**
2. Rode seu **projeto Spring Boot**
3. Veja no console:
   ```
   [MQTT] Conectado ao broker tcp://localhost:1883 com clientId=app-local
   ```
4. Publique uma mensagem no tópico:
   ```bash
   mosquitto_pub -h localhost -t "topico/teste" -m "Olá do Mosquitto!"
   ```
5. Observe o log:
   ```
   Mensagem recebida: Olá do Mosquitto!
   ```

---

## Pronto!

Agora seu projeto Spring Boot já pode publicar e receber mensagens MQTT de forma automática usando apenas anotações.

---


