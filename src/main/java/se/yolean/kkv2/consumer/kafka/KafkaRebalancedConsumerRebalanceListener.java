package se.yolean.kkv2.consumer.kafka;

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

  private final Logger logger = LoggerFactory.getLogger(KafkaRebalancedConsumerRebalanceListener.class);
  
 @Override
 public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
    logger.info("Partition assigned");
    consumer.seekToBeginning(partitions);
  }
}
