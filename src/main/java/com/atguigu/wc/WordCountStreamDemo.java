package com.atguigu.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * TODO DataStream实现Wordcount：读文件（有界流）
 *
 * @author cjp
 * @version 1.0
 */
// 问题：如果出现找不到类异常：java.lang.NoClassDefFoundError:xxx
// 当代码丢到集群上运行时有些依赖是已经有了的所以不需要依赖，通常我们会在本地配置成scope为provided的形式,所以idea也要配合 做相应调整。
// run -》 edit configurations -》 applicantion =》 add dependencies with “provided” scope
public class WordCountStreamDemo {
    public static void main(String[] args) throws Exception {
        // TODO 1.创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // TODO 2.读取数据:从文件读
        DataStreamSource<String> lineDS = env.readTextFile("input/word.txt");

        // TODO 3.处理数据: 切分、转换、分组、聚合
        // TODO 3.1 切分、转换
        SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOneDS = lineDS
                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
                        // 按照 空格 切分
                        String[] words = value.split(" ");
                        for (String word : words) {
                            // 转换成 二元组 （word，1）
                            Tuple2<String, Integer> wordsAndOne = Tuple2.of(word, 1);
                            // 通过 采集器 向下游发送数据
                            out.collect(wordsAndOne);
                        }
                    }
                });

        // 得到每行数据   hello java
        //              hello flink
        //              hello world
        // lineDS.print();
        // System.out.println("-------------");

        // TODO 3.2 分组 keyBy 算子
        KeyedStream<Tuple2<String, Integer>, String> wordAndOneKS = wordAndOneDS.keyBy(
                // 参数 IN： 输入类型   KEY： key的类型
                new KeySelector<Tuple2<String, Integer>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Integer> value) throws Exception {
                        return value.f0;
                    }
                }
        );
        // TODO 3.3 聚合
        SingleOutputStreamOperator<Tuple2<String, Integer>> sumDS = wordAndOneKS.sum(1);

        // TODO 4.输出数据
        sumDS.print();

        // TODO 5.执行：类似 sparkstreaming最后 ssc.start()
        env.execute();
    }
}

/**
 * 接口 A，里面有一个方法a()
 * 1、正常实现接口步骤：
 * <p>
 * 1.1 定义一个class B  实现 接口A、方法a()
 * 1.2 创建B的对象：   B b = new B()
 * <p>
 * <p>
 * 2、接口的匿名实现类：
 * new A(){
 * a(){
 * <p>
 * }
 * }
 */