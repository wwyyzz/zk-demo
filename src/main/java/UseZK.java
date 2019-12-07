import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/*
实现Watcher接口，这个类就是一个Watcher
作为规范，其中的process方法负责接收zookeeper实例中已关注节点的通知。
 */
public class UseZK implements Watcher {
    Logger logger  =  Logger.getLogger(UseZK.class);

    //CountDownLatch倒数器负责在激活条件下放行下方代码
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;
    private static Stat stat = new Stat();

    public static void main(String[] args) throws Exception {
        String path = "/username";
        //连接一个zookeeper并注册一个默认的监听器
        zk = new ZooKeeper("127.0.0.1:2181", 5000, new UseZK());
        //等待zookeeper连接成功的通知
        connectedSemaphore.await();
        //获取path目录节点的配置数据，并注册默认的监听器
        System.out.println(new String(zk.getData(path, true, stat)));
        Thread.sleep(Integer.MAX_VALUE);
    }


    public void process(WatchedEvent event) {
        //连接成功
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if (Event.EventType.None == event.getType() && null == event.getPath()) {
                connectedSemaphore.countDown();
            } else if (event.getType() == Event.EventType.NodeDataChanged) {
                try {
                    System.out.println("配置已修改，新值为：" + new String(zk.getData(event.getPath(), true, stat)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
