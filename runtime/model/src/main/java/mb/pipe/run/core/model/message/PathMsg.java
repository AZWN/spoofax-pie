package mb.pipe.run.core.model.message;

import mb.vfs.path.PPath;

public interface PathMsg extends Msg {
    /**
     * @return Path the message belongs to.
     */
    PPath path();
}
