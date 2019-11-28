# RabbitMQ
环境要求

jdk 1.8+


节点 | 角色
:--: | :--:
172.30.20.250 | 主节点
172.30.20.251 | 从节点
172.30.20.252 | 从节点

## 1. 下载
```
RabbitMQ-server
    https://www.rabbitmq.com/releases/rabbitmq-server
    下载
    rabbitmq-server-3.6.15-1.el7.noarch.rpm

Erlang
    http://packages.erlang-solutions.com/erlang-solutions-1.0-1.noarch.rpm
```

## 2. 安装
```
rpm -ivh rabbitmq-server-3.6.15-1.el7.noarch.rpm
```

## 3. 配置
cp /usr/share/doc/rabbitmq-server-3.6.15/rabbitmq.config.example /etc/rabbitmq/rabbitmq.config
vi /etc/rabbitmq/rabbitmq.config
```
%% 端口
{tcp_listeners, [5672]}

%% guest账号可通过ip访问rabbitmq页面，要把最后面的逗号去掉
{loopback_users, []}
```

## 4. 启动
```
修改/etc/hosts
172.30.20.250 node1
172.30.20.251 node2
172.30.20.252 node3

同步.erlang.cookie
scp /var/lib/rabbitmq/.erlang.cookie 172.30.20.251:/var/lib/rabbitmq/
scp /var/lib/rabbitmq/.erlang.cookie 172.30.20.252:/var/lib/rabbitmq/

启动
systemctl start rabbitmq-server.service
systemctl enable rabbitmq-server.service
```

## 5. 搭建集群
### 5.1 关闭从节点app
```
rabbitmqctl stop_app
```

### 5.2 将从节点加入到主节点
```
一个从节点运行
rabbitmqctl join_cluster --ram rabbit@node1

另一个从节点运行
rabbitmqctl join_cluster --disc rabbit@node1

在RabbitMQ集群中的节点只有两种类型：内存节点/磁盘节点，单节点系统只运行磁盘类型的节点。而在集群中，可以选择配置部分节点为内存节点。
为了保证集群的高可用性，必须保证集群中有两个以上的磁盘节点，来保证当有一个磁盘节点崩溃了，集群还能对外提供访问服务。
```

### 5.3 启动从节点app
```
rabbitmqctl start_app
```

### 5.4 查看集群状态
```
rabbitmqctl cluster_status
```

## 6. 启动web管理插件
```
所有节点执行
rabbitmq-plugins enable rabbitmq_management

访问
http://172.30.20.250:15672
默认用户名和密码为：
guest/guest
```

## 7. 用户管理
```
默认用户名和密码为：
guest/guest

查看用户：
rabbitmqctl list_users

修改密码：
rabbitmqctl change_password <username> <newPassword>

新增用户：
rabbitmqctl add_user <username> <password>

修改用户角色：
超级管理员(administrator)、监控(monitoring)、策略制定(policymaker)、普通管理者(management)、其他自定义名称
rabbitmqctl set_user_tags <username> <tag1> <tag2> ...

删除用户：
rabbitmqctl delete_user <username>
```

## 8. 权限管理
```
查看所有用户的权限
rabbitmqctl list_permissions [-p VHostPath]

查看单个用户的权限
rabbitmqctl list_user_permissions <username>

设置权限
rabbitmqctl set_permissions -p <VHostPath> <username> <conf-pattern> <write-pattern> <read-pattern>

清除用户权限
rabbitmqctl clear_permissions [-p VHostPath] <username>
```

## 9. 镜像策略
普通模式：默认的集群模式。

镜像模式：把需要的队列做成镜像队列，存在于多个节点，属于RabbitMQ的HA方案。只有配置了此模式，才能在主节点宕机后，正常使用。

### 9.1 设置
策略 | 结果
:--: | :--:
all | 队列镜像到所有节点
exactly | 
```
rabbitmqctl set_policy [-p <vhost>] [--priority <priority>] [--apply-to <apply-to>] <name> <pattern> <definition>
```
参数名称 | 描述
-- | --
-p | 可选参数，针对指定 vhost 下的exchange或 queue
–priority | 可选参数，policy 的优先级
–apply-to | 可选参数，策略适用的对象类型，其值可为 “queues”, “exchanges” 或 “all”.默认是”all”
name | policy 的名称
pattern | 匹配模式（正则表达式）
definition | 镜像定义，json 格式，包括三部分（ha-mode,ha-params,ha-sync-mode）具体配置见下表

definition参数详情
参数名称 | 描述
-- | --
ha-mode	| 指名镜像队列模式，其值可为”all”,”exactly”或”nodes”，all：表示在集群所有节点上进行镜像；exactly：表示在指定个数的节点上镜像，节点个数由 ha-params 指定；nodes：表示在指定节点上进行镜像，节点名称通过ha-params 指定。
ha-params | ha-mode模式需要用到的参数：exactly 模式下为数字表述镜像节点数，nodes 模式下为节点列表表示需要镜像的节点。
ha-sync-mode | 镜像队列中消息的同步方式，其值可为”automatic”或”manually”.

例子：
```
rabbitmqctl set_policy ha-all "^" '{"ha-mode":"all"}'
```

### 9.2 清除
```
rabbitmqctl clear_policy [-p <vhost>] <name>
```
### 9.3 查看
```
rabbitmqctl list_policies [-p <vhost>]
```



## 10. 使用
### 10.1 maven依赖
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 10.2 application.yml配置
```
spring:
  rabbitmq:
    # 集群节点
    addresses: 172.30.20.250:5672,172.30.20.251:5672,172.30.20.252:5672
    username: guest
    password: 123456
```

### 10.3 生产者
```
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Producer {
	@Autowired
	private AmqpTemplate rabbitTemplate;

	public void send() {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());// 24小时制
		String context = "hello " + date;
		System.out.println("Producer : " + context);
        // demo 为队列名称
		this.rabbitTemplate.convertAndSend("demo", context);
	}
}

```
```
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitmqDemoApplicationTests {
    @Autowired
    private Producer producer;

    @Test
    public void sendMessage() throws Exception {
        producer.send();
    }
}
```

### 10.4 消费者
```
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "demo")
public class Consumer {

	@RabbitHandler
	public void process(String hello) {
		System.out.println("Consumer  : " + hello);
	}
}
```

## 11. 问题
### 11.1 问题1
```
11月 26 16:06:16 node1 systemd[1]: rabbitmq-server.service: main process exited, code=exited, status=1/FAILURE
11月 26 16:06:17 node1 rabbitmqctl[40386]: Stopping and halting node rabbit@node1
11月 26 16:06:17 node1 rabbitmqctl[40386]: Error: unable to connect to node rabbit@node1: nodedown
11月 26 16:06:17 node1 rabbitmqctl[40386]: DIAGNOSTICS
11月 26 16:06:17 node1 rabbitmqctl[40386]: ===========
11月 26 16:06:17 node1 rabbitmqctl[40386]: attempted to contact: [rabbit@node1]
11月 26 16:06:17 node1 rabbitmqctl[40386]: rabbit@node1:
11月 26 16:06:17 node1 rabbitmqctl[40386]: * connected to epmd (port 4369) on node1
11月 26 16:06:17 node1 rabbitmqctl[40386]: * epmd reports: node 'rabbit' not running at all
11月 26 16:06:17 node1 rabbitmqctl[40386]: no other nodes on node1
11月 26 16:06:17 node1 rabbitmqctl[40386]: * suggestion: start the node
11月 26 16:06:17 node1 rabbitmqctl[40386]: current node details:
11月 26 16:06:17 node1 rabbitmqctl[40386]: - node name: 'rabbitmq-cli-15@node1'
11月 26 16:06:17 node1 rabbitmqctl[40386]: - home dir: .
11月 26 16:06:17 node1 rabbitmqctl[40386]: - cookie hash: Ij1egNr0lVU5H9TJmVAxfA==
11月 26 16:06:17 node1 systemd[1]: Failed to start RabbitMQ broker.
-- Subject: Unit rabbitmq-server.service has failed
-- Defined-By: systemd
-- Support: http://lists.freedesktop.org/mailman/listinfo/systemd-devel
-- 
-- Unit rabbitmq-server.service has failed.
```

原因：
```
rabbitmq-server和erlang版本不匹配
```

解决方法：
```
卸载重装
```

## 12. 扩展
### 12.1 移除节点
```
rabbitmqctl stop_app

rabbitmqctl reset

rabbitmqctl start_app
```
