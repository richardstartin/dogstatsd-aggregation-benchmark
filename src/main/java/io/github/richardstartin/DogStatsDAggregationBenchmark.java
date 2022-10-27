package io.github.richardstartin;

import com.timgroup.statsd.CounterMessage;
import com.timgroup.statsd.StatsDAggregator;
import org.openjdk.jmh.annotations.*;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class DogStatsDAggregationBenchmark {

    private final SplittableRandom random = new SplittableRandom(0);
    private StatsDAggregator aggregator;

    @Param({"1"})
    int numAspects;

    @Param({"10"})
    int numTags;

    @Param({"C}~,C~_,D^~,D__,D`@,Da!,E?~,E@_,EA@,EB!", "0,1,2,3,4,5,6,7,8,9"})
    String rawTags;

    private String[] aspects;
    private String[] tags;
    private int numTagValues;


    @Setup(Level.Trial)
    public void setup() {
        aggregator = new StatsDAggregator(null, 4, 1000);
        SplittableRandom random = new SplittableRandom(42);
        aspects = new String[numAspects];
        for (int i = 0; i < aspects.length; i++) {
            aspects[i] = lowerCaseName(random);
        }
        String[] tagValues = rawTags.split(",");
        tags = new String[numTags * tagValues.length];
        for (int i = 0; i < numTags; i++) {
            for (int j = 0; j < tagValues.length; j++) {
                tags[tagValues.length * i + j] = "tag" + i + ":" + tagValues[j];
            }
            shuffleRange(tags, random, tagValues.length * i, tagValues.length * (i + 1));
        }
        numTagValues = tagValues.length;
    }

    private static String lowerCaseName(SplittableRandom random) {
        char[] digits = String.valueOf(random.nextLong()).toCharArray();
        for (int j = 0; j < digits.length; j++) {
            digits[j] = (char) (digits[j] + 'a');
        }
        return new String(digits);
    }

    private static void shuffleRange(String[] data, SplittableRandom random, int from, int to) {
        for (int i = to; i > from + 1; i--) {
            int j = random.nextInt(from, to);
            String tmp = data[j];
            data[j] = data[i - 1];
            data[i - 1] = tmp;
        }
    }

    @Benchmark
    @Threads(4)
    public boolean aggregateCounter() {
        SplittableRandom random = this.random.split();
        String[] messageTags = new String[numTags];
        int index = random.nextInt(numTagValues);
        for (int i = 0; i < messageTags.length; i++) {
            messageTags[i] = tags[i * numTagValues + index];
        }
        String aspect = aspects[index % aspects.length];
        return aggregator.aggregateMessage(new CounterMessage(aspect, 1L, messageTags));
    }


}
