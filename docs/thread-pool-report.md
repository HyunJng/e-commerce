# 비동기 설정 보고서 
## 스레드 풀 설정
> 참고: https://mangkyu.tistory.com/425

```java
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(32);
        executor.setMaxPoolSize(32);
        executor.setThreadNamePrefix("async-");
        executor.setQueueCapacity(1000);
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        executor.getThreadPoolExecutor().prestartAllCoreThreads();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }
}
```
- **Thread 수와 Queue 크기**
    - Core/Max Pool: CPU 코어 수(16) x 2
    - Queue Capacity: 웹 API이므로 지연에 민감하고, 외부대기열(redis, kafka 예정)를 통해 비동기 큐를 적용하였므로 큐의 크기를 작게 잡았다.
    - 프로젝트는 외부 API와 DB 호출이 많기 때문에 대기 시간이 큰 편이라 Thread 수를 크게 잡는 편이 좋을 것이라 판단했다.
    - 외부 대기열이 스파이크를 흡수하기에 Max Pool을 통한 추가 처리를 하는 것보단, 
      Core과 Max의 크기를 동일하게 두어 항상 일정 수의 스레드만 운용하도록 하여 예측 가능성을 높였다.

- **ThreadNamePrefix** 
    - 스레드 이름을 구분하여 비동기 디버깅 편의성 확보했다
- **shutdown 설정**
    - setAwaitTerminationSeconds(30) : 종료 시 대기 시간을 두어 I/O 작업이 완료될 시간(30초)을 주었다

## 비동기 로깅 설정
```java
private static class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```
- MDC는 기본적으로 스레드마다 독립적이라 비동기로 가면 사라진다.
- 따라서 비동기로 작업을 처리할 때도 부모의 MDC값을 복사해서 사용할 수 있도록 TaskDecorator을 구현한다.
- 로그 상에서 traceId로 연속성을 유지시킬 수 있다.