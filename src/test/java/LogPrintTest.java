import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class LogPrintTest {
    private static final Logger heartbeatLogger = LogManager.getLogger("heartbeat");
    @Test
    public void printLog(){
        heartbeatLogger.debug("123");
        heartbeatLogger.info("123");
        heartbeatLogger.trace("123");
        heartbeatLogger.fatal("123");
    }

}
