
@Service
public class EventManager {
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private AsyncEventBus asyncEventBus;

    @PostConstruct
    public void init() {
        asyncEventBus = new AsyncEventBus(executor);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
            }
        }));
    }


    public void notifyOnCreateBlacklistItem(Object createBlacklistItemReq) {
        asyncEventBus.post(createBlacklistItemReq);
    }

    public void register(Object object) {
        this.asyncEventBus.register(object);
    }
}
