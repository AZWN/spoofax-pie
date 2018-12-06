package mb.spoofax.runtime.cfg;

import mb.fs.java.JavaFSPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Serial.Version(value = 1L)
public interface WorkspaceConfigPaths extends Serializable {
    @Value.Parameter List<JavaFSPath> langSpecConfigFiles();
}
