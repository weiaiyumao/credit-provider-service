package cn.activemq;

import javax.jms.Destination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import main.java.cn.domain.TrdOrderDomain;
import main.java.cn.domain.UserAccountDomain;

/**
 * mq 消息生产者
 * @author ChuangLan
 *
 */
@Service("producerService")
public class ProducerService {
	
	@Autowired
	private JmsMessagingTemplate jmsTemplate;

	public void sendMessage(Destination destination, final String message) {
		jmsTemplate.convertAndSend(destination, message);
	}
	
	public void sendMessage(Destination destination, final UserAccountDomain domain) {
		jmsTemplate.convertAndSend(destination, domain);
	}
	
	@JmsListener(destination = "out.queue")
	public void consumerMessage(TrdOrderDomain domain) {
//		jmsTemplate.
		System.out.println("从out.queue队列收到的回复报文为:" + domain.getClOrderNo());
	}
}
