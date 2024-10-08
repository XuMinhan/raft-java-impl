import net.RPC.entity.NodeIdAndOqId;
import net.RPC.entity.RemotingRequest;
import net.RPC.entity.RemotingResponse;
import net.RPC.entity.ResponseFuture;
import net.client.NettyClient;
import net.client.RemotingClient;
import net.server.NettyServer;
import net.server.RemotingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.RPC.entity.RPCKind.COMMAND_TEST;
import static org.junit.jupiter.api.Assertions.*;

public class ClientServerIntegrationTest {

    private NettyServer nettyServer;
    private RemotingServer remotingServer;
    private NettyClient nettyClient;
    private RemotingClient remotingClient;

    @BeforeEach
    public void setUp() {
        // 启动服务器
        remotingServer = new RemotingServer();
        remotingServer.registerProcessor(COMMAND_TEST, (ctx, request) -> {
            NodeIdAndOqId nodeIdAndOqId = new NodeIdAndOqId(1, request.getOpaque());
            RemotingResponse remotingResponse = new RemotingResponse(nodeIdAndOqId);
            remotingResponse.setSuccess(true);
            remotingResponse.setMessage("TestSuccess");
            return remotingResponse;
        });

        nettyServer = new NettyServer(8080, remotingServer);
        new Thread(() -> {
            try {
                nettyServer.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 启动客户端
        remotingClient = new RemotingClient();
        nettyClient = new NettyClient(remotingClient);
        nettyClient.start();
    }

    @AfterEach
    public void tearDown() {
        // 关闭客户端和服务器
        nettyClient.shutdown();
        nettyServer.shutdown();
    }

    @Test
    public void testClientServerCommunication() throws InterruptedException {
        // 连接服务器
        nettyClient.testConnect("127.0.0.1", 8080);

        // 客户端发送请求
        RemotingRequest command = new RemotingRequest();
        command.setKind(COMMAND_TEST);
        ResponseFuture future = remotingClient.testSend(command);

        // 验证响应
        assertNotNull(future);
        assertTrue(future.getSuccess(), "Request failed.");

        RemotingResponse response = future.getResponse();
        assertNotNull(response);
        assertEquals("TestSuccess", response.getMessage(), "Unexpected response message.");
        assertEquals(command.getOpaque(), response.getMsgId().getOqId(), "Opaque ID mismatch.");
    }
}
