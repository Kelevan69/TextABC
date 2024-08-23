import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TextAnalyzer {
    private static final int TEXT_LENGTH = 100_000;
    private static final int NUM_TEXTS = 10_000;
    private static final int QUEUE_CAPACITY = 100;

    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private static final AtomicInteger maxACount = new AtomicInteger();
    private static final AtomicInteger maxBCount = new AtomicInteger();
    private static final AtomicInteger maxCCount = new AtomicInteger();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Поток генерации текстов
        executor.submit(() -> {
            for (int i = 0; i < NUM_TEXTS; i++) {
                String text = generateText();
                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Завершаем потоки
            queueA.add("EOF");
            queueB.add("EOF");
            queueC.add("EOF");
        });

        // Поток для поиска максимума 'a'
        executor.submit(() -> {
            try {
                while (true) {
                    String text = queueA.take();
                    if ("EOF".equals(text)) break;
                    int count = countChar(text, 'a');
                    maxACount.updateAndGet(max -> Math.max(max, count));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Поток для поиска максимума 'b'
        executor.submit(() -> {
            try {
                while (true) {
                    String text = queueB.take();
                    if ("EOF".equals(text)) break;
                    int count = countChar(text, 'b');
                    maxBCount.updateAndGet(max -> Math.max(max, count));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Поток для поиска максимума 'c'
        executor.submit(() -> {
            try {
                while (true) {
                    String text = queueC.take();
                    if ("EOF".equals(text)) break;
                    int count = countChar(text, 'c');
                    maxCCount.updateAndGet(max -> Math.max(max, count));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        executor.shutdown();

        // Ожидаем завершения всех потоков
        while (!executor.isTerminated()) {}

        // Выводим результаты
        System.out.println("Max 'a' count: " + maxACount.get());
        System.out.println("Max 'b' count: " + maxBCount.get());
        System.out.println("Max 'c' count: " + maxCCount.get());
    }

    private static String generateText() {
        StringBuilder sb = new StringBuilder(TEXT_LENGTH);
        for (int i = 0; i < TEXT_LENGTH; i++) {
            sb.append((char) ('a' + (int) (Math.random() * 3)));
        }
        return sb.toString();
    }

    private static int countChar(String text, char ch) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
}
