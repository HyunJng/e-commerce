# 카프카 도커 컴포즈 설정
> GPT 도움을 받으며 제가 이해한 내용을 정리한 문서입니다.

## 전체 구조

* `services.kafka`: 실제 **카프카 브로커 1대**(단일 노드).
* `services.kafka-ui`: 웹 UI(브라우저로 토픽/컨슈머 그룹 등 확인).
* `networks.kafkanet`: 두 컨테이너가 같은 가상 네트워크를 쓰도록 묶음.

---

## kafka 브로커 환경 설정
### KRaft 모드 기본 세팅.
> **KRaft 모드란?**
>
> 기존 카프카는 Zookeeper가 메타데이터/클러스터 관리를 담당하였으나,
> KRaft 모드에서는 카프카 브로커 자체가 이 역할을 수행하도록 하여
> 성능 면에서 더 우수하고 운영이 간편해져서 최근에 더 권장되는 방식이라고 한다
> 
> 참조: https://brunch.co.kr/@peter5236/19

```yaml
KAFKA_NODE_ID: 1
KAFKA_PROCESS_ROLES: "broker,controller"
KAFKA_CONTROLLER_QUORUM_VOTERS: "1@kafka:29093"
```
* 용어 정리
    * `broker` = 메시지 저장/전달 역할
    * `controller` = 메타데이터 관리/리더 선출 역할
* 단일 노드이므로 브로커와 컨트롤러를 **한 프로세스에서 같이** 돌림.
* `…QUORUM_VOTERS`: 컨트롤러 투표자 목록(여기선 1개: `노드ID@호스트:컨트롤러포트`).

### 리스너 설정
> **리스너란?**
>
> 카프카 브로커는 네트워크 포트를 열어서 클라이언트/다른 브로커와 통신한다
> 접속하는 주체
> - 프로듀서/컨슈머 (애플리케이션)
> - 다른 브로커 (리더-팔로워 동기화)
> - 컨트롤러 (메타데이터 관리)
>
> -> 이들을 구분하기 위해 여러 개의 "리스너"를 설정할 수 있다.
>
> 즉 하나의 브로커가 여러 포트를 열고, 각 포트마다 다른 용도로 통신할 수 있다.

```yaml
# <listener-name>://<host>:<port>
KAFKA_LISTENERS: "PLAINTEXT://:9092,CONTROLLER://:29093,PLAINTEXT_HOST://:29092"
```

* 브로커가 **어떤 포트로 어떤 용도의 리스너**를 열지 선언.

    * `PLAINTEXT://:9092` → **컨테이너 내부/같은 도커 네트워크**에서 쓸 내부용(예: `kafka-ui`가 `kafka:9092`로 붙음).
    * `CONTROLLER://:29093` → KRaft 컨트롤러용(브로커 간 메타데이터 통신).
    * `PLAINTEXT_HOST://:29092` → \*\*호스트(내 PC)\*\*에서 붙을 외부용.

### Advertised 주소 설정
```yaml
KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092"
```

* **클라이언트(프로듀서/컨슈머)에게 “나(브로커)는 여기로 접속해” 라고 광고하는 주소**.

    * 도커 네트워크 내부 컨테이너들은 `kafka:9092`로,
    * 내 PC 등 외부는 `localhost:29092`로 안내받게 함.
* **핵심 포인트**: 여기 오타/불일치가 있으면, **프로듀서/컨슈머가 연결되자마자 끊기거나 무한 대기** 걸릴 수 있다.

```yaml
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT"
KAFKA_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
KAFKA_INTER_BROKER_LISTENER_NAME: "PLAINTEXT"
```

* 각 리스너의 **보안 프로토콜** 지정(여기서는 전부 `PLAINTEXT` = 암호화/인증 없음).
* 컨트롤러 역할은 `CONTROLLER` 리스너로, 브로커끼리(인터브로커)는 `PLAINTEXT`로 통신.

### 단일 브로커용 추가 설정
> **내부토픽이란?**
> 
> 운영에 필요한 메타데이터를 저장하는 특수 토픽
> - 오프셋 토픽: 컨슈머가 어디까지 읽었는지 저장
> - 트랜잭션 상태 토픽: 트랜잭션 상태 저장
>
> -> 이 토픽들은 복제/가용성이 중요하지만, 단일 브로커에서는 복제 불가능.
> 따라서 복제 팩터를 1로 낮춰야한다.

```yaml
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
```

* **단일 브로커**라서 복제 팩터를 1로 맞춘다.
* `MIN_ISR`(In-Sync Replicas 최소 수)도 1이어야 단일 노드에서 트랜잭션/오프셋 토픽 정상 동작.

### 개발 편의용 설정
```yaml
KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
```

* **토픽 자동 생성** 허용(개발 편의용).
* 운영에서는 보통 끄고(=false) 배포 스크립트로 명시 생성 권장.

### 클러스터 설정
```yaml
CLUSTER_ID: "hhplus-ecommerce"
```

* **KRaft 전용 클러스터 식별자**.
* 단일 노드라도 내부적으로는 “클러스터” 개념이라 **필수**.
* 데이터 볼륨 재사용 시 동일해야 부팅 이슈가 없다. 고정값 두거나 .env로 관리해도 됨.

### 카프카 로그 경로 설정
```yaml
KAFKA_LOG_DIRS: "/tmp/kraft-combined-logs"
```

* 카프카 **로그/메타데이터 저장 경로**(컨테이너 내부 경로).
* 로컬 개발은 볼륨 생략해도 되고, 영속화 원하면 `volumes:`로 호스트 디렉토리 연결해도 된다.

```yaml
networks:
  - kafkanet
```

* `kafka-ui`와 같은 가상 네트워크에 붙여 **서비스명이 호스트네임**처럼 동작(`kafka:9092`).

---

## kafka-ui 서비스(웹 UI)

```yaml
image: provectuslabs/kafka-ui:latest
ports:
  - "8080:8080"
environment:
  KAFKA_CLUSTERS_0_NAME: local
  KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
depends_on:
  - kafka
networks:
  - kafkanet
```

* 브라우저에서 `http://localhost:8080` 접속하면 **토픽/컨슈머 그룹/메시지**를 GUI로 확인 가능.
