package uk.co.mrry.mercator.mapreduce;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.serializer.Serializer;
import org.apache.hadoop.io.serializer.WritableSerialization;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.Mapper.Context;


public class SWOutputCollector<K extends Writable, V extends Writable> 
  extends org.apache.hadoop.mapreduce.RecordWriter<K,V> {
  
  private final Partitioner<K,V> partitioner;
  private final ArrayList<TreeMap<K, List<V>>> partitions;
  private final int numPartitions;
  private final WritableSerialization serialization;
  private final FileOutputStream[] outputStreams;
  
  @SuppressWarnings("unchecked")
  public SWOutputCollector(Partitioner partitioner, FileOutputStream[] outputs) 
    throws IOException, ClassNotFoundException {
    numPartitions = outputs.length;
    outputStreams = outputs;
    partitions = new ArrayList<TreeMap<K, List<V>>>(numPartitions);
    serialization = new WritableSerialization();
    this.partitioner = partitioner;
    
    for (int i = 0; i < outputs.length; ++i) {
      partitions.add(new TreeMap<K, List<V>>());
    } 
  }
  
  @Override
  public void write(K key, V value) throws IOException, InterruptedException {
    TreeMap<K, List<V>> partition = partitions.get(partitioner.getPartition(key, value, numPartitions));
    List<V> partitionList = partition.get(key);
    if (partitionList == null) {
      partitionList = new LinkedList<V>();
      partition.put(key, partitionList);
    }
    partitionList.add(value);
  }
  
  
  @Override
  public void close(TaskAttemptContext context
                    ) throws IOException,InterruptedException {
    // XXX: Hack to get around generics.
    Serializer<Writable> serializer = serialization.getSerializer(null);
    for (int i = 0; i < numPartitions; ++i) {
      serializer.open(outputStreams[i]);
      for (Entry<K, List<V>> e : partitions.get(i).entrySet()) {
        for (V v : e.getValue()) {
          serializer.serialize(e.getKey());
          serializer.serialize(v);
        }
      }
      serializer.close();
    }
  }
}