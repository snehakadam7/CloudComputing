import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class SparkKeySort {
    public static void main(String args[]) {
                SparkConf sparkConf = new SparkConf().setAppName("SparkKeySort");
                JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
                long start = System.currentTimeMillis();
                JavaRDD<String> textFile = sparkContext.textFile(args[0]);

                PairFunction<String, String, String> keyData =
                new PairFunction<String, String, String>() {
                    public Tuple2<String, String> call(String x) {
                        return new Tuple2(x.split("\r")[0], x);
                    }
                };
        String out = args[1];

        JavaPairRDD<String, String> pairs = textFile.mapToPair(keyData);
        JavaPairRDD<String, String> sorted = pairs.sortByKey(true,1);
        sorted.map(x->x._2+"\r").saveAsTextFile(out);

        long end = System.currentTimeMillis();
        long timeTaken  = end - start;
        System.out.println("Time taken: "+timeTaken+" milliseconds");
    }
}
