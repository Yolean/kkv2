package se.yolean.consumer.kafka;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.KafkaConsumerRebalanceListener;

import org.apache.kafka.common.TopicPartition;

@ApplicationScoped
@Identifier("config.rebalancer")
public class KafkaRebalancedConsumerRebalanceListener implements KafkaConsumerRebalanceListener {

  private static final Logger logger = LoggerFactory.getLogger(KafkaRebalancedConsumerRebalanceListener.class);
  
/* 
  @Override
  public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
    logger.info("Partition assigned!");

    Map<TopicPartition, Long> lastRecords = new HashMap<>();
    logger.info("Assigned partition: {}", partitions);

    consumer.endOffsets(partitions).forEach((topicPartition, offset) -> lastRecords.put(topicPartition, offset));
    
    lastRecords.forEach((topicPartition, offset) -> 
      consumer.seek(topicPartition, offset - 1));
  }
 */

 @Override
 public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
   logger.info("Partition(s) {} assigned", partitions);
    consumer.seekToBeginning(partitions);
  }
}
